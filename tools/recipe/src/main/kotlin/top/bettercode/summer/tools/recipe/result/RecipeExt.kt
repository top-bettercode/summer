package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.recipe.criteria.DoubleRange
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs.Companion.toMaterialIDs
import top.bettercode.summer.tools.recipe.material.id.RelationMaterialIDs

/**
 *
 * @author Peter Wu
 */
class RecipeExt(private val recipe: Recipe) {

    /**
     * 是否存在过量消息
     */
    val RecipeMaterialValue.hasOverdose: Boolean
        get() {
            val relationMap = recipe.requirement.materialRelationConstraints.find { it.then.any { m -> m.term.contains(this.id) } }
                    ?: return false
            val recipeRelation = relationMap.then.first().then
            return recipeRelation.overdose != null || recipeRelation.overdoseMaterial != null
        }

    /**
     * 消耗原料ID
     */
    val RecipeMaterialValue.relationName: String?
        get() {
            val materialRelationConstraints = recipe.requirement.materialRelationConstraints.filter { it.then.any { m -> m.term.contains(this.id) } }
            val ids = materialRelationConstraints.firstOrNull() ?: return null

            val materials = recipe.materials
            val usedIds = materials.filter { ids.term.contains(it.id) }.map { it.id }.toMaterialIDs()
            return usedIds.toString()
        }

    /**
     * 消息原料替换比率
     */
    private val RecipeMaterialValue.replaceRate: Double
        get() {
            val ids = recipe.requirement.materialRelationConstraints.firstOrNull { it.term.any { m -> m.contains(this.id) } }?.term
                    ?: return 1.0

            val usedIds = recipe.materials.filter { ids.contains(it.id) }.map { it.id }.toMaterialIDs()
            return if (ids.replaceIds == usedIds) ids.replaceRate ?: 1.0 else 1.0
        }


    val RecipeMaterialValue.recipeRelationPair: Pair<RelationMaterialIDs, RecipeRelation>?
        get() {
            val entry = recipe.requirement.materialRelationConstraints.flatMap { it.then }.firstOrNull { it.term.contains(this.id) }
                    ?: return null

            return entry.term to entry.then.replaceRate(replaceRate)
        }


    val RecipeMaterialValue.relationValue: Pair<DoubleRange, DoubleRange>?
        get() {

            val recipeRelationPair = recipeRelationPair
            if (recipeRelationPair == null) {
                //消耗汇总
                val materialRelationConstraint = recipe.requirement.materialRelationConstraints.firstOrNull { it.term.contains(this.id) }
                        ?: return null
                recipe.apply {
                    return materialRelationConstraint.relationValue
                }
            } else {
                var usedMinNormalWeight = 0.0
                var usedMaxNormalWeight = 0.0
                var usedMinOverdoseWeight = 0.0
                var usedMaxOverdoseWeight = 0.0
                val relationIds = recipeRelationPair.first.relationIds
                val recipeRelation = recipeRelationPair.second
                val normal = recipeRelation.normal
                val overdose = recipeRelation.overdose
                var normalWeight = normalWeight(relationIds)
                if (normalWeight == 0.0) {
                    normalWeight = weight
                }

                if (normal != null) {
                    usedMinNormalWeight += normalWeight * normal.min
                    usedMaxNormalWeight += normalWeight * normal.max
                }
                if (overdose != null) {
                    usedMinOverdoseWeight += normalWeight * overdose.min
                    usedMaxOverdoseWeight += normalWeight * overdose.max
                }
                //过量原料
                var usedMinOverdoseMaterialWeight = 0.0
                var usedMaxOverdoseMaterialWeight = 0.0
                val overdoseWeight = overdoseWeight(relationIds)
                val overdoseMaterial = recipeRelation.overdoseMaterial
                if (overdoseMaterial != null && overdoseWeight > 0) {
                    val overdoseMaterialNormal = overdoseMaterial.normal
                    val overdoseMaterialOverdose = overdoseMaterial.overdose
                    if (overdoseMaterialNormal != null) {
                        usedMinOverdoseMaterialWeight += overdoseWeight * overdoseMaterialNormal.min
                        usedMaxOverdoseMaterialWeight += overdoseWeight * overdoseMaterialNormal.max
                    }
                    if (overdoseMaterialOverdose != null) {
                        usedMinOverdoseMaterialWeight += overdoseWeight * overdoseMaterialOverdose.min
                        usedMaxOverdoseMaterialWeight += overdoseWeight * overdoseMaterialOverdose.max
                    }
                }
                if (usedMinOverdoseWeight == 0.0) {
                    usedMinOverdoseWeight = usedMinOverdoseMaterialWeight
                }
                if (usedMaxOverdoseWeight == 0.0) {
                    usedMaxOverdoseWeight = usedMaxOverdoseMaterialWeight
                }

                return DoubleRange(usedMinNormalWeight, usedMaxNormalWeight) to DoubleRange(usedMinOverdoseWeight, usedMaxOverdoseWeight)
            }
        }

}