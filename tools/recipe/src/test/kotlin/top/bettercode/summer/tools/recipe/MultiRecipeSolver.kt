package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.recipe.RecipeSolver.prepare
import top.bettercode.summer.tools.recipe.data.RecipeResult
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue

object MultiRecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(MultiRecipeSolver::class.java)

    fun solve(solver: Solver, requirement: RecipeRequirement, maxResult: Int = 1): RecipeResult {
        solver.apply {
            val s = System.currentTimeMillis()
            val (recipeMaterials, objective) = prepare(requirement)
            var e = System.currentTimeMillis()
            val recipeResult = RecipeResult(name)
            log.trace("==================================================")
            while ((e - s) / 1000 < requirement.timeout
                    && recipeResult.recipeCount < maxResult) {
                // 求解
                solve()
                log.trace(
                        "solve times: " + recipeResult.solveCount + " 耗时：" + (e - s) + "ms " + "变量数量："
                                + numVariables() + " 约束数量："
                                + numConstraints())
                if (numVariables() > 2000 || numConstraints() > 2000) {
                    log.error("变量或约束过多，变量数量：" + numVariables() + " 约束数量：" + numConstraints())
                    return recipeResult
                }
                if (isOptimal()) {
                    recipeResult.addSolveCount()
                    // 约束
                    val first = recipeResult.recipes.isEmpty()
                    val useMaterials: MutableMap<String, RecipeMaterialValue> = HashMap()
                    val recipe = Recipe(requirement, objective.value.scale(),
                            recipeMaterials.mapNotNull { (t, u) ->
                                val value = u.solutionVar.value
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

                    val recipeCount = recipeResult.recipeCount
                    log.trace("====================solve size: $recipeCount")
                    val cost = objective.value
                    if (first) {
                        // 后续配方原料不变
                        recipeMaterials.forEach { (id, material) ->
                            if (useMaterials.contains(id)) {
                                material.solutionVar.gt(0.0)
                            } else {
                                material.solutionVar.eq(0.0)
                            }
                        }
                        // 养份保持不变 总养份
                        val totalNutrient = recipe.materials.sumOf { m ->
                            m.totalNutrient()
                        }

                        recipeMaterials.map { (_, material) ->
                            val value = material.totalNutrient()
                            material.solutionVar.coeff(value)
                        }.toTypedArray().eq(totalNutrient)
                    }
                    // 添加价格约束，约束下一个解的范围
                    // 前十个每3元价差一推，后十个每5元价差一推。
                    recipeMaterials.map { (_, material) ->
                        val price = material.price
                        material.solutionVar.coeff(price)
                    }.toTypedArray().ge(cost + if (recipeCount < 10) 3 else 5)
                } else {
                    log.error("Could not find optimal solution:${getResultStatus()}")
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
