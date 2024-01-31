package top.bettercode.summer.tools.recipe.productioncost

/** 制造费用项-物料类 */
data class CostMaterialItem(
        /** 物料品类ID  */
        val categoryId: String,
        /** 物料ID  */
        val materialId: String,
        /** 数量  */
        val value: Double,
        /** 单价  */
        val price: Double
)