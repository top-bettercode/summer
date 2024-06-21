package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.CarrierValue
import top.bettercode.summer.tools.recipe.RecipeUtil
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class ProductionCostValue(
        /**
         * 能耗费用
         */
        @JsonProperty("materialItems")
        val materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>,
        /**
         * 其他固定费用
         */
        @JsonProperty("dictItems")
        val dictItems: Map<DictType, CarrierValue<Cost, Double>>,
        /**
         * 人工+折旧费+其他费用
         *
         */
        @JsonProperty("otherFee")
        val otherFee: Double,
        /**
         * 能耗费用
         */
        @JsonProperty("energyFee")
        val energyFee: Double,
        /**
         *  税费 =（人工+折旧费+其他费用）*0.09+15
         *
         */
        @JsonProperty("taxFee")
        val taxFee: Double,
        /**
         *  制造费用合计=人工费+折旧费+其他费用+能耗费+税费
         *
         */
        @JsonProperty("totalFee")
        val totalFee: Double,
        /**
         * 制造费用增减
         */
        @JsonProperty("allChange")
        val allChange: Double
) {
    private val log = LoggerFactory.getLogger(ProductionCostValue::class.java)

    fun compareTo(other: ProductionCostValue) {
        val otherMaterialItemsMap = other.materialItems.associateBy { it.it.id }
        materialItems.forEach {
            val thisVal = (it.it.cost * it.value).scale()
            val oth = otherMaterialItemsMap[it.it.id]
            val otherVal = if (oth == null) 0.0 else (oth.it.cost * oth.value).scale()
            Assert.isTrue(thisVal - otherVal in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON, "${it.it.name}不一致:${thisVal}!=${otherVal}, 差值：${(thisVal - otherVal).scale().toBigDecimal().toPlainString()}")
        }
        Assert.isTrue(this.energyFee - other.energyFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON, "总能耗费用不一致:${this.energyFee}!=${other.energyFee},差值：${(this.energyFee - other.energyFee).scale().toBigDecimal().toPlainString()}")
        dictItems.forEach { (key, value) ->
            val thisVal = (value.it.cost * value.value).scale()
            val oth = other.dictItems[key]
            val otherVal = if (oth == null) 0.0 else (oth.it.cost * oth.value).scale()
            Assert.isTrue(thisVal - otherVal in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON, "${key.dictName}不一致:${thisVal}!=${otherVal}, 差值：${(thisVal - otherVal).scale().toBigDecimal().toPlainString()}")
        }
        Assert.isTrue(this.otherFee - other.otherFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON, "人工+折旧费+其他费用不一致:${this.otherFee}!=${other.otherFee}, 差值：${(this.otherFee - other.otherFee).scale().toBigDecimal().toPlainString()}")

        Assert.isTrue(this.taxFee - other.taxFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON, "税费不一致:${this.taxFee}!=${other.taxFee}, 差值：${(this.taxFee - other.taxFee).scale().toBigDecimal().toPlainString()}")

        Assert.isTrue(this.totalFee - other.totalFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON, "制造费用合计不一致:${this.totalFee}!=${other.totalFee}, 差值：${(this.totalFee - other.totalFee).scale().toBigDecimal().toPlainString()}")
    }

}