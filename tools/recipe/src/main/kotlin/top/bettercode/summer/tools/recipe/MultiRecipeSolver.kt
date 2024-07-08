package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.Solver
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
        minMaterialNum: Boolean = true,
        minEpsilon: Double = OptimalUtil.DEFAULT_EPSILON
    ): RecipeResult {
        solver.apply {
            val s = System.currentTimeMillis()
            var prepareData = PrepareSolveData.of(
                this,
                requirement = requirement,
                includeProductionCost = includeProductionCost
            )
            var eachMinMaterialNum = minMaterialNum
            var e = System.currentTimeMillis()
            val recipeResult = RecipeResult(requirement = requirement, solverName = name)
            while ((e - s) / 1000 < requirement.timeout
                && recipeResult.recipes.size < maxResult
            ) {
                // 求解
                val recipe =
                    prepareData.solve(
                        this,
                        eachMinMaterialNum,
                        "${name}-${recipeResult.recipes.size + 1}",
                        minEpsilon
                    )
                if (recipe != null) {
                    recipeResult.addRecipe(recipe)
                    eachMinMaterialNum = false
                    reset()
                    prepareData = PrepareSolveData.of(
                        this,
                        requirement = requirement,
                        includeProductionCost = includeProductionCost
                    )
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
                    e = System.currentTimeMillis()
                    recipeResult.time = e - s
                    log.info("${requirement.id}:${requirement.productName} ${solver.name}求解耗时：" + (e - s) + "ms")
                    return recipeResult
                }
                e = System.currentTimeMillis()
            }
            e = System.currentTimeMillis()
            recipeResult.time = e - s
            log.info("${requirement.id}:${requirement.productName} ${solver.name}求解耗时：" + (e - s) + "ms")
            return recipeResult
        }
    }
}
