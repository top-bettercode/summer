package top.bettercode.summer.tools.recipe

import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.productioncost.Cost
import top.bettercode.summer.tools.recipe.productioncost.DictType

/**
 *
 * @author Peter Wu
 */
data class PrepareData(
        val recipeMaterials: Map<String, RecipeMaterialVar>,
        val objective: IVar,
        val others: List<IVar>,
        val materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>?,
        val dictItems: Map<DictType, CarrierValue<Cost, IVar>>?,
)