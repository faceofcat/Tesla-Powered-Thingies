package net.ndrei.teslapoweredthingies.machines.pump

import net.minecraft.block.ITileEntityProvider
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagString
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidTank
import net.minecraftforge.items.IItemHandlerModifiable
import net.ndrei.teslacorelib.TeslaCoreLib
import net.ndrei.teslacorelib.gui.*
import net.ndrei.teslacorelib.inventory.BoundingRectangle
import net.ndrei.teslacorelib.inventory.FluidTankType
import net.ndrei.teslacorelib.inventory.SyncProviderLevel
import net.ndrei.teslacorelib.localization.makeTextComponent
import net.ndrei.teslacorelib.netsync.SimpleNBTMessage
import net.ndrei.teslacorelib.tileentities.ElectricMachine
import net.ndrei.teslacorelib.tileentities.SidedTileEntity
import net.ndrei.teslapoweredthingies.client.ThingiesTexture
import net.ndrei.teslapoweredthingies.integrations.GUI_PUMP
import net.ndrei.teslapoweredthingies.integrations.localize
import net.ndrei.teslapoweredthingies.render.bakery.SelfRenderingTESR
import java.util.function.Consumer
import java.util.function.Supplier

class PumpEntity: ElectricMachine(SidedTileEntity::class.java.name.hashCode()) {
    private lateinit var tank: IFluidTank
    private lateinit var storage: IItemHandlerModifiable
    private var mode: PumpMode = PumpMode.AUTO

    private var scanner: PumpScanner? = null
    private var scannerInfo: PumpScanner.Info? = null

    //#region inventories, gui and sync

    override fun initializeInventories() {
        super.initializeInventories()

        this.tank = this.addSimpleFluidTank(6000, "Fluid Tank", EnumDyeColor.BLUE, 52, 25, FluidTankType.OUTPUT)
        this.storage = this.addSimpleInventory(9, "storage", EnumDyeColor.GREEN, "Blocks Buffer",
            BoundingRectangle.slots(115, 25, 3, 3),
            { stack, _ -> (stack.item is ItemBlock) && ((stack.item as? ItemBlock)?.block !is ITileEntityProvider) },
            { _, _ -> false},
            true)

        this.registerSyncStringPart(PumpEntity.SYNC_PUMP_MODE,
            Consumer { this.mode = PumpMode.valueOf(it.string) },
            Supplier { NBTTagString(this.mode.name) }, SyncProviderLevel.GUI)

        this.registerSyncTagPart(PumpEntity.SYNC_SCANNER,
            Consumer { this.scanner = PumpScanner.deserializeNBT(this, it) },
            Supplier { this.scanner?.serializeNBT() ?: NBTTagCompound() }, SyncProviderLevel.SERVER_ONLY)

        this.registerSyncTagPart(PumpEntity.SYNC_SCANNER_INFO,
            Consumer { this.scannerInfo = PumpScanner.deserializeInfoNBT(it) },
            Supplier { this.scanner?.serializeInfoNBT() ?: NBTTagCompound() }, SyncProviderLevel.GUI_ONLY)

        if (!super.isPaused()) super.togglePause()
    }

    override fun getGuiContainerPieces(container: BasicTeslaGuiContainer<*>) = super.getGuiContainerPieces(container).also {
        it.add(LockedInventoryTogglePiece(99, 27, this, EnumDyeColor.GREEN))

        it.add(object: BasicRenderedGuiPiece(99, 45, 14, 14, ThingiesTexture.MACHINES_TEXTURES.resource, 100, 96) {
            override fun drawForegroundTopLayer(container: BasicTeslaGuiContainer<*>, guiX: Int, guiY: Int, mouseX: Int, mouseY: Int) {
                super.drawForegroundTopLayer(container, guiX, guiY, mouseX, mouseY)

                if (this.isInside(container, mouseX, mouseY)) {
                    container.drawTooltip(if (this@PumpEntity.scannerInfo == null)
                        listOf(localize(GUI_PUMP, "no_scanner") { +TextFormatting.GRAY })
                    else listOf(
                        localize(GUI_PUMP, "scanned") {
                            +TextFormatting.RED
                            +this@PumpEntity.scannerInfo!!.scanned.makeTextComponent()
                        }
                    ), this.left + this.width, this.top)
                }
            }
        })

        it.add(object: ToggleButtonPiece(99, 63, 14,14, 1) {
            override fun drawBackgroundLayer(container: BasicTeslaGuiContainer<*>, guiX: Int, guiY: Int, partialTicks: Float, mouseX: Int, mouseY: Int) {
                GuiIcon.SMALL_BUTTON.drawCentered(container, this)
                super.drawBackgroundLayer(container, guiX, guiY, partialTicks, mouseX, mouseY)
            }

            override fun renderState(container: BasicTeslaGuiContainer<*>, state: Int, box: BoundingRectangle) {
                this@PumpEntity.mode.icon.drawCentered(container, this, true)
            }

            override fun drawForegroundTopLayer(container: BasicTeslaGuiContainer<*>, guiX: Int, guiY: Int, mouseX: Int, mouseY: Int) {
                super.drawForegroundTopLayer(container, guiX, guiY, mouseX, mouseY)

                if (this.isInside(container, mouseX, mouseY)) {
                    container.drawTooltip(mutableListOf(this@PumpEntity.mode.getLocalizedLabel()), mouseX - guiX, mouseY - guiY)
                }
            }

            override fun clicked() {
                this@PumpEntity.toggleWorkMode()
            }
        })
    }

    override fun getRenderers(): MutableList<TileEntitySpecialRenderer<TileEntity>> {
        return super.getRenderers().also { it.add(SelfRenderingTESR) }
    }

    fun getFluid(): FluidStack? = this.tank.fluid

    fun scannedChanged() {
        this.partialSync(PumpEntity.SYNC_SCANNER, true)
        this.partialSync(PumpEntity.SYNC_SCANNER_INFO, false)
    }

    private fun getRandomSlot(): Int {
        val slot = this.getWorld().rand.nextInt(this.storage.slots)
        (0 until this.storage.slots).forEach {
            val index = (slot + it) % this.storage.slots
            val stack = this.storage.getStackInSlot(index)
            if (!stack.isEmpty)
                return index
        }
        return -1
    }

    private fun toggleWorkMode() {
        this.mode = PumpMode.values()[(this.mode.ordinal + 1) % PumpMode.values().size]

        @Suppress("SENSELESS_COMPARISON")
        if (this.getWorld() != null) {
            if (TeslaCoreLib.isClientSide) {
                val nbt = this.setupSpecialNBTMessage("SET_PUMP_MODE")
                nbt.setString("pump_mode", this.mode.name)
                this.sendToServer(nbt)
            }
            else {
                this.partialSync(PumpEntity.SYNC_PUMP_MODE)
            }
        }
    }

    override fun processClientMessage(messageType: String?, compound: NBTTagCompound): SimpleNBTMessage? {
        if (messageType == "SET_PUMP_MODE") {
            val mode = compound.getString("pump_mode")
            this.mode = PumpMode.valueOf(mode)
            this.markDirty()
        }
        return super.processClientMessage(messageType, compound)
    }

    //#endregion

    override val energyForWork: Int get() = 200
    override val minimumWorkTicks: Int get() = 5

    fun neighborChanged(pos: BlockPos) {
        if (pos == this.getPos().down()) {
            this.scanner = null
        }
    }

    override fun performWork(): Float {
        if (this.pos.y > 0) {
            val under = this.getWorld().getBlockState(this.pos.down())
            val fluidBlock = under.getFluidWrapper() // .block as? IFluidBlock
            if (fluidBlock != null) {
                val stack = this.getRandomSlot()
                val fluid = fluidBlock.fluid
                if ((stack < 0) && (fluid == FluidRegistry.WATER) && (this.mode == PumpMode.AUTO)) {
                    // special case, water
                    val nearby = EnumFacing.HORIZONTALS.map {
                        val nearbyFluid = this.getWorld().getBlockState(this.pos.down().offset(it)).getFluidWrapper()
                        if ((nearbyFluid != null) && (nearbyFluid.fluid == fluid)) 1 else 0
                    }.sum() + 1
                    val amount = 200 * nearby
                    this.tank.fill(FluidStack(fluid, amount), true)
                    return 1.0f
                }
                else {
                    if (this.scanner == null) {
                        this.scanner = PumpScanner(this, this.pos.down())
                    }
                    val scanned = this.scanner!!.scan()

                    if (scanned == 0) {
                        if ((stack >= 0) || (this.mode != PumpMode.BLOCKS)) {
                            val picked = this.scanner!!.peekBlocks() // onlyDrainable = (stack < 0))
                            var actuallyPicked = false
                            if (picked.isNotEmpty()) {
                                picked.forEach { pos, fluid ->
                                    val drained = fluid.drain(this.getWorld(), pos, false)
                                    if ((drained != null) && (this.tank.fill(drained, false) == drained.amount)) {
                                        this.tank.fill(fluid.drain(this.getWorld(), pos, true), true)

                                        val thing = if ((stack < 0) || (this.mode == PumpMode.FLUID)) {
                                            ItemStack.EMPTY
                                        } else {
                                            this.storage.getStackInSlot(stack)
                                        }
                                        if (thing.isEmpty || (thing.item !is ItemBlock)) {
                                            this.getWorld().setBlockToAir(pos)
                                        } else {
                                            val block = (thing.item as ItemBlock).block
                                            this.getWorld().setBlockState(pos, block.getStateFromMeta(thing.metadata))
                                            this.storage.setStackInSlot(stack, thing.copy().also { it.shrink(1) })
                                        }

                                        this.scanner!!.removeBlock(pos)
                                        actuallyPicked = true
                                    }
                                }
                                if (actuallyPicked) return 1.0f
                            }
                        }
                    }
                    else {
                        return .75f
                    }
                }
            }
        }

        return 0.0f
    }

    companion object {
        const val SYNC_SCANNER = "scanner"
        const val SYNC_SCANNER_INFO = "scanner_info"
        const val SYNC_PUMP_MODE = "pump_mode"
    }
}
