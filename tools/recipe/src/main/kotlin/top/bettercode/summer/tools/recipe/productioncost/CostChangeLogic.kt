package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

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
@JsonPropertyOrder(alphabetic = true)
data class CostChangeLogic(
        /** 增减类型 */
        @JsonProperty("type")
        val type: ChangeLogicType,
        /** 结果每变化值（%）  */
        @JsonProperty("changeValue")
        val changeValue: Double,
        /** 原料ID  */
        @JsonProperty("materialId")
        val materialId: List<String>? = null,
        /** 超过条件值（公斤）  */
        @JsonProperty("exceedValue")
        val exceedValue: Double? = null,
        /** 条件每变化值（公斤）  */
        @JsonProperty("eachValue")
        val eachValue: Double? = null,
        /**
         * 制造费用指标
         */
        @JsonProperty("changeItems")
        val changeItems: List<ChangeItem>? = null
)
