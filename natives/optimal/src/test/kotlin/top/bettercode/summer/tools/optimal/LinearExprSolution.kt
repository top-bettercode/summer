package top.bettercode.summer.tools.optimal

import com.google.ortools.Loader
import com.google.ortools.sat.*

object LinearExprSolution {
    @JvmStatic
    fun main(args: Array<String>) {
        Loader.loadNativeLibraries()

        // 创建CpModel
        val model = CpModel()

        // 创建一组整数变量
        val numVariables = 5
        val variables = arrayOfNulls<IntVar>(numVariables)
        for (i in 0 until numVariables) {
            variables[i] = model.newIntVar(0, 10, "Var_$i")
        }

        // 设置一组变量的和大于等于一个给定的数
        val targetSum = 20
        val sumExpr = LinearExpr.weightedSum(variables, variables.map { 1L }.toLongArray()) // 创建一个表示变量和的线性表达式
        model.addGreaterOrEqual(sumExpr, targetSum.toLong()) // 添加约束

        // 创建CpSolver并求解模型
        val solver = CpSolver()
        val status = solver.solve(model)

        // 打印结果
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            println("Solution found:")
            for (i in 0 until numVariables) {
                println(variables[i]!!.name + " = " + solver.value(variables[i]))
            }
            println("Sum: " + solver.value(sumExpr))
        } else {
            println("No solution found.")
        }
    }
}
