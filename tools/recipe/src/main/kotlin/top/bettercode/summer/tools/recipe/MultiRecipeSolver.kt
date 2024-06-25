package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.SolverType
import top.bettercode.summer.tools.recipe.RecipeSolver.prepare
import top.bettercode.summer.tools.recipe.material.RecipeMaterialValue
import top.bettercode.summer.tools.recipe.result.RecipeResult

object MultiRecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(MultiRecipeSolver::class.java)

    fun solve(
        solver: Solver,
        requirement: RecipeRequirement,
        maxResult: Int = 1,
        /**
         * 结果原料不变
         */
        materialUnchanged: Boolean = true,
        /**
         * 养分含量不变
         */
        nutrientUnchanged: Boolean = true,
        includeProductionCost: Boolean = true,
        /**
         * 是否使用最小数量原料
         */
        minMaterialNum: Boolean = true
    ): RecipeResult {
        solver.use { so ->
            so.apply {
                val s = System.currentTimeMillis()
                val prepareData = prepare(requirement, includeProductionCost, minMaterialNum)
                var e = System.currentTimeMillis()
                val recipeResult = RecipeResult(requirement = requirement, solverName = name)
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
                    val recipe =
                        prepareData.solve(this, "${so.name}-${recipeResult.recipes.size + 1}")
                    if (recipe != null) {
                        recipeResult.addRecipe(recipe)
                        val recipeMaterials = prepareData.recipeMaterials
                        val first = recipeResult.recipes.isEmpty()
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
                                val totalNutrientWeight = recipe.materials.sumOf { m ->
                                    m.totalNutrientWeight
                                }
                                recipeMaterials.map { (_, material) ->
                                    material.weight * material.totalNutrient
                                }.eq(totalNutrientWeight)
                            }
                        }
                        // 添加价格约束，约束下一个解的范围
                        // 前十个每3元价差一推，后十个每5元价差一推。
                        val cost = recipe.cost
                        prepareData.objectiveVars.ge(cost + if (recipeResult.recipes.size < 10) 3 else 5)
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
