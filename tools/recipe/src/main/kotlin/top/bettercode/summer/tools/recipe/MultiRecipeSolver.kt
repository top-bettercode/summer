package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.SolverType
import top.bettercode.summer.tools.recipe.RecipeSolver.prepare
import top.bettercode.summer.tools.recipe.RecipeSolver.toRecipe
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.result.RecipeResult

object MultiRecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(MultiRecipeSolver::class.java)

    fun solve(
        solver: Solver,
        requirement: RecipeRequirement,
        maxResult: Int = 1,
        materialUnchanged: Boolean = true,
        nutrientUnchanged: Boolean = true,
        includeProductionCost: Boolean = true
    ): RecipeResult {
        solver.use { so ->
            so.apply {
                val s = System.currentTimeMillis()
                val prepareData = prepare(requirement, includeProductionCost)
                var e = System.currentTimeMillis()
                val recipeResult = RecipeResult(name)
                while ((e - s) / 1000 < requirement.timeout
                    && recipeResult.recipes.size < maxResult
                ) {
                    if (SolverType.COPT == so.type) {
                        val numVariables = numVariables()
                        val numConstraints = numConstraints()
                        log.info("=========变量数量：{},约束数量：{}", numConstraints, numConstraints)
                        if (numVariables > 2000 || numConstraints > 2000) {
                            log.error("变量或约束过多，变量数量：$numVariables 约束数量：$numConstraints")
                        }
                    }
                    // 求解
                    solve()
                    if (isOptimal()) {
                        val recipeMaterials = prepareData.recipeMaterials
                        // 约束
                        val recipe = prepareData.toRecipe(requirement, includeProductionCost)
                        val first = recipeResult.recipes.isEmpty()
                        recipeResult.addRecipe(recipe)
                        if (first) {
                            val useMaterials: Map<String, RecipeMaterialValue> =
                                recipe.materials.associateBy { it.id }
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
                                    material.weight * material.totalNutrient()
                                }.eq(totalNutrient)
                            }
                        }
                        // 添加价格约束，约束下一个解的范围
                        // 前十个每3元价差一推，后十个每5元价差一推。
                        val cost = recipe.cost
                        recipeMaterials.map { (_, material) ->
                            material.weight * material.price
                        }.ge(cost + if (recipeResult.recipes.size < 10) 3 else 5)
                    } else {
                        log.warn("Could not find optimal solution:${getResultStatus()}")
                        e = System.currentTimeMillis()
                        recipeResult.time = e - s
                        log.info("${requirement.productName} ${solver.name}求解耗时：" + (e - s) + "ms")
                        return recipeResult
                    }
                    e = System.currentTimeMillis()
                }
                e = System.currentTimeMillis()
                recipeResult.time = e - s
                log.info("${requirement.productName} ${solver.name}求解耗时：" + (e - s) + "ms")
                return recipeResult
            }
        }
    }
}
