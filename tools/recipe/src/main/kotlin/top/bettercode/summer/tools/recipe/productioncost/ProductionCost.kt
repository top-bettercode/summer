package top.bettercode.summer.tools.recipe.productioncost

import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue

/**
 * 制造费用
 * @author Peter Wu
 */
data class ProductionCost(
        /**
         * 投入物料
         */
        val materials: List<RecipeMaterialValue>,
        /**
         * 制造费用项-物料类
         */
        val materialItems: List<CostMaterialItem>,
        /**
         * 制造费用项-字典类
         */
        val dictItems: List<CostDictItem>,
        /**
         * 费用增减逻辑
         */
        val change: List<CostChangeLogic>
) {

    val fee: Double by lazy {


        0.0
    }

}