package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.StringUtil.toFullWidth
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.CarrierValue
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.result.IllegalRecipeException
import top.bettercode.summer.tools.recipe.result.RecipeColumns

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
    val allChange: Double,
    /**
     * 小数位数
     */
    @JsonProperty("scale")
    val scale: Int,
    /**
     * 误差
     */
    @JsonProperty("minEpsilon")
    val minEpsilon: Double
) {
    private val log = LoggerFactory.getLogger(ProductionCostValue::class.java)

    fun compareTo(other: ProductionCostValue) {
        val names = RecipeColumns(scale)
        val itValues = RecipeColumns(scale)
        val compares = RecipeColumns(scale)
        val otherValues = RecipeColumns(scale)
        val diffValues = RecipeColumns(scale)
        names.add("制造费用项")
        itValues.add("this")
        compares.add("=")
        otherValues.add("other")
        diffValues.add("diff")

        val separatorIndexs = compareTo(other, names, itValues, compares, otherValues, diffValues)

        // 计算每一列的最大宽度
        val nameWidth = names.width
        val itValueWidth = itValues.width
        val compareWidth = compares.width
        val otherValueWidth = otherValues.width
        val diffValueWidth = diffValues.width

        separatorIndexs.forEachIndexed { index, i ->
            val index1 = index + i
            names.add(index1, "".padEnd(nameWidth, '-'))
            itValues.add(index1, "".padEnd(itValueWidth, '-'))
            compares.add(index1, "".padEnd(compareWidth, '-'))
            otherValues.add(index1, "".padEnd(otherValueWidth, '-'))
            diffValues.add(index1, "".padEnd(diffValueWidth, '-'))
        }

        val result = StringBuilder()

        for (i in names.indices) {
            val name = names[i].toFullWidth().padEnd(nameWidth, '\u3000')
            val itValue = itValues[i].padStart(itValueWidth)
            val compare = compares[i].padEnd(compareWidth)
            val otherValue = otherValues[i].padEnd(otherValueWidth)
            val diffValue = diffValues[i].padStart(diffValueWidth)
            result.appendLine("$name | $itValue | $compare | $otherValue | $diffValue")
        }

        if (compares.isDiff) {
            throw IllegalRecipeException("制造成本计算结果不一致\n$result")
        } else if (log.isDebugEnabled) {
            log.debug("制造成本计算结果一致")
        }
    }

    fun compareTo(
        other: ProductionCostValue,
        names: RecipeColumns,
        itValues: RecipeColumns,
        compares: RecipeColumns,
        otherValues: RecipeColumns,
        diffValues: RecipeColumns
    ): List<Int> {
        val separatorIndexs = mutableListOf<Int>()
        separatorIndexs.add(names.size)
        names.add("能耗费用原料数量")
        itValues.add(materialItems.size)
        compares.add(materialItems.size == other.materialItems.size)
        otherValues.add(other.materialItems.size)
        diffValues.add(materialItems.size - other.materialItems.size)
        separatorIndexs.add(names.size)

        val otherMaterialItemsMap = other.materialItems.associateBy { it.it.id }
        materialItems.forEach {
            val thisVal = (it.it.cost * it.value)
            val oth = otherMaterialItemsMap[it.it.id]
            val otherVal = if (oth == null) 0.0 else (oth.it.cost * oth.value)
            names.add("${it.it.name} (${(it.value * 100).scale(2)}%)")
            itValues.add(thisVal)
            compares.add((thisVal - otherVal).scale(scale) in -minEpsilon..minEpsilon)
            otherValues.add(otherVal)
            diffValues.add((thisVal - otherVal))
        }
        other.materialItems.filter { m -> !materialItems.any { m.it.id == it.it.id } }.forEach {
            val otherVal = (it.it.cost * it.value)
            names.add("${it.it.name} (${(it.value * 100).scale(2)}%)")
            itValues.add(0.0)
            compares.add(-otherVal.scale(scale) in -minEpsilon..minEpsilon)
            otherValues.add(otherVal)
            diffValues.add(-otherVal)
        }
        separatorIndexs.add(names.size)

        names.add("总能耗费用")
        itValues.add(energyFee)
        compares.add((this.energyFee - other.energyFee).scale(scale) in -minEpsilon..minEpsilon)
        otherValues.add(other.energyFee)
        diffValues.add((energyFee - other.energyFee))
        separatorIndexs.add(names.size)

        names.add("其他固定费用数量")
        itValues.add(dictItems.size)
        compares.add(dictItems.size == other.dictItems.size)
        otherValues.add(other.dictItems.size)
        diffValues.add(dictItems.size - other.dictItems.size)
        separatorIndexs.add(names.size)

        dictItems.forEach { (key, value) ->
            val thisVal = (value.it.cost * value.value)
            val oth = other.dictItems[key]
            val otherVal = if (oth == null) 0.0 else (oth.it.cost * oth.value)

            names.add("${key.dictName} (${(value.value * 100).scale(2)}%)")
            itValues.add(thisVal)
            compares.add((thisVal - otherVal).scale(scale) in -minEpsilon..minEpsilon)
            otherValues.add(otherVal)
            diffValues.add((thisVal - otherVal))
        }
        other.dictItems.filter { m -> !dictItems.any { m.key == it.key } }.forEach {
            val otherVal = (it.value.it.cost * it.value.value)
            names.add("${it.key.dictName} (${(it.value.value * 100).scale(2)}%)")
            itValues.add(0.0)
            compares.add(-otherVal.scale(scale) in -minEpsilon..minEpsilon)
            otherValues.add(otherVal)
            diffValues.add(-otherVal)
        }
        separatorIndexs.add(names.size)

        names.add("人工+折旧费+其他费用")
        itValues.add(otherFee)
        compares.add((this.otherFee - other.otherFee).scale(scale) in -minEpsilon..minEpsilon)
        otherValues.add(other.otherFee)
        diffValues.add((otherFee - other.otherFee))
        separatorIndexs.add(names.size)

        names.add("税费")
        itValues.add(taxFee)
        compares.add((this.taxFee - other.taxFee).scale(scale) in -minEpsilon..minEpsilon)
        otherValues.add(other.taxFee)
        diffValues.add((taxFee - other.taxFee))
        separatorIndexs.add(names.size)

        names.add("制造费用合计 (${(allChange * 100).scale(2)}%)")
        itValues.add(totalFee)
        compares.add((this.totalFee - other.totalFee).scale(scale) in -minEpsilon..minEpsilon)
        otherValues.add(other.totalFee)
        diffValues.add((totalFee - other.totalFee))
        separatorIndexs.add(names.size)
        return separatorIndexs
    }

    fun toString(names: RecipeColumns, itValues: RecipeColumns): MutableList<Int> {
        val separatorIndexs = mutableListOf<Int>()
        separatorIndexs.add(names.size)
        names.add("能耗费用原料数量")
        itValues.add(materialItems.size)
        separatorIndexs.add(names.size)

        materialItems.forEach {
            val thisVal = (it.it.cost * it.value)
            names.add("${it.it.name} (${(it.value * 100).scale(2)}%)")
            itValues.add(thisVal)
        }
        separatorIndexs.add(names.size)

        names.add("总能耗费用")
        itValues.add(energyFee)
        separatorIndexs.add(names.size)

        names.add("其他固定费用数量")
        itValues.add(dictItems.size)
        separatorIndexs.add(names.size)

        dictItems.forEach { (key, value) ->
            val thisVal = (value.it.cost * value.value)
            names.add("${key.dictName} (${(value.value * 100).scale(2)}%)")
            itValues.add(thisVal)
        }
        separatorIndexs.add(names.size)

        names.add("人工+折旧费+其他费用")
        itValues.add(otherFee)
        separatorIndexs.add(names.size)

        names.add("税费")
        itValues.add(taxFee)
        separatorIndexs.add(names.size)

        names.add("制造费用合计 (${(allChange * 100).scale(2)}%)")
        itValues.add(totalFee)
        separatorIndexs.add(names.size)
        return separatorIndexs
    }

    override fun toString(): String {
        val names = RecipeColumns(scale)
        val itValues = RecipeColumns(scale)
        names.add("制造费用项")
        itValues.add("this")

        val separatorIndexs = toString(
            names,
            itValues
        )

        // 计算每一列的最大宽度
        val nameWidth = names.width
        val itValueWidth = itValues.width

        separatorIndexs.forEachIndexed { index, i ->
            val index1 = index + i
            names.add(index1, "".padEnd(nameWidth, '-'))
            itValues.add(index1, "".padEnd(itValueWidth, '-'))
        }

        val result = StringBuilder()

        for (i in names.indices) {
            val name = names[i].toFullWidth().padEnd(nameWidth, '\u3000')
            val itValue = itValues[i].padStart(itValueWidth)
            result.appendLine("$name | $itValue")
        }

        return result.toString()
    }


}