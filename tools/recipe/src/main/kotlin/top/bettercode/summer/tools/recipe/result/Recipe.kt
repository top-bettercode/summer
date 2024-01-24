package top.bettercode.summer.tools.recipe.result

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.solver.OptimalUtil
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.Operator
import top.bettercode.summer.tools.recipe.criteria.RecipeCondition
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicatorType
import top.bettercode.summer.tools.recipe.material.MaterialCondition
import top.bettercode.summer.tools.recipe.material.MaterialIDs
import top.bettercode.summer.tools.recipe.material.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.material.ReplacebleMaterialIDs

/**
 * 配方
 *
 * @author Peter Wu
 */
data class Recipe(
        /**
         * 配方要求
         */
        val requirement: RecipeRequirement,
        val cost: Double,
        /** 选用的物料  */
        val materials: List<RecipeMaterialValue>
) {
    private val log: Logger = LoggerFactory.getLogger(Recipe::class.java)

    /** 需要烘干的水分含量  */
    val dryWater: Double
        get() = (materials.sumOf { it.weight } - requirement.targetWeight).scale()


    /** 配方成本  */
    val trueCost: Double
        get() = materials.sumOf { it.weight * it.price }.scale()


    //检查结果
    fun validate(): Boolean {
        //检查进料口
        if (requirement.maxMaterialNum > 0 && materials.size > requirement.maxMaterialNum) {
            log.warn("配方所需进料口：{} 超过最大进料口：{}", materials.size, requirement.maxMaterialNum)
            return false
        }
        //检查成本
        if ((trueCost - cost).scale() !in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON) {
            log.warn("配方成本不匹配:{} / {}", trueCost, cost)
            return false
        }
        //检查烘干水分
        if (dryWater < -OptimalUtil.DEFAULT_MIN_EPSILON) {
            log.warn("配方烘干水分异常：{}", dryWater)
            return false
        }
        if (requirement.maxBakeWeight >= 0 && (dryWater - requirement.maxBakeWeight).scale() > OptimalUtil.DEFAULT_MIN_EPSILON) {
            log.warn("配方烘干水分:{} 超过最大可烘干水分：{}", dryWater, requirement.maxBakeWeight)
            return false
        }
        val totalWater = materials.sumOf {
            it.waterWeight
        }.scale()
        if ((dryWater - totalWater).scale() > OptimalUtil.DEFAULT_MIN_EPSILON) {
            log.warn("配方烘干水分:{} 超过总水分：{}", dryWater, totalWater)
            return false
        }

        val targetWeight = requirement.targetWeight
        // 指标范围约束
        val rangeIndicators = requirement.rangeIndicators
        for (indicator in rangeIndicators) {
            val indicatorValue = when (indicator.type) {
                RecipeIndicatorType.WATER -> ((materials.sumOf { it.waterWeight } - dryWater) / targetWeight).scale()
                RecipeIndicatorType.RATE_TO_OTHER -> (materials.sumOf { it.indicatorWeight(indicator.itId!!) } / materials.sumOf { it.indicatorWeight(indicator.otherId!!) }).scale()
                else -> (materials.sumOf { it.indicatorWeight(indicator.id) } / targetWeight).scale()
            }
            // 如果 indicatorValue 不在value.min,value.max范围内，返回 false
            if (indicatorValue !in indicator.value.min..indicator.value.max) {
                log.warn("指标:{}：{} 不在范围{}-{}内", indicator.name, indicatorValue, indicator.value.min, indicator.value.max)
                return false
            }
        }

        val materialRangeConstraints = requirement.materialRangeConstraints
        val mustUseMaterials = materialRangeConstraints.filter { it.value.min > 0 }.map { it.key }.flatten()

        // 指标物料约束
        val materialIDIndicators = requirement.materialIDIndicators
        for (indicator in materialIDIndicators) {
            val indicatorUsedMaterials = materials.filter { it.indicators.valueOf(indicator.id) > 0.0 }.map { it.id }.filter { !mustUseMaterials.contains(it) }
            if (!indicator.value.toList().containsAll(indicatorUsedMaterials)) {
                log.warn("指标:{}所用物料：{} 不在范围{}内", indicator.name, indicatorUsedMaterials, indicator.value)
                return false
            }
        }

        val usedMaterials = materials.map { it.id }
        // 限用物料ID
        val useMaterials = requirement.useMaterials
        if (useMaterials.isNotEmpty()) {
            if (!useMaterials.containsAll(usedMaterials)) {
                log.warn("配方所用物料：{} 不在范围{}内", usedMaterials, useMaterials)
                return false
            }
        }
        // 不使用的物料ID
        val noUseMaterials = requirement.noUseMaterials
        if (noUseMaterials.isNotEmpty()) {
            if (usedMaterials.any { noUseMaterials.contains(it) }) {
                log.warn("配方所用物料：{} 包含不可用物料{}内", usedMaterials, noUseMaterials)
                return false
            }
        }
        // 不能混用的物料,value: 物料ID
        val notMixMaterials = requirement.notMixMaterials
        for (notMixMaterial in notMixMaterials) {
            val mixMaterials = notMixMaterial.map { notMix -> usedMaterials.filter { notMix.contains(it) } }.filter { it.isNotEmpty() }
            val size = mixMaterials.size
            if (size > 1) {
                log.warn("配方混用物料：{}", mixMaterials.joinToString(",", "[", "]") { it.joinToString("、") })
                return false
            }
        }

        // 物料约束,key:物料ID, value: 物料使用范围约束
        for ((ids, range) in materialRangeConstraints) {
            val weight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            if (weight !in range.min..range.max) {
                log.warn("物料{}使用量：{} 不在范围{}-{}内", ids, weight, range.min, range.max)
                return false
            }
        }
        // 指定物料约束
        val materialIDConstraints = requirement.materialIDConstraints
        for ((ids, value) in materialIDConstraints) {
            for (id in ids) {
                if (usedMaterials.contains(id) && !value.contains(id)) {
                    log.warn("{}使用了不在指定物料{}范围内的物料：{}", ids, value, id)
                    return false
                }
            }
        }
        // 关联物料约束
        val materialRelationConstraints = requirement.materialRelationConstraints
        for (entry in materialRelationConstraints) {
            val ids = entry.key
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val usedWeight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            val usedNormalWeight = materials.filter { ids.contains(it.id) }.sumOf { it.normalWeight }
            val usedOverdoseWeight = materials.filter { ids.contains(it.id) }.sumOf { it.overdoseWeight }
            val usedAddWeight = (usedNormalWeight + usedOverdoseWeight)
            if ((usedWeight - usedAddWeight).scale() !in -OptimalUtil.DEFAULT_MIN_EPSILON..OptimalUtil.DEFAULT_MIN_EPSILON) {
                log.warn("物料{}使用量：{} 不等于:{} = 正常使用量：{}+过量使用量：{}", usedIds, usedWeight, usedAddWeight, usedNormalWeight, usedOverdoseWeight)
                return false
            }
            val (normal, overdose) = entry.relationValue
            val usedMinNormalWeights = normal.min
            val usedMaxNormalWeights = normal.max
            val usedMinOverdoseWeights = overdose.min
            val usedMaxOverdoseWeights = overdose.max

            // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
            if (usedNormalWeight !in (usedMinNormalWeights - OptimalUtil.DEFAULT_MIN_EPSILON).scale()..(usedMaxNormalWeights + OptimalUtil.DEFAULT_MIN_EPSILON).scale()) {
                log.warn("物料{}正常使用量：{} 不在范围{}-{}内", usedIds, usedNormalWeight, usedMinNormalWeights, usedMaxNormalWeights)
                return false
            }

            // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
            if (usedOverdoseWeight !in (usedMinOverdoseWeights - OptimalUtil.DEFAULT_MIN_EPSILON).scale()..(usedMaxOverdoseWeights + OptimalUtil.DEFAULT_MIN_EPSILON).scale()) {
                log.warn("物料{}过量使用量：{} 不在范围{}-{}内", usedIds, usedOverdoseWeight, usedMinOverdoseWeights, usedMaxOverdoseWeights)
                return false
            }
        }
        // 条件约束，当条件1满足时，条件2必须满足
        val materialConditions = requirement.materialConditions
        for ((whenCon, thenCon) in materialConditions) {
            val whenWeight = materials.filter { whenCon.materials.contains(it.id) }.sumOf { it.weight }
            val thenWeight = materials.filter { thenCon.materials.contains(it.id) }.sumOf { it.weight }
            var whenTrue = false
            when (whenCon.condition.operator) {
                Operator.EQ -> {
                    whenTrue = whenWeight == whenCon.condition.value
                }

                Operator.NE -> {
                    whenTrue = whenWeight != whenCon.condition.value
                }

                Operator.GT -> {
                    whenTrue = whenWeight > whenCon.condition.value
                }

                Operator.LT -> {
                    whenTrue = whenWeight < whenCon.condition.value
                }

                Operator.GE -> {
                    whenTrue = whenWeight >= whenCon.condition.value
                }

                Operator.LE -> {
                    whenTrue = whenWeight <= whenCon.condition.value
                }
            }
            when (thenCon.condition.operator) {
                Operator.EQ -> {
                    if (whenTrue && thenWeight != thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.NE -> {
                    if (whenTrue && thenWeight == thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.GT -> {
                    if (whenTrue && thenWeight <= thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.LT -> {
                    if (whenTrue && thenWeight >= thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false

                    }
                }

                Operator.GE -> {
                    if (whenTrue && thenWeight < thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.LE -> {
                    if (whenTrue && thenWeight > thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }
            }
        }
        return true
    }

    val Map.Entry<ReplacebleMaterialIDs, Map<MaterialIDs, RecipeRelation>>.relationValue: Pair<DoubleRange, DoubleRange>
        get() {
            val ids = this.key
            val materials = materials
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val replaceRate = if (ids.replaceIds == usedIds) ids.replaceRate ?: 1.0 else 1.0

            var usedMinNormalWeight = 0.0
            var usedMaxNormalWeight = 0.0
            var usedMinOverdoseWeight = 0.0
            var usedMaxOverdoseWeight = 0.0
            this.value.forEach { (materialIDs, recipeRelation) ->
                val normal = recipeRelation.normal
                val overdose = recipeRelation.overdose
                val weight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.weight }
                val normalWeight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.normalWeight }

                if (normalWeight > 0) {
                    usedMinNormalWeight += normalWeight * normal.min * replaceRate
                    usedMaxNormalWeight += normalWeight * normal.max * replaceRate
                    if (overdose != null) {
                        usedMinOverdoseWeight += normalWeight * overdose.min * replaceRate
                        usedMaxOverdoseWeight += normalWeight * overdose.max * replaceRate
                    }
                } else {
                    usedMinNormalWeight += weight * normal.min * replaceRate
                    usedMaxNormalWeight += weight * normal.max * replaceRate
                    if (overdose != null) {
                        usedMinOverdoseWeight += weight * overdose.min * replaceRate
                        usedMaxOverdoseWeight += weight * overdose.max * replaceRate
                    }
                }

                val overdoseMaterial = recipeRelation.overdoseMaterial
                val overdoseWeight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.overdoseWeight }
                if (overdoseMaterial != null && overdoseWeight > 0) {
                    val overdoseMaterialNormal = overdoseMaterial.normal
                    val overdoseMaterialOverdose = overdoseMaterial.overdose
                    usedMinNormalWeight += overdoseWeight * overdoseMaterialNormal.min * replaceRate
                    usedMaxNormalWeight += overdoseWeight * overdoseMaterialNormal.max * replaceRate
                    if (overdoseMaterialOverdose != null) {
                        usedMinOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.min * replaceRate
                        usedMaxOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.max * replaceRate
                    }
                }
            }

            return DoubleRange(usedMinNormalWeight, usedMaxNormalWeight) to DoubleRange(usedMinOverdoseWeight, usedMaxOverdoseWeight)
        }
}
