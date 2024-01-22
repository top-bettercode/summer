package top.bettercode.summer.tools.recipe.packaging

import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial

/**
 * 包装费
 * @author Peter Wu
 */
class PackagingFee(
        /**
         * 包装耗材
         */
        val materials: List<RecipeOtherMaterial>,
) {

    val fee: Double = materials.sumOf {
        it.price * it.materialQuantity
    }
}