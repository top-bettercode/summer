package top.bettercode.summer.tools.optimal

import com.gurobi.gurobi.*
import org.junit.jupiter.api.Test


object GurobiTest {

    @Test
    fun test() {
        try {
            // 创建Gurobi环境
            val env = GRBEnv()
            env[GRB.IntParam.OutputFlag] = 0 // 设置输出日志的详细程度

            // 创建一个模型
            val model = GRBModel(env)

            // 创建决策变量
            val x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x")
            val y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y")


            val expr = GRBLinExpr()
            expr.addTerm(1.0, y)
            model.addGenConstrIndicator(x,  1,expr, GRB.GREATER_EQUAL, 1.0, "indicator_constraint")

            // 设置优化目标
            val objExpr = GRBLinExpr()
            objExpr.addTerm(1.0, x)
            objExpr.addTerm(2.0, y)
            model.setObjective(objExpr, GRB.MAXIMIZE)

            // 求解模型
            model.optimize()

            // 打印结果
            println("Optimal objective: " + model[GRB.DoubleAttr.ObjVal])
            println("x = " + x[GRB.DoubleAttr.X])
            println("y = " + y[GRB.DoubleAttr.X])

            // 释放资源
            model.dispose()
            env.dispose()
        } catch (e: GRBException) {
            e.printStackTrace()
        }
    }
}
