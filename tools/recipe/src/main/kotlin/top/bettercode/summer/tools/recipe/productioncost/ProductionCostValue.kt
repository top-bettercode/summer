package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import top.bettercode.summer.tools.optimal.solver.OptimalUtil
import top.bettercode.summer.tools.recipe.CarrierValue
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
        val materialItems: List<CarrierValue<RecipeOtherMaterial, Double>>,
        /**
         * 其他固定费用
         */
        val dictItems: Map<DictType, CarrierValue<Cost, Double>>,
        /**
         * 人工+折旧费+其他费用
         *
         */
        val otherFee: Double,
        /**
         * 能耗费用
         */
        val energyFee: Double,
        /**
         *  税费 =（人工+折旧费+其他费用）*0.09+15
         *
         */
        val taxFee: Double,
        /**
         *  制造费用合计=人工费+折旧费+其他费用+能耗费+税费
         *
         */
        val totalFee: Double,
        /**
         * 制造费用增减
         */
        val allChange: Double
) {
    private val log = LoggerFactory.getLogger(ProductionCostValue::class.java)

    fun validate(productionCost: ProductionCostValue) {
        val otherMaterialItemsMap = productionCost.materialItems.associateBy { it.it.id }
        materialItems.forEach {
            val thisVal = it.it.price * it.it.value * it.value
            val other = otherMaterialItemsMap[it.it.id]
            val otherVal = if (other == null) 0.0 else other.it.price * other.it.value * other.value
            Assert.isTrue(thisVal - otherVal in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON, "${it.it.name}不一致:${thisVal}!=${otherVal}, 差值：${thisVal - otherVal}")
        }
        Assert.isTrue(this.energyFee - productionCost.energyFee in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON, "总能耗费用不一致:${this.energyFee}!=${productionCost.energyFee},差值：${this.energyFee - productionCost.energyFee}")
        dictItems.forEach { (key, value) ->
            val thisVal = value.it.price * value.it.value * value.value
            val other = productionCost.dictItems[key]
            val otherVal = if (other == null) 0.0 else other.it.price * other.it.value * other.value
            Assert.isTrue(thisVal - otherVal in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON, "${key.remark}不一致:${thisVal}!=${otherVal}, 差值：${thisVal - otherVal}")
        }
        Assert.isTrue(this.otherFee - productionCost.otherFee in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON, "人工+折旧费+其他费用不一致:${this.otherFee}!=${productionCost.otherFee}, 差值：${this.otherFee - productionCost.otherFee}")

        Assert.isTrue(this.taxFee - productionCost.taxFee in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON, "税费不一致:${this.taxFee}!=${productionCost.taxFee}, 差值：${this.taxFee - productionCost.taxFee}")

        Assert.isTrue(this.totalFee - productionCost.totalFee in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON, "制造费用合计不一致:${this.totalFee}!=${productionCost.totalFee}, 差值：${this.totalFee - productionCost.totalFee}")
    }

}