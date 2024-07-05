package top.bettercode.summer.tools.recipe.result

import com.fasterxml.jackson.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.StringUtil.toFullWidth
import top.bettercode.summer.tools.optimal.Operator
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeCondition
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.criteria.TermThen
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicatorType
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.RelationMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.ReplacebleMaterialIDs
import top.bettercode.summer.tools.recipe.productioncost.ProductionCostValue
import java.io.File

/**
 * 配方
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
data class Recipe(
    /**
     * 配方名称
     */
    @JsonProperty("recipeName")
    val recipeName: String,
    /**
     * 配方要求
     */
    @JsonProperty("requirement")
    val requirement: RecipeRequirement,
    @JsonProperty("includeProductionCost")
    val includeProductionCost: Boolean,
    @JsonProperty("optimalProductionCost")
    val optimalProductionCost: ProductionCostValue?,
    /**
     * 总成本(制造费用+原料成本)
     */
    @JsonProperty("cost")
    val cost: Double,
    /** 选用的原料  */
    @JsonProperty("materials")
    val materials: List<RecipeMaterialValue>,
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
    private val log: Logger = LoggerFactory.getLogger(Recipe::class.java)

    @JsonIgnore
    val productionCost: ProductionCostValue =
        requirement.productionCost.computeFee(this)

    val packagingCost: Double = requirement.packagingMaterials.sumOf {
        it.price * it.value
    }

    /** 需要烘干的水分含量  */
    val dryWaterWeight: Double
        get() = (weight - requirement.targetWeight)

    /**
     * 物料水分重量
     */
    val waterWeight: Double
        get() = materials.sumOf { it.waterWeight }

    /**
     * 总养分
     */
    val totalNutrientWeight: Double
        get() = materials.sumOf { it.totalNutrientWeight }

    /**
     * 产出重量
     */
    val weight: Double
        get() = materials.sumOf { it.weight }

    /** 原料成本  */
    val materialCost: Double
        get() = materials.sumOf { it.weight * it.price }

    companion object {
        fun read(file: File): Recipe {
            val objectMapper =
                StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
            return objectMapper.readValue(file, Recipe::class.java)
        }
    }

    /**
     * 比较配方
     */
    fun compareTo(other: Recipe?) {
        Assert.notNull(other, "${requirement.productName}-other配方不能为空")
        val separatorIndexs = mutableListOf<Int>()
        val names = RecipeColumns(scale)
        val itValues = RecipeColumns(scale)
        val compares = RecipeColumns(scale)
        val otherValues = RecipeColumns(scale)
        val diffValues = RecipeColumns(scale)
        separatorIndexs.add(names.size)
        names.add("原料/制造费用")
        itValues.add(this.recipeName)
        compares.add("=")
        otherValues.add(other!!.recipeName)
        diffValues.add("diff")
        separatorIndexs.add(names.size)

        //总成本(制造费用+原料成本)
        names.add("总成本(制造费用+原料成本)")
        itValues.add(cost)
        compares.add((cost - other.cost).scale(scale) in -minEpsilon..minEpsilon)
        otherValues.add(other.cost)
        diffValues.add((cost - other.cost))
        separatorIndexs.add(names.size)
        //原料用量
        names.add("原料数量")
        itValues.add(materials.size)
        compares.add(materials.size == other.materials.size)
        otherValues.add(other.materials.size)
        diffValues.add(materials.size - other.materials.size)
        separatorIndexs.add(names.size)

        val otherMaterialsMap = other.materials.associateBy { it.id }
        materials.forEach { m ->
            val otherMaterialValue = otherMaterialsMap[m.id]
            val otherWeight = otherMaterialValue?.weight ?: 0.0
            names.add(m.name)
            itValues.add(m.weight)
            compares.add((m.weight - otherWeight).scale(scale) in -minEpsilon..minEpsilon)
            otherValues.add(otherWeight)
            diffValues.add((m.weight - otherWeight))
        }
        if (materials.size < other.materials.size) {
            other.materials.filter { m -> !materials.any { m.id == it.id } }.forEach {
                val otherWeight = it.weight
                names.add(it.name)
                itValues.add(0.0)
                compares.add(-otherWeight.scale(scale) in -minEpsilon..minEpsilon)
                otherValues.add(otherWeight)
                diffValues.add(-otherWeight)
            }
        }

        //制造费用
        if (optimalProductionCost != null) {
            Assert.notNull(
                other.optimalProductionCost,
                "${requirement.productName}-other配方制造费用为空"
            )
            val productionCostSeparatorIndexs = optimalProductionCost.compareTo(
                other.optimalProductionCost!!,
                names,
                itValues,
                compares,
                otherValues,
                diffValues
            )
            separatorIndexs.addAll(productionCostSeparatorIndexs)
        }

        // 计算每一列的最大宽度
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
            throw IllegalRecipeException("${requirement.productName}-配方不一致\n$result")
        } else if (log.isDebugEnabled) {
            log.debug("${requirement.productName}-配方一致\n$result")
        }
    }

    fun write(file: File) {
        val objectMapper =
            StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
        objectMapper.writeValue(file, this)
    }

    //检查结果
    fun validate(): Boolean {
        //检查进料口
        if (requirement.maxUseMaterialNum != null && materials.size > requirement.maxUseMaterialNum) {
            throw IllegalRecipeException("${requirement.productName}-配方所需进料口：${materials.size} 超过最大进料口：${requirement.maxUseMaterialNum}")
        }

        //检查烘干水分
        if (dryWaterWeight.scale(scale) < 0) {
            throw IllegalRecipeException("${requirement.productName}-配方烘干水分异常：${dryWaterWeight}")
        }
        if (requirement.maxBakeWeight != null && (dryWaterWeight - requirement.maxBakeWeight).scale(
                scale
            ) > 0
        ) {
            throw IllegalRecipeException(
                "${requirement.productName}-配方烘干水分:${dryWaterWeight} 超过最大可烘干水分：${requirement.maxBakeWeight} ,差值：${
                    (dryWaterWeight - requirement.maxBakeWeight).toBigDecimal()
                        .toPlainString()
                }"
            )
        }
        if ((dryWaterWeight - waterWeight).scale(scale) > 0) {
            throw IllegalRecipeException(
                "${requirement.productName}-配方烘干水分:${dryWaterWeight} 超过总水分：${waterWeight},差值：${
                    (dryWaterWeight - waterWeight).toBigDecimal().toPlainString()
                }"
            )
        }

        val targetWeight = requirement.targetWeight
        // 指标范围约束
        val rangeIndicators = requirement.indicatorRangeConstraints
        for (rangeIndicator in rangeIndicators) {
            val indicator = rangeIndicator.indicator
            val indicatorValue = when (indicator.type) {
                RecipeIndicatorType.TOTAL_NUTRIENT -> (totalNutrientWeight / targetWeight)
                RecipeIndicatorType.PRODUCT_WATER -> ((waterWeight - dryWaterWeight) / targetWeight)
                RecipeIndicatorType.RATE_TO_OTHER -> (materials.sumOf { it.indicatorWeight(indicator.itId!!) } / materials.sumOf {
                    it.indicatorWeight(
                        indicator.otherId!!
                    )
                })

                else -> (materials.sumOf { it.indicatorWeight(rangeIndicator.id) } / targetWeight)
            }
            // 如果 indicatorValue 不在value.min,value.max范围内，返回 false
            if (indicatorValue.scale(scale) !in rangeIndicator.scaledValue.min.scale(scale)..rangeIndicator.scaledValue.max.scale(
                    scale
                )
            ) {
                throw IllegalRecipeException("${requirement.productName}-指标:${indicator.name}：${indicatorValue} 不在范围${rangeIndicator.scaledValue.min}-${rangeIndicator.scaledValue.max}内")
            }
        }

        val materialRangeConstraints = requirement.materialRangeConstraints
        val mustUseMaterials =
            materialRangeConstraints.filter { it.then.min > 0 }.map { it.term }.flatten()

        // 指标原料约束
        val materialIDIndicators = requirement.indicatorMaterialIDConstraints
        for (materialIDIndicator in materialIDIndicators) {
            val indicator = materialIDIndicator.indicator
            val materialList =
                materials.filter { it.indicators.valueOf(materialIDIndicator.id) > 0.0 }
            if (materialList.isNotEmpty()) {
                val indicatorUsedMaterials =
                    materialList.map { it.id }.filter { !mustUseMaterials.contains(it) }
                if (!materialIDIndicator.value.containsAll(indicatorUsedMaterials)) {
                    throw IllegalRecipeException("${requirement.productName}-指标:${indicator.name}所用原料：${indicatorUsedMaterials} 不在范围${materialIDIndicator.value}内")
                }
            }
        }

        val usedMaterials = materials.map { it.id }
        // 保留用原料ID
        val keepMaterials = requirement.keepMaterialConstraints
        if (keepMaterials.ids.isNotEmpty()) {
            if (!keepMaterials.containsAll(usedMaterials)) {
                throw IllegalRecipeException("${requirement.productName}-配方所用原料：${usedMaterials} 不在范围${keepMaterials}内")
            }
        }
        // 不能用原料ID
        val noUseMaterials = requirement.noUseMaterialConstraints
        if (noUseMaterials.ids.isNotEmpty()) {
            if (usedMaterials.any { !requirement.materialMust.test(it) && noUseMaterials.contains(it) }) {
                throw IllegalRecipeException("${requirement.productName}-配方所用原料：${usedMaterials} 包含不可用原料${noUseMaterials}内")
            }
        }
        // 不能混用的原料,value: 原料ID
        val notMixMaterials = requirement.notMixMaterialConstraints
        for (notMixMaterial in notMixMaterials) {
            val mixMaterials =
                notMixMaterial.map { notMix -> usedMaterials.filter { notMix.contains(it) } }
                    .filter { it.isNotEmpty() }
            val size = mixMaterials.size
            if (size > 1) {
                throw IllegalRecipeException(
                    "${requirement.productName}-配方混用原料：${
                        mixMaterials.joinToString(
                            ",",
                            "[",
                            "]"
                        ) { it.joinToString("、") }
                    }"
                )
            }
        }

        // 原料约束,key:原料ID, value: 原料使用范围约束
        for ((ids, range) in materialRangeConstraints) {
            val weight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            if (weight.scale(scale) !in range.min..range.max) {
                throw IllegalRecipeException(
                    "${requirement.productName}-原料${
                        ids.toNames(requirement)
                    }使用量：${
                        weight.toBigDecimal().toPlainString()
                    } 不在范围${range.min}-${range.max}内"
                )
            }
        }
        // 指定原料约束
        val materialIDConstraints = requirement.materialIDConstraints
        for ((ids, value) in materialIDConstraints) {
            for (idd in ids) {
                if (usedMaterials.contains(idd) && !value.contains(idd)) {
                    throw IllegalRecipeException(
                        "${requirement.productName}-${
                            ids.toNames(
                                requirement
                            )
                        }使用了不在指定原料${value.toNames(requirement)}范围内的原料：${
                            arrayOf(idd).toMaterialIDs().toNames(requirement)
                        }"
                    )
                }
            }
        }
        // 关联原料约束
        val materialRelationConstraints = requirement.materialRelationConstraints
        for (termThen in materialRelationConstraints) {
            val ids = termThen.term
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val usedWeight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            val usedNormalWeight =
                materials.filter { ids.contains(it.id) }.sumOf { it.normalWeight }
            val usedOverdoseWeight =
                materials.filter { ids.contains(it.id) }.sumOf { it.overdoseWeight }
            val usedAddWeight = (usedNormalWeight + usedOverdoseWeight)
            if ((usedWeight - usedAddWeight).scale(scale) != 0.0) {
                throw IllegalRecipeException(
                    "${requirement.productName}-原料[${usedIds.toNames(requirement)}]使用量：${
                        usedWeight.toBigDecimal().toPlainString()
                    } 不等于:${
                        usedAddWeight.toBigDecimal().toPlainString()
                    } = 正常使用量：${
                        usedNormalWeight.toBigDecimal().toPlainString()
                    }+过量使用量：${
                        usedOverdoseWeight.toBigDecimal().toPlainString()
                    }，差值:${
                        (usedWeight - usedAddWeight).toBigDecimal().toPlainString()
                    }"
                )
            }
            val (normal, overdose) = termThen.relationValue(true)
            val usedMinNormalWeights = normal.min
            val usedMaxNormalWeights = normal.max
            val usedMinOverdoseWeights = overdose.min
            val usedMaxOverdoseWeights = overdose.max

            // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
            if (usedNormalWeight.scale(scale) !in usedMinNormalWeights.scale(scale)..usedMaxNormalWeights.scale(
                    scale
                )
            ) {
                throw IllegalRecipeException(
                    "${requirement.productName}-原料[${usedIds.toNames(requirement)}]正常使用量：${
                        usedNormalWeight.toBigDecimal().toPlainString()
                    } 不在范围${
                        usedMinNormalWeights.toBigDecimal().toPlainString()
                    }-${usedMaxNormalWeights.toBigDecimal().toPlainString()}内"
                )
            }

            // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
            if (usedOverdoseWeight.scale(scale) !in usedMinOverdoseWeights.scale(scale)..usedMaxOverdoseWeights.scale(
                    scale
                )
            ) {
                throw IllegalRecipeException(
                    "${requirement.productName}-原料[${usedIds.toNames(requirement)}]过量使用量：${
                        usedOverdoseWeight.toBigDecimal().toPlainString()
                    } 不在范围${
                        usedMinOverdoseWeights.toBigDecimal().toPlainString()
                    }-${usedMaxOverdoseWeights.toBigDecimal().toPlainString()}内"
                )
            }
        }
        // 条件约束，当条件1满足时，条件2必须满足
        val materialConditions = requirement.materialConditionConstraints
        for ((whenCon, thenCon) in materialConditions) {
            val whenWeight =
                materials.filter { whenCon.materials.contains(it.id) }.sumOf { it.weight }
                    .scale(scale)
            val thenWeight =
                materials.filter { thenCon.materials.contains(it.id) }.sumOf { it.weight }
                    .scale(scale)
            var whenTrue = false
            val whenValue = whenCon.condition.value.scale(scale)
            val thenValue = thenCon.condition.value.scale(scale)
            when (whenCon.condition.operator) {
                Operator.EQ -> {
                    whenTrue = whenWeight == whenValue
                }

                Operator.NE -> {
                    whenTrue = whenWeight != whenValue
                }

                Operator.GT -> {
                    whenTrue = whenWeight > whenValue
                }

                Operator.LT -> {
                    whenTrue = whenWeight < whenValue
                }

                Operator.GE -> {
                    whenTrue = whenWeight >= whenValue
                }

                Operator.LE -> {
                    whenTrue = whenWeight <= whenValue
                }
            }
            when (thenCon.condition.operator) {
                Operator.EQ -> {
                    if (whenTrue && thenWeight != thenValue) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-条件约束：当${whenCon}时，${thenCon}不成立:${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                )
                            }"
                        )
                    }
                }

                Operator.NE -> {
                    if (whenTrue && thenWeight == thenValue) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-条件约束：当${whenCon}时，${thenCon}不成立:${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                )
                            }"
                        )
                    }
                }

                Operator.GT -> {
                    if (whenTrue && thenWeight <= thenValue) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-条件约束：当${whenCon}时，${thenCon}不成立:${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                )
                            }"
                        )
                    }
                }

                Operator.LT -> {
                    if (whenTrue && thenWeight >= thenValue) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-条件约束：当${whenCon}时，${thenCon}不成立:${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                )
                            }"
                        )
                    }
                }

                Operator.GE -> {
                    if (whenTrue && thenWeight < thenValue) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-条件约束：当${whenCon}时，${thenCon}不成立:${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                )
                            }"
                        )
                    }
                }

                Operator.LE -> {
                    if (whenTrue && thenWeight > thenValue) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-条件约束：当${whenCon}时，${thenCon}不成立:${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                )
                            }"
                        )
                    }
                }
            }
        }

        //检查制造费用
        optimalProductionCost?.compareTo(productionCost)

        //检查成本
        val productionCostFee = if (includeProductionCost) productionCost.totalFee else 0.0
        if ((materialCost + productionCostFee - cost).scale(scale) != 0.0) {
            throw IllegalRecipeException(
                "${requirement.productName}-配方成本不匹配，物料成本：${materialCost}+制造费用：${
                    productionCostFee.toBigDecimal().toPlainString()
                }=${
                    (materialCost + productionCostFee).toBigDecimal().toPlainString()
                } / ${cost.toBigDecimal().toPlainString()},差值：${
                    (materialCost + productionCostFee - cost).toBigDecimal().toPlainString()
                }"
            )
        }

        return true
    }

    /**
     * 消耗原料汇总相关值
     */
    fun TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>.relationValue(
        check: Boolean = false
    ): Pair<DoubleRange, DoubleRange> {
        val ids = this.term
        val materials = materials
        val consumeMaterials = materials.filter { ids.contains(it.id) }
        val usedIds = consumeMaterials.map { it.id }.toMaterialIDs()
        val replaceRate =
            if (ids.replaceRate != null && ids.replaceIds?.any { usedIds.contains(it) } == true) ids.replaceRate else 1.0

        var usedMinNormalWeight = 0.0
        var usedMaxNormalWeight = 0.0
        var usedMinOverdoseWeight = 0.0
        var usedMaxOverdoseWeight = 0.0

        this.then.forEach { (materialIDs, recipeRelation) ->
            val normal = recipeRelation.normal
            val overdose = recipeRelation.overdose
            val relationIds = materialIDs.relationIds

            materials.filter { materialIDs.contains(it.id) }.forEach { m ->

                var mMinNormalWeight = 0.0
                var mMaxNormalWeight = 0.0
                var mMinOverdoseWeight = 0.0
                var mMaxOverdoseWeight = 0.0

                //m 对应原料的用量变量
                val normalWeight =
                    if (relationIds == null) {//当无其他消耗m原料时，取本身用量
                        m.weight
                    } else {//当有其他消耗m原料时，如：氯化钾反应所需硫酸量耗液氨,取关联原料消耗汇总
                        Assert.notEmpty(
                            m.consumes,
                            "有其他关联原料消耗此原料时，先计算每个关联原料消耗的此原料"
                        )
                        m.normalWeight(relationIds)
                    }
                //过量消耗原料用量变量
                val overdoseWeight =
                    if (relationIds == null) {//当无其他消耗m原料时，不存在过量消耗
                        0.0
                    } else {//当有其他消耗m原料时，如：氯化钾反应需过量硫酸耗液氨,取关联原料消耗汇总
                        Assert.notEmpty(
                            m.consumes,
                            "有其他关联原料消耗此原料时，先计算每个关联原料消耗的此原料"
                        )
                        m.overdoseWeight(relationIds)
                    }
                if (normal != null) {
                    mMinNormalWeight += normalWeight * normal.min * replaceRate
                    mMaxNormalWeight += normalWeight * normal.max * replaceRate
                }
                if (overdose != null) {
                    mMinOverdoseWeight += normalWeight * overdose.min * replaceRate
                    mMaxOverdoseWeight += normalWeight * overdose.max * replaceRate
                }

                val overdoseMaterial = recipeRelation.overdoseMaterial
                if (overdoseMaterial != null && overdoseWeight > 0) {
                    val overdoseMaterialNormal = overdoseMaterial.normal
                    val overdoseMaterialOverdose = overdoseMaterial.overdose
                    if (overdoseMaterialNormal != null) {
                        mMinNormalWeight += overdoseWeight * overdoseMaterialNormal.min * replaceRate
                        mMaxNormalWeight += overdoseWeight * overdoseMaterialNormal.max * replaceRate
                    }
                    if (overdoseMaterialOverdose != null) {
                        mMinOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.min * replaceRate
                        mMaxOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.max * replaceRate
                    }
                }
                if (check) {
                    val consumeNormalWeight = consumeMaterials.sumOf {
                        it.consumes[m.id]!!.normal
                    }
                    val consumeOverdoseWeight = consumeMaterials.sumOf {
                        it.consumes[m.id]!!.overdose
                    }
                    // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
                    if (consumeNormalWeight.scale(scale) !in mMinNormalWeight.scale(scale)..mMaxNormalWeight.scale(
                            scale
                        )
                    ) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-原料${m.name}消耗${
                                usedIds.toNames(
                                    requirement
                                )
                            }正常使用量：${
                                consumeNormalWeight.toBigDecimal().toPlainString()
                            } 不在范围${
                                mMinNormalWeight.toBigDecimal().toPlainString()
                            }-${mMaxNormalWeight.toBigDecimal().toPlainString()}内"
                        )
                    }

                    // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
                    if (consumeOverdoseWeight.scale(scale) !in mMinOverdoseWeight.scale(scale)..mMaxOverdoseWeight.scale(
                            scale
                        )
                    ) {
                        throw IllegalRecipeException(
                            "${requirement.productName}-原料${m.name}消耗${
                                usedIds.toNames(
                                    requirement
                                )
                            }过量使用量：${
                                consumeOverdoseWeight.toBigDecimal().toPlainString()
                            } 不在范围${
                                mMinOverdoseWeight.toBigDecimal().toPlainString()
                            }-${mMaxOverdoseWeight.toBigDecimal().toPlainString()}内"
                        )
                    }
                }
                usedMinNormalWeight += mMinNormalWeight
                usedMaxNormalWeight += mMaxNormalWeight
                usedMinOverdoseWeight += mMinOverdoseWeight
                usedMaxOverdoseWeight += mMaxOverdoseWeight
            }
        }

        return DoubleRange(usedMinNormalWeight, usedMaxNormalWeight) to DoubleRange(
            usedMinOverdoseWeight,
            usedMaxOverdoseWeight
        )
    }
}
