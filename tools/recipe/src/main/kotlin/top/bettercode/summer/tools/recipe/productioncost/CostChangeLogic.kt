package top.bettercode.summer.tools.recipe.productioncost

/**
 * 制造费用增减逻辑:
 *
 * 物料/物料水分产品含量 value
 *
 * 制造费用指标费用 itemValue
 *
 * 计算公式：itemValue*(1+(value-exceedValue)/eachValue*changeValue)
 *
 */
data class CostChangeLogic @JvmOverloads constructor(
        /** 增减类型 */
        val type: ChangeLogicType,
        /** 结果每变化值（%）  */
        val changeValue: Double,
        /** 原料ID  */
        val materialId: String? = null,
        /** 超过条件值（公斤）  */
        val exceedValue: Double? = null,
        /** 条件每变化值（公斤）  */
        val eachValue: Double? = null,
        /**
         * 制造费用指标
         */
        val changeItems: List<ChangeItem>? = null
        )
