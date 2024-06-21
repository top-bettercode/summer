package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.CarrierValue
import top.bettercode.summer.tools.recipe.RecipeUtil
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.result.IllegalRecipeException
import java.math.BigDecimal

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
        val names = mutableListOf<String>()
        val itValues = mutableListOf<Number>()
        val compares = mutableListOf<Boolean>()
        val otherValues = mutableListOf<Number>()
        val diffValues = mutableListOf<Number>()
        names.add("能耗费用原料数量")
        itValues.add(materialItems.size)
        compares.add(materialItems.size == other.materialItems.size)
        otherValues.add(other.materialItems.size)
        diffValues.add(materialItems.size - other.materialItems.size)

        materialItems.forEach {
            val thisVal = (it.it.cost * it.value).scale()
            val oth = otherMaterialItemsMap[it.it.id]
            val otherVal = if (oth == null) 0.0 else (oth.it.cost * oth.value).scale()
            names.add(it.it.name)
            itValues.add(thisVal)
            compares.add(thisVal - otherVal in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
            otherValues.add(otherVal)
            diffValues.add((thisVal - otherVal).scale())
        }
        if (materialItems.size < other.materialItems.size) {
            other.materialItems.filter { m -> !materialItems.any { m.it.id == it.it.id } }.forEach {
                val otherVal = (it.it.cost * it.value).scale()
                names.add(it.it.name)
                itValues.add(0.0)
                compares.add(-otherVal in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
                otherValues.add(otherVal)
                diffValues.add(-otherVal)
            }
        }

        names.add("总能耗费用")
        itValues.add(energyFee)
        compares.add(this.energyFee - other.energyFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
        otherValues.add(other.energyFee)
        diffValues.add((energyFee - other.energyFee).scale())

        names.add("其他固定费用数量")
        itValues.add(dictItems.size)
        compares.add(dictItems.size == other.dictItems.size)
        otherValues.add(other.dictItems.size)
        diffValues.add(dictItems.size - other.dictItems.size)

        dictItems.forEach { (key, value) ->
            val thisVal = (value.it.cost * value.value).scale()
            val oth = other.dictItems[key]
            val otherVal = if (oth == null) 0.0 else (oth.it.cost * oth.value).scale()

            names.add(key.dictName)
            itValues.add(thisVal)
            compares.add(thisVal - otherVal in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
            otherValues.add(otherVal)
            diffValues.add((thisVal - otherVal).scale())
        }
        if (dictItems.size < other.dictItems.size) {
            other.dictItems.filter { m -> !dictItems.any { m.key == it.key } }.forEach {
                val otherVal = (it.value.it.cost * it.value.value).scale()
                names.add(it.key.dictName)
                itValues.add(0.0)
                compares.add(-otherVal in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
                otherValues.add(otherVal)
                diffValues.add(-otherVal)
            }
        }

        names.add("人工+折旧费+其他费用")
        itValues.add(otherFee)
        compares.add(this.otherFee - other.otherFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
        otherValues.add(other.otherFee)
        diffValues.add((otherFee - other.otherFee).scale())

        names.add("税费")
        itValues.add(taxFee)
        compares.add(this.taxFee - other.taxFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
        otherValues.add(other.taxFee)
        diffValues.add((taxFee - other.taxFee).scale())

        names.add("制造费用合计")
        itValues.add(totalFee)
        compares.add(this.totalFee - other.totalFee in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON)
        otherValues.add(other.totalFee)
        diffValues.add((totalFee - other.totalFee).scale())


        // 计算每一列的最大宽度
        val nameWidth = names.maxOf { it.length }
        val thisStrValues =
            itValues.map { BigDecimal(it.toString()).stripTrailingZeros().toPlainString() }
        val otherStrValues =
            otherValues.map { BigDecimal(it.toString()).stripTrailingZeros().toPlainString() }
        val diffStrValues =
            diffValues.map { BigDecimal(it.toString()).stripTrailingZeros().toPlainString() }

        val itValueWidth = thisStrValues.maxOf { it.length }
        val otherValueWidth = otherStrValues.maxOf { it.length }
        val diffValueWidth = diffStrValues.maxOf { it.length }

        val result = StringBuilder()
        // 打印表头
        val compareWidth = "比较".length
        result.appendLine(
            "${"原料名称".padEnd(nameWidth)} | ${"this".padStart(itValueWidth)} | ${
                "比较".padEnd(
                    compareWidth
                )
            } | ${"other".padEnd(otherValueWidth)} | ${"差值".padStart(diffValueWidth)}"
        )

        // 打印数据行
        for (i in names.indices) {
            val name = names[i].padEnd(nameWidth)
            val compare = (if (compares[i]) "==" else "!=").padEnd(compareWidth)
            val itValue = thisStrValues[i].padStart(itValueWidth)
            val otherValue = otherStrValues[i].padEnd(otherValueWidth)
            val diffValue = diffStrValues[i].padStart(diffValueWidth)
            result.appendLine("$name | $itValue | $compare | $otherValue | $diffValue")
        }

        val diff = !compares.all { it }
        if (diff) {
            throw IllegalRecipeException("制造成本计算结果不一致\n$result")
        } else if (log.isDebugEnabled) {
            log.debug("制造成本计算结果一致\n$result")
        }
    }

}