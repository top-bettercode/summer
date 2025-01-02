package top.bettercode.summer.tools.recipe.result

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.StringUtil.toFullWidth
import top.bettercode.summer.tools.optimal.Operator
import top.bettercode.summer.tools.optimal.OptimalUtil.inRange
import top.bettercode.summer.tools.optimal.OptimalUtil.inTolerance
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.OptimalUtil.toPlainString
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
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

/**
 * 配方
 *
 * @author Peter Wu
 */
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
     * 指标小数位数
     */
    @JsonProperty("indicatorScale")
    val indicatorScale: Int,
    /**
     * 误差
     */
    @JsonProperty("minEpsilon")
    val minEpsilon: Double,
    @JsonIgnore
    val ignoreRelationCheck: Boolean = false
) {
    private val log: Logger = LoggerFactory.getLogger(Recipe::class.java)

    constructor(
        recipeMaterials: Map<String, Double>,
        requirement: RecipeRequirement,
        epsilon: Double,
        minEpsilon: Double = epsilon,
        scale: Int = abs(log10(epsilon)).toInt(),
        materials: List<RecipeMaterialValue> = recipeMaterials.map { entry ->
            RecipeMaterialValue(
                requirement.materials.find { it.id == entry.key }!!,
                entry.value,
                emptyMap()
            )
        },
    ) : this(
        recipeName = requirement.productName,
        requirement = requirement,
        includeProductionCost = true,
        optimalProductionCost = null,
        materials = materials,
        cost = materials.sumOf { it.weight * it.price } + requirement.productionCost.computeFee(
            materials = materials,
            waterWeight = materials.sumOf { it.waterWeight },
            scale = scale,
            minEpsilon = minEpsilon
        ).totalFee,
        scale = scale,
        indicatorScale = requirement.indicatorScale,
        minEpsilon = minEpsilon,
        ignoreRelationCheck = true
    )

    @get:JsonIgnore
    val productionCost: ProductionCostValue by lazy {
        requirement.productionCost.computeFee(this)
    }

    val packagingCost: Double by lazy {
        requirement.packagingMaterials.sumOf {
            it.price * it.value
        }
    }

    /** 需要烘干的水分含量  */
    val dryWaterWeight: Double
            by lazy { weight - requirement.targetWeight }

    /**
     * 物料水分重量
     */
    val waterWeight: Double
            by lazy { materials.sumOf { it.waterWeight } }

    /**
     * 总养分
     */
    val totalNutrientWeight: Double
            by lazy { materials.sumOf { it.totalNutrientWeight } }

    /**
     * 产出重量
     */
    val weight: Double
            by lazy { materials.sumOf { it.weight } }

    /** 原料成本  */
    val materialCost: Double
            by lazy { materials.sumOf { it.weight * it.price } }

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
        Assert.notNull(other, "${requirement.id}:${requirement.productName}-other配方不能为空")
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
        val costEq = (cost - other.cost).inTolerance(minEpsilon)
        compares.add(costEq)
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
            names.add("$m")
            itValues.add(m.weight)
            compares.add(
                (m.weight - otherWeight).inTolerance(minEpsilon)
            )
            otherValues.add(otherWeight)
            diffValues.add((m.weight - otherWeight))
        }
        other.materials.filter { m -> !materials.any { m.id == it.id } }.forEach {
            val otherWeight = it.weight
            names.add("$it")
            itValues.add(0.0)
            compares.add((-otherWeight).inTolerance(minEpsilon))
            otherValues.add(otherWeight)
            diffValues.add(-otherWeight)
        }

        //制造费用
        val productionCostEq: Boolean
        if (optimalProductionCost != null) {
            Assert.notNull(
                other.optimalProductionCost,
                "${requirement.id}:${requirement.productName}-other配方制造费用为空"
            )
            val productionCostSeparatorIndexs = optimalProductionCost.compareTo(
                other.optimalProductionCost!!,
                names,
                itValues,
                compares,
                otherValues,
                diffValues
            )
            productionCostEq =
                (optimalProductionCost.totalFee - other.optimalProductionCost.totalFee).inTolerance(
                    minEpsilon
                )
            separatorIndexs.addAll(productionCostSeparatorIndexs)
        } else {
            Assert.isNull(
                other.optimalProductionCost,
                "${requirement.id}:${requirement.productName}-other配方制造费用不为空"
            )
            productionCostEq = true
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
            if (costEq && productionCostEq) {
                log.warn("${requirement.id}:${requirement.productName}推优成本相同-配方不一致\n$result")
            } else
                throw IllegalRecipeException("${requirement.id}:${requirement.productName}-配方不一致\n$result")
        } else if (log.isDebugEnabled) {
            log.debug("${requirement.id}:${requirement.productName}-配方一致\n$result")
        }
    }

    fun write(file: File) {
        val objectMapper =
            StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
        objectMapper.writeValue(file, this)
    }

    //检查结果
    fun validate(): Boolean {
        val errors = mutableListOf<String>()
        //检查进料口
        val useMaterialNum = materials.filter { !it.feedPortShare }.size
        if (requirement.maxUseMaterialNum != null && useMaterialNum > requirement.maxUseMaterialNum) {
            errors.add("${requirement.id}:${requirement.productName}-配方所需进料口：$useMaterialNum 超过最大进料口：${requirement.maxUseMaterialNum}")
        }

        //检查烘干水分
        if (dryWaterWeight.scale(scale) < 0) {
            errors.add("${requirement.id}:${requirement.productName}-配方烘干水分异常：${dryWaterWeight.toPlainString()}")
        }
        if ((dryWaterWeight - waterWeight).scale(scale) > 0) {
            errors.add(
                "${requirement.id}:${requirement.productName}-配方烘干水分:${dryWaterWeight.toPlainString()} 超过总水分：${waterWeight.toPlainString()},差值：${
                    (dryWaterWeight - waterWeight).toPlainString()
                }"
            )
        }
        if (requirement.maxBakeWeight != null) {
            val dryDiff = (dryWaterWeight - requirement.maxBakeWeight).scale(scale)
            if (dryDiff > 0) {
                errors.add(
                    "${requirement.id}:${requirement.productName}-配方烘干水分:${dryWaterWeight.toPlainString()} 超过最大可烘干水分：${requirement.maxBakeWeight} ,差值：${
                        dryDiff.toPlainString()
                    }"
                )
            }
        }


        val targetWeight = requirement.targetWeight
        // 指标范围约束
        val rangeIndicators = requirement.indicatorRangeConstraints
        for (rangeIndicator in rangeIndicators) {
            val indicator = rangeIndicator.indicator
            val indicatorValue = when (indicator.type) {
                RecipeIndicatorType.TOTAL_NUTRIENT -> (totalNutrientWeight / targetWeight)
                RecipeIndicatorType.PRODUCT_WATER -> ((waterWeight - dryWaterWeight) / targetWeight)
                RecipeIndicatorType.WATER -> (waterWeight / weight)
                RecipeIndicatorType.RATE_TO_OTHER -> {
                    val sumOf = materials.sumOf {
                        it.indicatorWeight(
                            indicator.otherId!!
                        )
                    }
                    if (sumOf == 0.0) {
                        0.0
                    } else
                        (materials.sumOf { it.indicatorWeight(indicator.itId!!) } / sumOf)
                }

                else -> (materials.sumOf { it.indicatorWeight(rangeIndicator.id) } / targetWeight)
            }
            // 如果 indicatorValue 不在value.min,value.max范围内，返回 false
            val minIndicatorEpsilon = 1 / 10.0.pow(indicatorScale.toDouble())
            if (!indicatorValue.inRange(
                    min = rangeIndicator.scaledValue.min,
                    max = rangeIndicator.scaledValue.max,
                    minEpsilon = minIndicatorEpsilon
                )
            ) {
                errors.add("${requirement.id}:${requirement.productName}-指标:${indicator.name}：${indicatorValue.toPlainString()} 不在范围${rangeIndicator.scaledValue.min.toPlainString()}-${rangeIndicator.scaledValue.max.toPlainString()}内")
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
                    errors.add("${requirement.id}:${requirement.productName}-指标:${indicator.name}所用原料：${indicatorUsedMaterials} 不在范围${materialIDIndicator.value}内")
                }
            }
        }

        val usedMaterials = materials.map { it.id }
        // 保留用原料ID
        val keepMaterials = requirement.keepMaterialConstraints
        if (keepMaterials.ids.isNotEmpty()) {
            if (!keepMaterials.containsAll(usedMaterials)) {
                errors.add("${requirement.id}:${requirement.productName}-配方所用原料：${usedMaterials} 不在范围${keepMaterials}内")
            }
        }
        // 不能用原料ID
        val noUseMaterials = requirement.noUseMaterialConstraints
        if (noUseMaterials.ids.isNotEmpty()) {
            if (usedMaterials.any {
                    !requirement.mustUseMaterial.test(it) && noUseMaterials.contains(
                        it
                    )
                }) {
                errors.add("${requirement.id}:${requirement.productName}-配方所用原料：${usedMaterials} 包含不可用原料${noUseMaterials}内")
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
                errors.add(
                    "${requirement.id}:${requirement.productName}-配方混用原料：${
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
            if (!weight.inRange(min = range.min, max = range.max, minEpsilon = minEpsilon)) {
                errors.add(
                    "${requirement.id}:${requirement.productName}-原料${
                        ids.toNames(requirement)
                    }使用量：${
                        weight.toPlainString()
                    } 不在范围${range.min.toPlainString()}-${range.max.toPlainString()}内"
                )
            }
        }
        // 指定原料约束
        val materialIDConstraints = requirement.materialIDConstraints
        for ((ids, value) in materialIDConstraints) {
            for (idd in ids) {
                if (usedMaterials.contains(idd) && !value.contains(idd)) {
                    errors.add(
                        "${requirement.id}:${requirement.productName}-${
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
        val calIds = mutableSetOf<String>()
        for (termThen in materialRelationConstraints) {
            if (termThen.then.isEmpty()) {
                continue
            }
            val ids = termThen.term
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val usedWeight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            val (normal, overdose) = termThen.relationValue(
                calIds = calIds,
                errors = errors
            )
            val usedMinNormalWeights = normal.min
            val usedMaxNormalWeights = normal.max
            val usedMinOverdoseWeights = overdose.min
            val usedMaxOverdoseWeights = overdose.max
            val useMinWeight = usedMinNormalWeights + usedMinOverdoseWeights
            val useMaxWeight = usedMaxNormalWeights + usedMaxOverdoseWeights

            if (!usedWeight.inRange(
                    min = useMinWeight,
                    max = useMaxWeight,
                    minEpsilon = minEpsilon
                )
            ) {
                errors.add(
                    "${requirement.id}:${requirement.productName}-原料[${
                        usedIds.toNames(
                            requirement
                        )
                    }]使用量：${
                        usedWeight.toPlainString()
                    } 不在范围${
                        useMinWeight.toPlainString()
                    }-${useMaxWeight.toPlainString()}内。${if (usedWeight < useMinWeight) "原料使用不足。" else "原料使用不是全部为关联原料产生。"}"
                )
            }

            if (!ignoreRelationCheck) {
                val usedNormalWeight =
                    materials.filter { ids.contains(it.id) }.sumOf { it.normalWeight }
                val usedOverdoseWeight =
                    materials.filter { ids.contains(it.id) }.sumOf { it.overdoseWeight }
                val usedAddWeight = (usedNormalWeight + usedOverdoseWeight)
                if (!(usedWeight - usedAddWeight).inTolerance(minEpsilon)) {
                    errors.add(
                        "${requirement.id}:${requirement.productName}-原料[${
                            usedIds.toNames(
                                requirement
                            )
                        }]使用量：${
                            usedWeight.toPlainString()
                        } 不等于:${
                            usedAddWeight.toPlainString()
                        } = 正常使用量：${
                            usedNormalWeight.toPlainString()
                        }+过量使用量：${
                            usedOverdoseWeight.toPlainString()
                        }，差值:${
                            (usedWeight - usedAddWeight).toPlainString()
                        }。${if ((usedWeight - usedAddWeight).scale(scale) < 0.0) "原料使用不足。" else "原料使用不是全部为关联原料产生。"}"
                    )
                }

                // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
                if (!usedNormalWeight.inRange(
                        min = usedMinNormalWeights,
                        max = usedMaxNormalWeights,
                        minEpsilon = minEpsilon
                    )
                ) {
                    errors.add(
                        "${requirement.id}:${requirement.productName}-原料[${
                            usedIds.toNames(
                                requirement
                            )
                        }]正常使用量：${
                            usedNormalWeight.toPlainString()
                        } 不在范围${
                            usedMinNormalWeights.toPlainString()
                        }-${usedMaxNormalWeights.toPlainString()}内"
                    )
                }

                // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
                if (!usedOverdoseWeight.inRange(
                        min = usedMinOverdoseWeights,
                        max = usedMaxOverdoseWeights,
                        minEpsilon = minEpsilon
                    )
                ) {
                    errors.add(
                        "${requirement.id}:${requirement.productName}-原料[${
                            usedIds.toNames(
                                requirement
                            )
                        }]过量使用量：${
                            usedOverdoseWeight.toPlainString()
                        } 不在范围${
                            usedMinOverdoseWeights.toPlainString()
                        }-${usedMaxOverdoseWeights.toPlainString()}内"
                    )
                }
            }
        }
        // 条件约束，当条件1满足时，条件2必须满足
        val materialConditions = requirement.materialConditionConstraints
        for ((whenCon, thenCon) in materialConditions) {
            val whenValue = whenCon.condition.value
            val thenValue = thenCon.condition.value

            val whenWeight =
                materials.filter { whenCon.materials.contains(it.id) }.sumOf { it.weight }
                    .scale(scale)
            val thenWeight =
                materials.filter { thenCon.materials.contains(it.id) }.sumOf { it.weight }
                    .scale(scale)
            var whenTrue = false

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
                        errors.add(
                            "${requirement.id}:${requirement.productName}-条件约束：当${
                                whenCon.toString(requirement)
                            }时，${thenCon.toString(requirement)}不成立:${
                                MaterialCondition(
                                    whenCon.materials,
                                    RecipeCondition(value = whenWeight)
                                ).toString(requirement)
                            } ${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                ).toString(requirement)
                            }"
                        )
                    }
                }

                Operator.NE -> {
                    if (whenTrue && thenWeight == thenValue) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-条件约束：当${
                                whenCon.toString(
                                    requirement
                                )
                            }时，${thenCon.toString(requirement)}不成立:${
                                MaterialCondition(
                                    whenCon.materials,
                                    RecipeCondition(value = whenWeight)
                                ).toString(requirement)
                            } ${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                ).toString(requirement)
                            }"
                        )
                    }
                }

                Operator.GT -> {
                    if (whenTrue && thenWeight <= thenValue) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-条件约束：当${
                                whenCon.toString(
                                    requirement
                                )
                            }时，${thenCon.toString(requirement)}不成立:${
                                MaterialCondition(
                                    whenCon.materials,
                                    RecipeCondition(value = whenWeight)
                                ).toString(requirement)
                            } ${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                ).toString(requirement)
                            }"
                        )
                    }
                }

                Operator.LT -> {
                    if (whenTrue && thenWeight >= thenValue) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-条件约束：当${
                                whenCon.toString(
                                    requirement
                                )
                            }时，${thenCon.toString(requirement)}不成立:${
                                MaterialCondition(
                                    whenCon.materials,
                                    RecipeCondition(value = whenWeight)
                                ).toString(requirement)
                            } ${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                ).toString(requirement)
                            }"
                        )
                    }
                }

                Operator.GE -> {
                    if (whenTrue && thenWeight < thenValue) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-条件约束：当${
                                whenCon.toString(
                                    requirement
                                )
                            }时，${thenCon.toString(requirement)}不成立:${
                                MaterialCondition(
                                    whenCon.materials,
                                    RecipeCondition(value = whenWeight)
                                ).toString(requirement)
                            } ${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                ).toString(requirement)
                            }"
                        )
                    }
                }

                Operator.LE -> {
                    if (whenTrue && thenWeight > thenValue) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-条件约束：当${
                                whenCon.toString(
                                    requirement
                                )
                            }时，${thenCon.toString(requirement)}不成立:${
                                MaterialCondition(
                                    whenCon.materials,
                                    RecipeCondition(value = whenWeight)
                                ).toString(requirement)
                            } ${
                                MaterialCondition(
                                    thenCon.materials,
                                    RecipeCondition(value = thenWeight)
                                ).toString(requirement)
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
        if (!(materialCost + productionCostFee - cost).inTolerance(minEpsilon)) {
            errors.add(
                "${requirement.id}:${requirement.productName}-配方成本不匹配，物料成本：${materialCost}+制造费用：${
                    productionCostFee.toPlainString()
                }=${
                    (materialCost + productionCostFee).toPlainString()
                } / ${cost.toPlainString()},差值：${
                    (materialCost + productionCostFee - cost).toPlainString()
                }"
            )
        }

        if (errors.isNotEmpty()) {
            throw IllegalRecipeException(errors.joinToString("\n"))
        }

        return true
    }

    /**
     * 消耗原料汇总相关值
     */
    fun TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>.relationValue(
        calIds: MutableSet<String> = mutableSetOf(), errors: MutableList<String>? = null
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
                        if (errors != null && !calIds.contains(m.id))
                            errors.add(
                                "有其他关联原料消耗此原料时，先计算每个关联原料消耗的此原料"
                            )
                        m.normalWeight(relationIds)
                    }
                //过量消耗原料用量变量
                val overdoseWeight =
                    if (relationIds == null) {//当无其他消耗m原料时，不存在过量消耗
                        0.0
                    } else {//当有其他消耗m原料时，如：氯化钾反应需过量硫酸耗液氨,取关联原料消耗汇总
                        if (errors != null && !calIds.contains(m.id))
                            errors.add(
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
                if (errors != null && !ignoreRelationCheck) {
                    val consumeNormalWeight = consumeMaterials.sumOf {
                        it.consumes[m.id]?.normal ?: 0.0
                    }
                    val consumeOverdoseWeight = consumeMaterials.sumOf {
                        it.consumes[m.id]?.overdose ?: 0.0
                    }
                    // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
                    if (!consumeNormalWeight.inRange(
                            min = mMinNormalWeight,
                            max = mMaxNormalWeight,
                            minEpsilon = minEpsilon
                        )
                    ) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-原料${m.name}消耗${
                                usedIds.toNames(
                                    requirement
                                )
                            }正常使用量：${
                                consumeNormalWeight.toPlainString()
                            } 不在范围${
                                mMinNormalWeight.toPlainString()
                            }-${mMaxNormalWeight.toPlainString()}内"
                        )
                    }

                    // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
                    if (!consumeOverdoseWeight.inRange(
                            min = mMinOverdoseWeight,
                            max = mMaxOverdoseWeight,
                            minEpsilon = minEpsilon
                        )
                    ) {
                        errors.add(
                            "${requirement.id}:${requirement.productName}-原料${m.name}消耗${
                                usedIds.toNames(
                                    requirement
                                )
                            }过量使用量：${
                                consumeOverdoseWeight.toPlainString()
                            } 不在范围${
                                mMinOverdoseWeight.toPlainString()
                            }-${mMaxOverdoseWeight.toPlainString()}内"
                        )
                    }
                }
                usedMinNormalWeight += mMinNormalWeight
                usedMaxNormalWeight += mMaxNormalWeight
                usedMinOverdoseWeight += mMinOverdoseWeight
                usedMaxOverdoseWeight += mMaxOverdoseWeight
            }
        }
        if (errors != null) {
            calIds.addAll(ids.ids)
            ids.replaceIds?.let {
                calIds.addAll(it)
            }
        }
        return DoubleRange(usedMinNormalWeight, usedMaxNormalWeight) to DoubleRange(
            usedMinOverdoseWeight,
            usedMaxOverdoseWeight
        )
    }

    override fun toString(): String {
        val separatorIndexs = mutableListOf<Int>()
        val names = RecipeColumns(scale)
        val itValues = RecipeColumns(scale)
        separatorIndexs.add(names.size)
        names.add("原料/制造费用")
        itValues.add(this.recipeName)
        separatorIndexs.add(names.size)

        //总成本(制造费用+原料成本)
        names.add("总成本(制造费用+原料成本)")
        itValues.add(cost)
        separatorIndexs.add(names.size)
        //原料用量
        names.add("原料数量")
        itValues.add(materials.size)
        separatorIndexs.add(names.size)

        materials.forEach { m ->
            names.add("$m")
            itValues.add(m.weight)
        }

        //制造费用
        if (optimalProductionCost != null) {
            val productionCostSeparatorIndexs = optimalProductionCost.toString(
                names,
                itValues
            )
            separatorIndexs.addAll(productionCostSeparatorIndexs)
        }

        // 计算每一列的最大宽度
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
