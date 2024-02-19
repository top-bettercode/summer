package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType
import top.bettercode.summer.tools.recipe.RecipeSolver.prepare
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.result.Recipe
import top.bettercode.summer.tools.recipe.result.RecipeResult

object MultiRecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(MultiRecipeSolver::class.java)

    fun solve(solverType: SolverType,
              requirement: RecipeRequirement,
              maxResult: Int = 1,
              materialUnchanged: Boolean = true,
              nutrientUnchanged: Boolean = true,
              includeProductionCost: Boolean = true
    ): RecipeResult {
        SolverFactory.createSolver(solverType = solverType, logging = true).use { solver ->
            solver.apply {
                val s = System.currentTimeMillis()
                val prepareData = prepare(requirement, includeProductionCost)
                var e = System.currentTimeMillis()
                val recipeResult = RecipeResult(name)
                while ((e - s) / 1000 < requirement.timeout
                        && recipeResult.recipes.size < maxResult) {
                    if (SolverType.COPT == solverType) {
                        val numVariables = numVariables()
                        val numConstraints = numConstraints()
                        log.info("=========变量数量：{},约束数量：{}", numConstraints, numConstraints)
                        if (numVariables > 2000 || numConstraints > 2000) {
                            log.error("变量或约束过多，变量数量：$numVariables 约束数量：$numConstraints")
                        }
                        (this as top.bettercode.summer.tools.optimal.solver.COPTSolver).model.writeMps("build/test.mps")
                    }
                    // 求解
                    solve()
                    if (isOptimal()) {

                        val objective = prepareData.objective
                        val recipeMaterials = prepareData.recipeMaterials

                        // 约束
                        val first = recipeResult.recipes.isEmpty()
                        val useMaterials: MutableMap<String, RecipeMaterialValue> = HashMap()
                        val recipe = Recipe(requirement = requirement,
                                includeProductionCost = includeProductionCost,
                                optimalProductionCost = requirement.productionCost.computeFee(
                                        materialItems = prepareData.materialItems?.map { CarrierValue(it.it, it.value.value) },
                                        dictItems = prepareData.dictItems?.mapValues { CarrierValue(it.value.it, it.value.value.value) }),
                                cost = objective.value.scale(),
                                materials = recipeMaterials.mapNotNull { (t, u) ->
                                    val value = u.weight.value
                                    if (value != 0.0) {
                                        val material = u.toMaterialValue()
                                        if (first) {
                                            useMaterials[t] = material
                                        }
                                        material
                                    } else {
                                        null
                                    }
                                })
                        recipeResult.addRecipe(recipe)

                        val cost = objective.value
                        if (first) {
                            // 后续配方原料不变
                            if (materialUnchanged) {
                                recipeMaterials.forEach { (id, material) ->
                                    if (useMaterials.contains(id)) {
                                        material.weight.gt(0.0)
                                    } else {
                                        material.weight.eq(0.0)
                                    }
                                }
                            }
                            // 养份保持不变 总养份
                            if (nutrientUnchanged) {
                                val totalNutrient = recipe.materials.sumOf { m ->
                                    m.totalNutrient()
                                }
                                recipeMaterials.map { (_, material) ->
                                    val value = material.totalNutrient()
                                    material.weight * value
                                }.eq(totalNutrient)
                            }
                        }
                        // 添加价格约束，约束下一个解的范围
                        // 前十个每3元价差一推，后十个每5元价差一推。
                        recipeMaterials.map { (_, material) ->
                            val price = material.price
                            material.weight * (price)
                        }.ge(cost + if (recipeResult.recipes.size < 10) 3 else 5)
                    } else {
                        log.error("Could not find optimal solution:${getResultStatus()}")
                        e = System.currentTimeMillis()
                        recipeResult.time = e - s
                        return recipeResult
                    }
                    e = System.currentTimeMillis()
                }
                e = System.currentTimeMillis()
                recipeResult.time = e - s
                return recipeResult
            }
        }
    }
}
