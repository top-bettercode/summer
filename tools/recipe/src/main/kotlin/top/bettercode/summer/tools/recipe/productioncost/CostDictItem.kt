package top.bettercode.summer.tools.recipe.productioncost

/** 制造费用项-字典类 */
data class CostDictItem(
        /** ID  */
        val id: String,

        /** 数量  */
        val value: Double,

        /** 单价  */
        val price: Double
)
