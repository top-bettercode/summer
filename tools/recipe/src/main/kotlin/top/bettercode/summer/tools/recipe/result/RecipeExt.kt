package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.material.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue

/**
 *
 * @author Peter Wu
 */
class RecipeExt(private val recipe: Recipe) {

    val RecipeMaterialValue.range: DoubleRange?
        get() {
            return recipe.requirement.materialRangeConstraints.filter { it.key.contains(this.id) }.values.firstOrNull()
        }

    val RecipeMaterialValue.double: Boolean
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return false
            val iDs = relationMap.keys.first { it.contains(this.id) }
            val recipeRelation = relationMap[iDs]
            return recipeRelation?.overdose != null || recipeRelation?.overdoseMaterial != null
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

    val RecipeMaterialValue.relationRate: RecipeRelation?
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.values.find { it.keys.any { m -> m.contains(this.id) } }
                    ?: return null
            val iDs = relationMap.keys.first { it.contains(this.id) }
            val recipeRelation = relationMap[iDs]

            return recipeRelation?.replaceRate(replaceRate)
        }


    val RecipeMaterialValue.relationValue: Pair<DoubleRange, DoubleRange>?
        get() {

            val relationRate = relationRate
            if (relationRate == null) {
                val materialRelationConstraints = recipe.requirement.materialRelationConstraints.filter { it.key.contains(this.id) }
                val entry = materialRelationConstraints.entries.firstOrNull() ?: return null
                recipe.apply {
                    return entry.relationValue
                }
            } else {
                var usedMinNormalWeight = 0.0
                var usedMaxNormalWeight = 0.0
                var usedMinOverdoseWeight = 0.0
                var usedMaxOverdoseWeight = 0.0

                val normal = relationRate.normal
                val overdose = relationRate.overdose
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

                val overdoseMaterial = relationRate.overdoseMaterial
                if (overdoseMaterial != null && overdoseWeight > 0) {
                    val overdoseMaterialNormal = overdoseMaterial.normal
                    val overdoseMaterialOverdose = overdoseMaterial.overdose
                    //在过量中显示
                    usedMinOverdoseWeight += overdoseWeight * overdoseMaterialNormal.min * replaceRate
                    usedMaxOverdoseWeight += overdoseWeight * overdoseMaterialNormal.max * replaceRate
                    if (overdoseMaterialOverdose != null) {
                        usedMinOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.min * replaceRate
                        usedMaxOverdoseWeight += overdoseWeight * overdoseMaterialOverdose.max * replaceRate
                    }
                }

                return DoubleRange(usedMinNormalWeight, usedMaxNormalWeight) to DoubleRange(usedMinOverdoseWeight, usedMaxOverdoseWeight)
            }
        }

}