package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.material.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue

/**
 *
 * @author Peter Wu
 */
class RecipeExt(private val recipe: Recipe) {

    val RecipeMaterialValue.minWeight: Double?
        get() {
            return recipe.requirement.materialRangeConstraints.filter { it.key.contains(this.id) }.values.firstOrNull()?.min
        }
    val RecipeMaterialValue.maxWeight: Double?
        get() {
            return recipe.requirement.materialRangeConstraints.filter { it.key.contains(this.id) }.values.firstOrNull()?.max
        }

    val RecipeMaterialValue.double: Boolean
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return false
            val iDs = relationMap.keys.first { it.contains(this.id) }
            return relationMap[iDs]?.overdose != null
        }

    val RecipeMaterialValue.minNormalRelationRate: Double?
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return null
            val iDs = relationMap.keys.first { it.contains(this.id) }
            return relationMap[iDs]?.normal?.min?.apply { this * replaceRate }
        }
    val RecipeMaterialValue.maxNormalRelationRate: Double?
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return null
            val iDs = relationMap.keys.first { it.contains(this.id) }
            return relationMap[iDs]?.normal?.max?.apply { this * replaceRate }
        }
    val RecipeMaterialValue.minOverdoseRelationRate: Double?
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return null
            val iDs = relationMap.keys.first { it.contains(this.id) }
            return relationMap[iDs]?.overdose?.min?.apply { this * replaceRate }
        }
    val RecipeMaterialValue.maxOverdoseRelationRate: Double?
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return null
            val iDs = relationMap.keys.first { it.contains(this.id) }
            return relationMap[iDs]?.overdose?.max?.apply { this * replaceRate }
        }

    val RecipeMaterialValue.minNormalRelationValue: Double?
        get() {
            val minNormalRelationRate = minNormalRelationRate
            return if (minNormalRelationRate == null) {
                val relationValue = relationValue
                if (double) {
                    relationValue?.first?.min
                } else {
                    if (relationValue == null) {
                        null
                    } else {
                        relationValue.first.min + relationValue.second.min
                    }
                }
            } else {
                if (normalWeight == 0.0) {
                    weight * minNormalRelationRate
                } else
                    normalWeight * minNormalRelationRate
            }
        }
    val RecipeMaterialValue.maxNormalRelationValue: Double?
        get() {
            val maxNormalRelationRate = maxNormalRelationRate
            return if (maxNormalRelationRate == null) {
                val relationValue = relationValue
                if (double) {
                    relationValue?.first?.max
                } else {
                    if (relationValue == null) {
                        null
                    } else {
                        relationValue.first.max + relationValue.second.max
                    }
                }
            } else {
                if (normalWeight == 0.0) {
                    weight * maxNormalRelationRate
                } else
                    normalWeight * maxNormalRelationRate
            }
        }
    val RecipeMaterialValue.minOverdoseRelationValue: Double?
        get() {
            val minOverdoseRelationRate = minOverdoseRelationRate
            return if (minOverdoseRelationRate == null) {
                relationValue?.second?.min
            } else {
                if (overdoseWeight == 0.0) {
                    weight * minOverdoseRelationRate
                } else
                    overdoseWeight * minOverdoseRelationRate
            }
        }
    val RecipeMaterialValue.maxOverdoseRelationValue: Double?
        get() {
            val maxOverdoseRelationRate = maxOverdoseRelationRate
            return if (maxOverdoseRelationRate == null) {
                relationValue?.second?.max
            } else {
                if (overdoseWeight == 0.0) {
                    weight * maxOverdoseRelationRate
                } else
                    overdoseWeight * maxOverdoseRelationRate
            }
        }

    val RecipeMaterialValue.relationName: String?
        get() {
            val materialRelationConstraints = recipe.requirement.materialRelationConstraints.filter { it.value.keys.any { m -> m.contains(this.id) } }
            val ids = materialRelationConstraints.keys.firstOrNull() ?: return null

            val materials = recipe.materials
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            return usedIds.toString()
        }

    private val RecipeMaterialValue.replaceRate: Double
        get() {
            val materialRelationConstraints = recipe.requirement.materialRelationConstraints.filter { it.value.keys.any { m -> m.contains(this.id) } }
            val ids = materialRelationConstraints.keys.firstOrNull() ?: return 1.0

            val materials = recipe.materials
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            return if (ids.replaceIds == usedIds) ids.replaceRate ?: 1.0 else 1.0
        }

    private val RecipeMaterialValue.relationValue: Pair<DoubleRange, DoubleRange>?
        get() {
            val materialRelationConstraints = recipe.requirement.materialRelationConstraints.filter { it.key.contains(this.id) }
            val ids = materialRelationConstraints.keys.firstOrNull() ?: return null

            val materials = recipe.materials
            val usedIds = materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            val replaceRate = if (ids.replaceIds == usedIds) ids.replaceRate ?: 1.0 else 1.0

            var usedMinNormalWeights = 0.0
            var usedMaxNormalWeights = 0.0
            var usedMinOverdoseWeights = 0.0
            var usedMaxOverdoseWeights = 0.0
            val relation = materialRelationConstraints.values.firstOrNull()
            relation?.forEach { (materialIDs, recipeRelation) ->
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