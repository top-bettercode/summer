package top.bettercode.summer.tools.recipe.productioncost

import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue

/**
 * 制造费用
 *
 * 制造费用合计=人工费+能耗费+折旧费+其他费用+税费
 * 税费=（人工+折旧费+其他费用）*0.09+15
 * 能耗费=煤/生物质耗费+电耗+蒸汽耗+天然气耗+∑其他1~4（若有）
 * 原型增加税率和15
 *
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