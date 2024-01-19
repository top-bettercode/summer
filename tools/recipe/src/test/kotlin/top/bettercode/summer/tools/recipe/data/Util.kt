package top.bettercode.summer.tools.recipe.data

import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.RecipeRelation
import top.bettercode.summer.tools.recipe.material.MaterialIDs

/**
 *
 * @author Peter Wu
 */

/**
 * 耗硫酸约束
 */
val RecipeRequirement.sulfuricAcidRelation: Map<MaterialIDs, RecipeRelation>?
    get() {
        val sulfuricAcid = materials.sulfuricAcid
        return if (sulfuricAcid == null) {
            null
        } else
            materialRelationConstraints.filter { it.key.contains(sulfuricAcid.id) }.values.firstOrNull()
    }

/**
 * 耗液氨约束
 */
val RecipeRequirement.liquidAmmoniaRelation: Map<MaterialIDs, RecipeRelation>?
    get() {
        val liquidAmmonia = materials.liquidAmmonia
        return if (liquidAmmonia == null) {
            null
        } else
            materialRelationConstraints.filter {
                it.key.contains(liquidAmmonia.id)
            }.values.firstOrNull()
    }
