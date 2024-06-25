package top.bettercode.summer.tools.recipe

import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.productioncost.Cost
import top.bettercode.summer.tools.recipe.productioncost.DictType
import top.bettercode.summer.tools.recipe.result.Recipe

/**
 *
 * @author Peter Wu
 */
data class PrepareData(
    val defaultRecipeName: String,
    val requirement: RecipeRequirement,
    val includeProductionCost: Boolean,
    val minMaterialNum: Boolean,
    val recipeMaterials: Map<String, RecipeMaterialVar>,
    val objective: IVar,
    val materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>?,
    val dictItems: Map<DictType, CarrierValue<Cost, IVar>>?,
) {

    @JvmOverloads
    fun toRecipe(recipeName: String? = null): Recipe {
        val materials = this.recipeMaterials.mapNotNull { (_, u) ->
            val value = u.weight.value
            if (value != 0.0) {
                u.toMaterialValue()
            } else {
                null
            }
        }
        val value = this.objective.value
        return Recipe(
            recipeName = recipeName ?: defaultRecipeName,
            requirement = requirement,
            includeProductionCost = includeProductionCost,
            optimalProductionCost = requirement.productionCost.computeFee(
                this.materialItems?.map { CarrierValue(it.it, it.value.value) },
                this.dictItems?.mapValues { CarrierValue(it.value.it, it.value.value.value) }),
            cost = if (minMaterialNum) value - materials.size else value,
            materials = materials
        )
    }

}