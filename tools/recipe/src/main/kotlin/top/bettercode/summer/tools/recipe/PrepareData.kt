package top.bettercode.summer.tools.recipe

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.recipe.material.RecipeMaterialVar
import top.bettercode.summer.tools.recipe.material.RecipeOtherMaterial
import top.bettercode.summer.tools.recipe.productioncost.Cost
import top.bettercode.summer.tools.recipe.productioncost.DictType
import top.bettercode.summer.tools.recipe.result.Recipe

/**
 *
 * @author Peter Wu
 */
data class PrepareData(
    val defaultRecipeName: String,
    val requirement: RecipeRequirement,
    val includeProductionCost: Boolean,
    val minMaterialNum: Boolean,
    val recipeMaterials: Map<String, RecipeMaterialVar>,
    val objectiveVars: List<IVar>,
    val materialItems: List<CarrierValue<RecipeOtherMaterial, IVar>>?,
    val dictItems: Map<DictType, CarrierValue<Cost, IVar>>?,
) {

    private val log = LoggerFactory.getLogger(PrepareData::class.java)

    @JvmOverloads
    fun solve(solver: Solver, recipeName: String? = null): Recipe? {
        solver.apply {
            val minimize = objectiveVars.minimize()
            solve()
            if (!isOptimal()) {
                log.warn("Could not find optimal solution:${getResultStatus()}")
                return null
            }

            val objectiveValue = minimize.value
            if (minMaterialNum) {
                //固定成本
                objectiveVars.le(objectiveValue)

                //使用最小数量原料
                recipeMaterials.values.map {
                    val intVar = intVar(0.0, 1.0)
                    intVar.geConst(1.0)
                        .onlyEnforceIf(it.weight.gtConst(0.0))
                    intVar
                }.minimize()
                solve()
                if (!isOptimal()) {
                    log.warn("Could not find optimal solution:${getResultStatus()}")
                    return null
                }
            }
            val materials = recipeMaterials.mapNotNull { (_, u) ->
                val value = u.weight.value
                if (value != 0.0) {
                    u.toMaterialValue()
                } else {
                    null
                }
            }
            return Recipe(
                recipeName = recipeName ?: defaultRecipeName,
                requirement = requirement,
                includeProductionCost = includeProductionCost,
                optimalProductionCost = requirement.productionCost.computeFee(
                    materialItems?.map { CarrierValue(it.it, it.value.value) },
                    dictItems?.mapValues { CarrierValue(it.value.it, it.value.value.value) }),
                cost = objectiveValue,
                materials = materials
            )
        }
    }

}