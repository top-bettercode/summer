package top.bettercode.summer.tools.recipe.result

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.Sense
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.RecipeUtil
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
         * 配方要求
         */
        @JsonProperty("requirement")
        val requirement: RecipeRequirement,
        @JsonProperty("includeProductionCost")
        val includeProductionCost: Boolean,
        @JsonProperty("optimalProductionCost")
        val optimalProductionCost: ProductionCostValue?,
        @JsonProperty("cost")
        val cost: Double,
        /** 选用的原料  */
        @JsonProperty("materials")
        val materials: List<RecipeMaterialValue>
) {
    private val log: Logger = LoggerFactory.getLogger(Recipe::class.java)

    val productionCost: ProductionCostValue = requirement.productionCost.computeFee(this)

    val packagingCost: Double = requirement.packagingMaterials.sumOf {
        it.price * it.value
    }

    /** 需要烘干的水分含量  */
    val dryWaterWeight: Double
        get() = (weight - requirement.targetWeight).scale()

    /**
     * 物料水分重量
     */
    val waterWeight: Double
        get() = materials.sumOf { it.waterWeight }.scale()

    /**
     * 产出重量
     */
    val weight: Double
        get() = materials.sumOf { it.weight }.scale()

    /** 配方成本  */
    val materialCost: Double
        get() = materials.sumOf { it.weight * it.price }.scale()

    companion object {
        fun read(file: File): Recipe {
            val objectMapper = StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
            return objectMapper.readValue(file, Recipe::class.java)
        }
    }

    fun write(file: File) {
        val objectMapper = StringUtil.objectMapper(format = true, include = JsonInclude.Include.NON_NULL)
        objectMapper.writeValue(file, this)
    }

    //检查结果
    fun validate(): Boolean {
        //检查进料口
        if (requirement.maxUseMaterialNum != null && materials.size > requirement.maxUseMaterialNum) {
            throw IllegalRecipeException("配方所需进料口：${materials.size} 超过最大进料口：${requirement.maxUseMaterialNum}")
        }

        //检查烘干水分
        if (dryWaterWeight < -RecipeUtil.DEFAULT_MIN_EPSILON) {
            throw IllegalRecipeException("配方烘干水分异常：${dryWaterWeight}")
        }
        if (requirement.maxBakeWeight != null && (dryWaterWeight - requirement.maxBakeWeight).scale() > RecipeUtil.DEFAULT_MIN_EPSILON) {
            throw IllegalRecipeException("配方烘干水分:${dryWaterWeight} 超过最大可烘干水分：${requirement.maxBakeWeight}")
        }
        if ((dryWaterWeight - waterWeight).scale() > RecipeUtil.DEFAULT_MIN_EPSILON) {
            throw IllegalRecipeException("配方烘干水分:${dryWaterWeight} 超过总水分：${waterWeight}")
        }

        val targetWeight = requirement.targetWeight
        // 指标范围约束
        val rangeIndicators = requirement.indicatorRangeConstraints
        for (indicator in rangeIndicators) {
            val indicatorValue = when (indicator.type) {
                RecipeIndicatorType.PRODUCT_WATER -> ((waterWeight - dryWaterWeight) / targetWeight).scale()
                RecipeIndicatorType.RATE_TO_OTHER -> (materials.sumOf { it.indicatorWeight(indicator.itId!!) } / materials.sumOf { it.indicatorWeight(indicator.otherId!!) }).scale()
                else -> (materials.sumOf { it.indicatorWeight(indicator.id) } / targetWeight).scale()
            }
            // 如果 indicatorValue 不在value.min,value.max范围内，返回 false
            if (indicatorValue !in indicator.value.min..indicator.value.max) {
                throw IllegalRecipeException("指标:${indicator.name}：${indicatorValue} 不在范围${indicator.value.min}-${indicator.value.max}内")
            }
        }

        val materialRangeConstraints = requirement.materialRangeConstraints
        val mustUseMaterials = materialRangeConstraints.filter { it.then.min > 0 }.map { it.term }.flatten()

        // 指标原料约束
        val materialIDIndicators = requirement.indicatorMaterialIDConstraints
        for (indicator in materialIDIndicators) {
            val materialList = materials.filter { it.indicators.valueOf(indicator.id) > 0.0 }
            if (materialList.isNotEmpty()) {
                val indicatorUsedMaterials = materialList.map { it.id }.filter { !mustUseMaterials.contains(it) }
                if (!indicator.value.containsAll(indicatorUsedMaterials)) {
                    throw IllegalRecipeException("指标:${indicator.name}所用原料：${indicatorUsedMaterials} 不在范围${indicator.value}内")
                }
            }
        }

        val usedMaterials = materials.map { it.id }
        // 指定用原料ID
        val useMaterials = requirement.useMaterialConstraints
        if (useMaterials.ids.isNotEmpty()) {
            if (!useMaterials.containsAll(usedMaterials)) {
                throw IllegalRecipeException("配方所用原料：${usedMaterials} 不在范围${useMaterials}内")
            }
        }
        // 不能用原料ID
        val noUseMaterials = requirement.noUseMaterialConstraints
        if (noUseMaterials.ids.isNotEmpty()) {
            if (usedMaterials.any { noUseMaterials.contains(it) }) {
                throw IllegalRecipeException("配方所用原料：${usedMaterials} 包含不可用原料${noUseMaterials}内")
            }
        }
        // 不能混用的原料,value: 原料ID
        val notMixMaterials = requirement.notMixMaterialConstraints
        for (notMixMaterial in notMixMaterials) {
            val mixMaterials = notMixMaterial.map { notMix -> usedMaterials.filter { notMix.contains(it) } }.filter { it.isNotEmpty() }
            val size = mixMaterials.size
            if (size > 1) {
                throw IllegalRecipeException("配方混用原料：${mixMaterials.joinToString(",", "[", "]") { it.joinToString("、") }}")
            }
        }

        // 原料约束,key:原料ID, value: 原料使用范围约束
        for ((ids, range) in materialRangeConstraints) {
            val weight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            if (weight !in range.min..range.max) {
                throw IllegalRecipeException("原料${ids}使用量：${weight} 不在范围${range.min}-${range.max}内")
            }
        }
        // 指定原料约束
        val materialIDConstraints = requirement.materialIDConstraints
        for ((ids, value) in materialIDConstraints) {
            for (id in ids) {
                if (usedMaterials.contains(id) && !value.contains(id)) {
                    throw IllegalRecipeException("${ids}使用了不在指定原料${value}范围内的原料：${id}")
                }
            }
        }
        // 关联原料约束
        val materialRelationConstraints = requirement.materialRelationConstraints
        for (termThen in materialRelationConstraints) {
            val ids = termThen.term
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val usedWeight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            val usedNormalWeight = materials.filter { ids.contains(it.id) }.sumOf { it.normalWeight }
            val usedOverdoseWeight = materials.filter { ids.contains(it.id) }.sumOf { it.overdoseWeight }
            val usedAddWeight = (usedNormalWeight + usedOverdoseWeight)
            if ((usedWeight - usedAddWeight).scale() !in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON) {
                throw IllegalRecipeException("原料${usedIds}使用量：${usedWeight} 不等于:${usedAddWeight} = 正常使用量：${usedNormalWeight}+过量使用量：${usedOverdoseWeight}")
            }
            val (normal, overdose) = termThen.relationValue
            val usedMinNormalWeights = normal.min
            val usedMaxNormalWeights = normal.max
            val usedMinOverdoseWeights = overdose.min
            val usedMaxOverdoseWeights = overdose.max

            // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
            if (usedNormalWeight !in (usedMinNormalWeights - RecipeUtil.DEFAULT_MIN_EPSILON).scale()..(usedMaxNormalWeights + RecipeUtil.DEFAULT_MIN_EPSILON).scale()) {
                throw IllegalRecipeException("原料${usedIds}正常使用量：${usedNormalWeight} 不在范围${usedMinNormalWeights}-${usedMaxNormalWeights}内")
            }

            // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
            if (usedOverdoseWeight !in (usedMinOverdoseWeights - RecipeUtil.DEFAULT_MIN_EPSILON).scale()..(usedMaxOverdoseWeights + RecipeUtil.DEFAULT_MIN_EPSILON).scale()) {
                throw IllegalRecipeException("原料${usedIds}过量使用量：${usedOverdoseWeight} 不在范围${usedMinOverdoseWeights}-${usedMaxOverdoseWeights}内")
            }
        }
        // 条件约束，当条件1满足时，条件2必须满足
        val materialConditions = requirement.materialConditionConstraints
        for ((whenCon, thenCon) in materialConditions) {
            val whenWeight = materials.filter { whenCon.materials.contains(it.id) }.sumOf { it.weight }
            val thenWeight = materials.filter { thenCon.materials.contains(it.id) }.sumOf { it.weight }
            var whenTrue = false
            when (whenCon.condition.sense) {
                Sense.EQ -> {
                    whenTrue = whenWeight == whenCon.condition.value
                }

                Sense.NE -> {
                    whenTrue = whenWeight != whenCon.condition.value
                }

                Sense.GT -> {
                    whenTrue = whenWeight > whenCon.condition.value
                }

                Sense.LT -> {
                    whenTrue = whenWeight < whenCon.condition.value
                }

                Sense.GE -> {
                    whenTrue = whenWeight >= whenCon.condition.value
                }

                Sense.LE -> {
                    whenTrue = whenWeight <= whenCon.condition.value
                }
            }
            when (thenCon.condition.sense) {
                Sense.EQ -> {
                    if (whenTrue && thenWeight != thenCon.condition.value) {
                        throw IllegalRecipeException("条件约束：当${whenCon}时，${thenCon}不成立:${MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight))}")
                    }
                }

                Sense.NE -> {
                    if (whenTrue && thenWeight == thenCon.condition.value) {
                        throw IllegalRecipeException("条件约束：当${whenCon}时，${thenCon}不成立:${MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight))}")
                    }
                }

                Sense.GT -> {
                    if (whenTrue && thenWeight <= thenCon.condition.value) {
                        throw IllegalRecipeException("条件约束：当${whenCon}时，${thenCon}不成立:${MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight))}")
                    }
                }

                Sense.LT -> {
                    if (whenTrue && thenWeight >= thenCon.condition.value) {
                        throw IllegalRecipeException("条件约束：当${whenCon}时，${thenCon}不成立:${MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight))}")
                    }
                }

                Sense.GE -> {
                    if (whenTrue && thenWeight < thenCon.condition.value) {
                        throw IllegalRecipeException("条件约束：当${whenCon}时，${thenCon}不成立:${MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight))}")
                    }
                }

                Sense.LE -> {
                    if (whenTrue && thenWeight > thenCon.condition.value) {
                        throw IllegalRecipeException("条件约束：当${whenCon}时，${thenCon}不成立:${MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight))}")
                    }
                }
            }
        }

        //检查制造费用
        optimalProductionCost?.validate(productionCost)

        //检查成本
        val productionCostFee = if (includeProductionCost) productionCost.totalFee else 0.0
        if ((materialCost + productionCostFee - cost).scale() !in -RecipeUtil.DEFAULT_MIN_EPSILON..RecipeUtil.DEFAULT_MIN_EPSILON) {
            throw IllegalRecipeException("配方成本不匹配，物料成本：${materialCost}+制造费用：${productionCostFee}=${materialCost + productionCostFee} / ${cost},差：${materialCost + productionCostFee - cost}")
        }

        return true
    }

    /**
     * 消耗原料汇总相关值
     */
    val TermThen<ReplacebleMaterialIDs, List<TermThen<RelationMaterialIDs, RecipeRelation>>>.relationValue: Pair<DoubleRange, DoubleRange>
        get() {
            val ids = this.term
            val materials = materials
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val replaceRate = if (ids.replaceIds == usedIds) ids.replaceRate ?: 1.0 else 1.0

            var usedMinNormalWeight = 0.0
            var usedMaxNormalWeight = 0.0
            var usedMinOverdoseWeight = 0.0
            var usedMaxOverdoseWeight = 0.0
            this.then.forEach { (materialIDs, recipeRelation) ->
                val normal = recipeRelation.normal
                val overdose = recipeRelation.overdose
                val relationIds = materialIDs.relationIds
                val weight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.weight }
                var normalWeight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.normalWeight(relationIds) }
                if (normalWeight == 0.0) {
                    normalWeight = weight
                }
                if (normal != null) {
                    usedMinNormalWeight += normalWeight * normal.min * replaceRate
                    usedMaxNormalWeight += normalWeight * normal.max * replaceRate
                }
                if (overdose != null) {
                    usedMinOverdoseWeight += normalWeight * overdose.min * replaceRate
                    usedMaxOverdoseWeight += normalWeight * overdose.max * replaceRate
                }

                val overdoseMaterial = recipeRelation.overdoseMaterial
                val overdoseWeight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.overdoseWeight(relationIds) }
                if (overdoseMaterial != null && overdoseWeight > 0) {
                    val overdoseMaterialNormal = overdoseMaterial.normal
                    val overdoseMaterialOverdose = overdoseMaterial.overdose
                    if (overdoseMaterialNormal != null) {
                        usedMinNormalWeight += overdoseWeight * overdoseMaterialNormal.min * replaceRate
                        usedMaxNormalWeight += overdoseWeight * overdoseMaterialNormal.max * replaceRate
                    }
                    if (overdoseMaterialOverdose != null) {
                        usedMinOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.min * replaceRate
                        usedMaxOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.max * replaceRate
                    }
                }
            }

            return DoubleRange(usedMinNormalWeight, usedMaxNormalWeight) to DoubleRange(usedMinOverdoseWeight, usedMaxOverdoseWeight)
        }
}
