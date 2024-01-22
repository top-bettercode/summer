package top.bettercode.summer.tools.recipe.result

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        /** 选用的原料  */
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
        if ((trueCost - cost).scale() !in -1e-9..1e-9) {
            log.warn("配方成本不匹配:{} / {}", trueCost, cost)
            return false
        }
        //检查烘干水分
        if (dryWater < -1e-10) {
            log.warn("配方烘干水分异常：{}", dryWater)
            return false
        }
        if (requirement.maxBakeWeight >= 0 && (dryWater - requirement.maxBakeWeight).scale() > 1e-10) {
            log.warn("配方烘干水分:{} 超过最大可烘干水分：{}", dryWater, requirement.maxBakeWeight)
            return false
        }
        val totalWater = materials.sumOf {
            it.waterWeight
        }.scale()
        if ((dryWater - totalWater).scale() > 1e-10) {
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
        // 限用原料ID
        val useMaterials = requirement.useMaterials
        if (useMaterials.isNotEmpty()) {
            if (!useMaterials.containsAll(usedMaterials)) {
                log.warn("配方所用原料：{} 不在范围{}内", usedMaterials, useMaterials)
                return false
            }
        }
        // 不使用的原料ID
        val noUseMaterials = requirement.noUseMaterials
        if (noUseMaterials.isNotEmpty()) {
            if (usedMaterials.any { noUseMaterials.contains(it) }) {
                log.warn("配方所用原料：{} 包含不可用物料{}内", usedMaterials, noUseMaterials)
                return false
            }
        }
        // 不能混用的原料,value: 原料ID
        val notMixMaterials = requirement.notMixMaterials
        for (notMixMaterial in notMixMaterials) {
            val mixMaterials = notMixMaterial.map { notMix -> usedMaterials.filter { notMix.contains(it) } }.filter { it.isNotEmpty() }
            val size = mixMaterials.size
            if (size > 1) {
                log.warn("配方混用原料：{}", mixMaterials.joinToString(",", "[", "]") { it.joinToString("、") })
                return false
            }
        }

        // 原料约束,key:原料ID, value: 原料使用范围约束
        for ((ids, range) in materialRangeConstraints) {
            val weight = materials.filter { ids.contains(it.id) }.sumOf { it.weight }
            if (weight !in range.min..range.max) {
                log.warn("原料{}使用量：{} 不在范围{}-{}内", ids, weight, range.min, range.max)
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
            if ((usedWeight - usedAddWeight).scale() !in -1e-10..1e-10) {
                log.warn("原料{}使用量：{} 不等于:{} = 正常使用量：{}+过量使用量：{}", usedIds, usedWeight, usedAddWeight, usedNormalWeight, usedOverdoseWeight)
                return false
            }
            val (normal, overdose) = entry.relationValue
            val usedMinNormalWeights = normal.min
            val usedMaxNormalWeights = normal.max
            val usedMinOverdoseWeights = overdose.min
            val usedMaxOverdoseWeights = overdose.max

            // usedNormalWeight 必须在 usedMinNormalWeights usedMaxNormalWeights范围内
            if (usedNormalWeight !in (usedMinNormalWeights - 1e-10).scale()..(usedMaxNormalWeights + 1e-10).scale()) {
                log.warn("原料{}正常使用量：{} 不在范围{}-{}内", usedIds, usedNormalWeight, usedMinNormalWeights, usedMaxNormalWeights)
                return false
            }

            // usedOverdoseWeight 必须在 usedMinOverdoseWeights usedMaxOverdoseWeights范围内
            if (usedOverdoseWeight !in (usedMinOverdoseWeights - 1e-10).scale()..(usedMaxOverdoseWeights + 1e-10).scale()) {
                log.warn("原料{}过量使用量：{} 不在范围{}-{}内", usedIds, usedOverdoseWeight, usedMinOverdoseWeights, usedMaxOverdoseWeights)
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
                Operator.EQUAL -> {
                    whenTrue = whenWeight == whenCon.condition.value
                }

                Operator.NOT_EQUAL -> {
                    whenTrue = whenWeight != whenCon.condition.value
                }

                Operator.GREATER -> {
                    whenTrue = whenWeight > whenCon.condition.value
                }

                Operator.LESS -> {
                    whenTrue = whenWeight < whenCon.condition.value
                }

                Operator.GREATER_EQUAL -> {
                    whenTrue = whenWeight >= whenCon.condition.value
                }

                Operator.LESS_EQUAL -> {
                    whenTrue = whenWeight <= whenCon.condition.value
                }
            }
            when (thenCon.condition.operator) {
                Operator.EQUAL -> {
                    if (whenTrue && thenWeight != thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.NOT_EQUAL -> {
                    if (whenTrue && thenWeight == thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.GREATER -> {
                    if (whenTrue && thenWeight <= thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.LESS -> {
                    if (whenTrue && thenWeight >= thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false

                    }
                }

                Operator.GREATER_EQUAL -> {
                    if (whenTrue && thenWeight < thenCon.condition.value) {
                        log.warn("条件约束：当{}时，{}不成立:{}", whenCon, thenCon, MaterialCondition(thenCon.materials, RecipeCondition(value = thenWeight)))
                        return false
                    }
                }

                Operator.LESS_EQUAL -> {
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

            var usedMinNormalWeights = 0.0
            var usedMaxNormalWeights = 0.0
            var usedMinOverdoseWeights = 0.0
            var usedMaxOverdoseWeights = 0.0
            this.value.forEach { (materialIDs, recipeRelation) ->
                val normal = recipeRelation.normal
                val weight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.weight }
                val normalWeight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.normalWeight }

                if (normalWeight > 0) {
                    usedMinNormalWeights += normalWeight * normal.min * replaceRate
                    usedMaxNormalWeights += normalWeight * normal.max * replaceRate
                } else {
                    usedMinNormalWeights += weight * normal.min * replaceRate
                    usedMaxNormalWeights += weight * normal.max * replaceRate
                }
                val overdose = recipeRelation.overdose
                if (overdose != null) {
                    val overdoseWeight = materials.filter { materialIDs.contains(it.id) }.sumOf { it.overdoseWeight }
                    if (overdoseWeight > 0) {
                        usedMinOverdoseWeights += overdoseWeight * overdose.min * replaceRate
                        usedMaxOverdoseWeights += overdoseWeight * overdose.max * replaceRate
                    } else {
                        usedMinOverdoseWeights += weight * overdose.min * replaceRate
                        usedMaxOverdoseWeights += weight * overdose.max * replaceRate
                    }
                }
            }

            return DoubleRange(usedMinNormalWeights, usedMaxNormalWeights) to DoubleRange(usedMinOverdoseWeights, usedMaxOverdoseWeights)
        }
}
