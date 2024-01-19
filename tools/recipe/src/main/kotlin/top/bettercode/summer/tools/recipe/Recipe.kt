package top.bettercode.summer.tools.recipe

import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue

/**
 * 配方
 *
 * @author Peter Wu
 */
data class Recipe(
        /**
         * 配方要求
         */
        val requirement: RecipeRequirement,
        val cost: Double,
        /** 选用的原料  */
        val materials: MutableList<RecipeMaterialValue> = ArrayList()
) {
    /** 需要烘干的水分含量  */
    val dryWater: Double
        get() = (materials.sumOf { it.solutionValue.value } - requirement.targetWeight).scale()


    /** 配方成本  */
    val trueCost: Double
        get() = materials.sumOf { it.solutionValue.value * it.price / 1000 }.scale()

    // --------------------------------------------
    fun addMaterial(material: RecipeMaterialValue) {
        materials.add(material)
    }

    //检查结果
    fun check(): Boolean {
        //约束检查
        //混用检查
        //进料口检查
        return true
    }

}
