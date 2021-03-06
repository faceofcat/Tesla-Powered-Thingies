package net.ndrei.teslapoweredthingies.machines.poweredkiln

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.fml.common.discovery.ASMDataTable
import net.minecraftforge.registries.IForgeRegistry
import net.ndrei.teslacorelib.annotations.RegistryHandler
import net.ndrei.teslacorelib.config.readItemStacks
import net.ndrei.teslacorelib.utils.copyWithSize
import net.ndrei.teslacorelib.utils.equalsIgnoreSize
import net.ndrei.teslapoweredthingies.api.PoweredThingiesAPI
import net.ndrei.teslapoweredthingies.api.poweredkiln.IPoweredKilnRegistry
import net.ndrei.teslapoweredthingies.common.BaseTeslaRegistry
import net.ndrei.teslapoweredthingies.config.readExtraRecipesFile

@RegistryHandler
object PoweredKilnRegistry
    : BaseTeslaRegistry<PoweredKilnRecipe>("powered_kiln_recipes", PoweredKilnRecipe::class.java)
    , IPoweredKilnRegistry<PoweredKilnRecipe> {

    override fun construct(asm: ASMDataTable) {
        super.construct(asm)
        PoweredThingiesAPI.poweredKilnRegistry = this
    }

    override fun registerRecipes(asm: ASMDataTable, registry: IForgeRegistry<IRecipe>) {
        readExtraRecipesFile(PoweredKilnBlock.registryName!!.path) { json ->
            val input = json.readItemStacks("input_stack")
            if (input.isNotEmpty()) {
                val output = json.readItemStacks("output_stack").firstOrNull()
                if (output != null) {
                    input.forEach {
                        this.addRecipe(PoweredKilnRecipe(it, output))
                    }
                }
            }
        }

        this.registrationCompleted()
    }

    fun getRecipes(includeFurnace: Boolean = true): List<PoweredKilnRecipe> {
        val recipes = mutableListOf<PoweredKilnRecipe>()
        recipes.addAll(this.getAllRecipes())

        if (includeFurnace) {
            FurnaceRecipes.instance().smeltingList
                .mapTo(recipes) {
                    PoweredKilnRecipe(it.key, it.value)
                }
        }

        return recipes.toList()
    }

    override fun findRecipe(input: ItemStack) =
        this.findRecipe { it.input.equalsIgnoreSize(input) } ?:
            FurnaceRecipes.instance().getSmeltingResult(input).let {
                if (it.isEmpty) null
                else PoweredKilnRecipe(input.copyWithSize(1), it)
            }

    override fun hasRecipe(stack: ItemStack) = (this.findRecipe(stack) != null)
}
