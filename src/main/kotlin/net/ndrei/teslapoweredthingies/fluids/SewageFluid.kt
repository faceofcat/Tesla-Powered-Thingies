package net.ndrei.teslapoweredthingies.fluids

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.ndrei.teslacorelib.annotations.AutoRegisterFluid
import net.ndrei.teslapoweredthingies.TeslaThingiesMod

/**
 * Created by CF on 2017-07-07.
 */
@AutoRegisterFluid
object SewageFluid
    : Fluid("tf-sewage"
        , ResourceLocation(TeslaThingiesMod.MODID, "blocks/sewage_still")
        , ResourceLocation(TeslaThingiesMod.MODID, "blocks/sewage_flow")) {

    init {
        super.setViscosity(10000)
        super.setDensity(50000)
    }

    override fun getUnlocalizedName(): String {
        return "fluid.${TeslaThingiesMod.MODID}.${this.unlocalizedName}"
    }
}
