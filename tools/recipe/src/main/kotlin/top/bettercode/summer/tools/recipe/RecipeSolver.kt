package top.bettercode.summer.tools.recipe

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.recipe.result.Recipe

object RecipeSolver {

    private val log: Logger = LoggerFactory.getLogger(RecipeSolver::class.java)

    @JvmStatic
    @JvmOverloads
    fun solve(
        solver: Solver,
        requirement: RecipeRequirement,
        includeProductionCost: Boolean = true,
        /**
         * 是否使用最小数量原料
         */
        minMaterialNum: Boolean = true,
        minEpsilon: Double = OptimalUtil.DEFAULT_EPSILON
    ): Recipe? {
        solver.apply {
            val s = System.currentTimeMillis()
            val prepareData = PrepareSolveData.of(
                this,
                requirement = requirement,
                includeProductionCost = includeProductionCost
            )
            triggerLimit()

            // 求解
            val solve = prepareData.solve(this, minMaterialNum, minEpsilon = minEpsilon)
            val e = System.currentTimeMillis()
            log.info("${requirement.productName} ${solver.name}求解耗时：" + (e - s) + "ms")
            return solve
        }
    }


}
