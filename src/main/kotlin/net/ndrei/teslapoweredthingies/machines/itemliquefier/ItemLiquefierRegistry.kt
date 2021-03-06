package net.ndrei.teslapoweredthingies.machines.itemliquefier

import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.discovery.ASMDataTable
import net.minecraftforge.registries.IForgeRegistry
import net.ndrei.teslacorelib.annotations.RegistryHandler
import net.ndrei.teslacorelib.config.readFluidStack
import net.ndrei.teslacorelib.config.readItemStacks
import net.ndrei.teslacorelib.utils.equalsIgnoreSize
import net.ndrei.teslapoweredthingies.api.PoweredThingiesAPI
import net.ndrei.teslapoweredthingies.api.itemliquefier.IItemLiquefierRegistry
import net.ndrei.teslapoweredthingies.common.BaseTeslaRegistry
import net.ndrei.teslapoweredthingies.config.readExtraRecipesFile
import net.ndrei.teslapoweredthingies.fluids.MoltenTeslaFluid
import net.ndrei.teslapoweredthingies.items.TeslaPlantSeeds

@RegistryHandler
object ItemLiquefierRegistry
    : BaseTeslaRegistry<ItemLiquefierRecipe>("item_liquefier_recipes", ItemLiquefierRecipe::class.java)
    , IItemLiquefierRegistry<ItemLiquefierRecipe> {

    val VANILLA_STONE_TO_LAVA_RATE = 100

    override fun construct(asm: ASMDataTable) {
        super.construct(asm)
        PoweredThingiesAPI.itemLiquefierRegistry = this
    }

    override fun registerRecipes(asm: ASMDataTable, registry: IForgeRegistry<IRecipe>) {
        this.registerForcedRecipes()

        readExtraRecipesFile(ItemLiquefierBlock.registryName!!.path) { json ->
            val input = json.readItemStacks("input_stack")
            if (input.isNotEmpty()) {
                val output = json.readFluidStack("output_fluid") ?: return@readExtraRecipesFile

                input.forEach {
                    this.addRecipe(ItemLiquefierRecipe(it, output))
                }
            }
        }

        this.registrationCompleted()
    }

    private fun registerForcedRecipes() {
        // TODO: move these to the json thing

        // vanilla recipes
        for (b in arrayOf(Blocks.COBBLESTONE, Blocks.STONE, Blocks.STONEBRICK, Blocks.MOSSY_COBBLESTONE, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_STAIRS, Blocks.BRICK_BLOCK, Blocks.BRICK_STAIRS)) {
            this.addRecipe(ItemLiquefierRecipe(b, FluidRegistry.LAVA, VANILLA_STONE_TO_LAVA_RATE))
        }

        for (b in arrayOf(Blocks.NETHERRACK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART_BLOCK, Blocks.RED_NETHER_BRICK)) {
            this.addRecipe(ItemLiquefierRecipe(b, FluidRegistry.LAVA, VANILLA_STONE_TO_LAVA_RATE * 2))
        }

        for (b in arrayOf(Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.FURNACE, Blocks.OBSIDIAN)) {
            this.addRecipe(ItemLiquefierRecipe(b, FluidRegistry.LAVA, VANILLA_STONE_TO_LAVA_RATE * 4))
        }

        this.addRecipe(ItemLiquefierRecipe(Items.APPLE, 1, FluidRegistry.WATER, 50))
        this.addRecipe(ItemLiquefierRecipe(Items.POTATO, 1, FluidRegistry.WATER, 50))

        // tesla thingies recipes
        this.addRecipe(ItemLiquefierRecipe(TeslaPlantSeeds, 1, MoltenTeslaFluid, 25))
    }

    override fun getRecipe(input: ItemStack)= this.findRecipe {
        it.input.equalsIgnoreSize(input) && (it.input.count <= input.count)
    }
}
