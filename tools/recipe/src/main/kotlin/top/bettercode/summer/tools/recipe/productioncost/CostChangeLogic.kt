package top.bettercode.summer.tools.recipe.productioncost

/** 制造费用增减逻辑 */
data class CostChangeLogic(
        /** 增减类型 */
        val type: ChangeLogicType,
        /** 物料ID  */
        val materialId: String,
        /** 超过条件值（公斤）  */
        val whenExceedValue: Double,
        /** 条件每变化类型  */
        val whenEachType: EachChangeType,
        /** 条件每变化值（公斤）  */
        val whenEachValue: Double,
        val thenItems: List<ThenItem>,
        /** 结果每变化类型  */
        val thenEachType: EachChangeType,
        /** 结果每变化值（%）  */
        val thenEachValue: Double,
)
