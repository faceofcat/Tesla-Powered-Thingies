package net.ndrei.teslapoweredthingies.machines

import net.minecraft.block.material.Material
import net.ndrei.teslacorelib.blocks.OrientedBlock
import net.ndrei.teslacorelib.tileentities.SidedTileEntity
import net.ndrei.teslapoweredthingies.TeslaThingiesMod

/**
 * Created by CF on 2017-06-30.
 */
open class BaseThingyBlock<T : SidedTileEntity> : OrientedBlock<T> {
    protected constructor(registryName: String, teClass: Class<T>)
            : super(TeslaThingiesMod.MODID, TeslaThingiesMod.creativeTab, registryName, teClass)

    protected constructor(registryName: String, teClass: Class<T>, material: Material)
            : super(TeslaThingiesMod.MODID, TeslaThingiesMod.creativeTab, registryName, teClass, material)
}
