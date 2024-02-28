package top.bettercode.summer.tools.optimal.cplex

import ilog.cplex.IloCplex
import org.junit.jupiter.api.Test

class CplexTest {

    @Test
    fun test() {
        CplexNativeLibLoader.loadNativeLib()
        // 创建一个 MILP 模型
        IloCplex().use { model ->
            model.setParam(IloCplex.Param.MIP.Display, 0) // 0表示禁用日志，1表示启用

            // 添加变量
            val x1 = model.numVar(0.0, 100.0, "x1")
            val x2 = model.numVar(0.0, 100.0, "x2")

            // 添加 if 条件约束
            val ge = model.ge(x1, 10.0)
            val ge1 = model.ge(x2, 20.0)
            val iloConstraint = model.ifThen(ge, ge1)
            model.add(iloConstraint)
            model.addGe(x1, 10.0)
            // 添加目标函数
            val objective = model.linearNumExpr()
            objective.addTerm(1.0, x1)
            objective.addTerm(1.0, x2)
            model.addMinimize(objective)


            // 求解模型
            model.solve()

            // 获取解
            println("obj = " + model.objValue)
            println("x1 = " + model.getValue(x1))
            println("x2 = " + model.getValue(x2))
        }
    }
}