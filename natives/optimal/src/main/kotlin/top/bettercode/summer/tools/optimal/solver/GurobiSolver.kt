package top.bettercode.summer.tools.optimal.solver

import com.gurobi.gurobi.*
import top.bettercode.summer.tools.optimal.solver.`var`.GurobiObjectiveVar
import top.bettercode.summer.tools.optimal.solver.`var`.GurobiVar
import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 * https://www.gurobi.com/documentation/11.0/refman/java_api_overview.html
 *
 * @author Peter Wu
 */
class GurobiSolver @JvmOverloads constructor(
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        logging: Boolean = false,
        name: String = "GurobiSolver"
) : Solver(name, epsilon) {

    private val env = GRBEnv()
    val model: GRBModel = GRBModel(env)

    init {
        model.set(GRB.IntParam.OutputFlag, if (logging) 1 else 0)
        model.set(GRB.IntParam.LogToConsole, if (logging) 1 else 0)
        model.set(GRB.DoubleParam.IntFeasTol, 1e-6)
        model.set(GRB.DoubleParam.FeasibilityTol, 1e-9)

//        model.set(GRB.DoubleParam.PerturbValue, 0.0)

        //Cutoff:1.0E100
        //IterationLimit:1.0E100
        //MemLimit:1.0E100
        //SoftMemLimit:1.0E100
        //NodeLimit:1.0E100
        //TimeLimit:1.0E100
        //WorkLimit:1.0E100
        //FeasibilityTol:1.0E-6
        //IntFeasTol:1.0E-5
        //MarkowitzTol:0.0078125
        //MIPGap:1.0E-4
        //MIPGapAbs:1.0E-10
        //OptimalityTol:1.0E-6
        //PerturbValue:2.0E-4
        //Heuristics:0.05
        //ObjScale:0.0
        //NodefileStart:1.0E100
        //BarConvTol:1.0E-8
        //BarQCPConvTol:1.0E-6
        //PSDTol:1.0E-6
        //ImproveStartGap:0.0
        //ImproveStartNodes:1.0E100
        //ImproveStartTime:1.0E100
        //FeasRelaxBigM:1000000.0
        //TuneTimeLimit:1.0E100
        //TuneCleanup:0.0
        //TuneTargetMIPGap:0.0
        //TuneTargetTime:0.005
        //PreSOS1BigM:-1.0
        //PreSOS2BigM:-1.0
        //PoolGap:1.0E100
        //PoolGapAbs:1.0E100
        //BestObjStop:-1.0E100
        //BestBdStop:1.0E100
        //CSQueueTimeout:-1.0
        //FuncPieceError:0.001
        //FuncPieceLength:0.01
        //FuncPieceRatio:-1.0
        //FuncMaxVal:1000000.0
        //NoRelHeurTime:0.0
        //NoRelHeurWork:0.0
        //WLSTokenRefresh:0.9
    }

    override fun setTimeLimit(seconds: Int) {
        model.set(GRB.DoubleParam.TimeLimit, seconds.toDouble())
    }

    override fun solve() {
        model.optimize()
    }

    override fun close() {
        model.dispose()
        env.dispose()
    }

    override fun clear() {
    }

    override fun isOptimal(): Boolean {
        return GRB.Status.OPTIMAL == getStatus()
    }

    override fun getResultStatus(): String {
        val status = getStatus()
        try {
            val fields = GRB.Status::class.java.fields
            for (field in fields) {
                if (field[null] == status) {
                    return field.name
                }
            }
        } catch (ignored: Exception) {
        }
        return "未知状态:$status"
    }

    private fun getStatus(): Int {
        return model.get(GRB.IntAttr.Status)
    }

    override fun numVariables(): Int {
        return model.get(GRB.IntAttr.NumVars)
    }

    override fun numConstraints(): Int {
        return model.get(GRB.IntAttr.NumConstrs)
    }

    private fun IVar.expr() = GRBLinExpr().also { it.addTerm(this.coeff, this.getDelegate()) }

    override fun boolVar(name: String?): IVar {
        val gurobiVar = GurobiVar(model.addVar(0.0, 1.0, 1.0, GRB.BINARY, name
                ?: ("b" + (numVariables() + 1))))
        model.update()
        return gurobiVar
    }

    override fun intVar(lb: Double, ub: Double, name: String?): IVar {
        val gurobiVar = GurobiVar(model.addVar(lb, ub, 1.0, GRB.INTEGER, name
                ?: ("i" + (numVariables() + 1))))
        model.update()
        return gurobiVar
    }

    override fun numVar(lb: Double, ub: Double, name: String?): IVar {
        val gurobiVar = GurobiVar(model.addVar(lb, ub, 1.0, GRB.CONTINUOUS, name
                ?: ("n" + (numVariables() + 1))))
        model.update()
        return gurobiVar
    }

    override fun IVar.plus(value: Double): IVar {
        val expr = GRBLinExpr()
        expr.addTerm(this.coeff, this.getDelegate())
        expr.addConstant(value)
        val sum = numVar()
        model.addConstr(expr, GRB.EQUAL, sum.getDelegate<GRBVar>(), "c" + (numConstraints() + 1))
        model.update()
        return sum
    }

    override fun IVar.ge(lb: Double) {
        model.addConstr(this.expr(), GRB.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.ge(lb: IVar) {
        model.addConstr(this.expr(), GRB.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.ge(lb: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.gt(lb: IVar) {
        val expr = this.expr()
        expr.addConstant(-epsilon)
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.gt(lb: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        expr.addConstant(-epsilon)
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        expr.addConstant(-epsilon)
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
        model.update()
    }


    override fun IVar.le(ub: Double) {
        model.addConstr(this.expr(), GRB.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.le(ub: IVar) {
        model.addConstr(this.expr(), GRB.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.le(ub: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.le(ub: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.lt(ub: IVar) {
        val expr = this.expr()
        expr.addConstant(epsilon)
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        expr.addConstant(epsilon)
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        expr.addConstant(epsilon)
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
        model.update()
    }


    override fun IVar.eq(value: Double) {
        model.addConstr(this.expr(), GRB.EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.eq(value: IVar) {
        model.addConstr(this.expr(), GRB.EQUAL, value.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.eq(value: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.eq(value: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.eq(value: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.EQUAL, value.expr(), "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Iterable<IVar>.eq(value: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.addConstr(expr, GRB.EQUAL, value.expr(), "c" + (numConstraints() + 1))
        model.update()
    }


    override fun IVar.between(lb: Double, ub: Double) {
        val size = numConstraints()
        model.addConstr(this.expr(), GRB.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(this.expr(), GRB.LESS_EQUAL, ub, "c" + (size + 2))
        model.update()
    }

    override fun IVar.between(lb: IVar, ub: IVar) {
        val size = numConstraints()
        model.addConstr(this.expr(), GRB.GREATER_EQUAL, lb.expr(), "c" + (size + 1))
        model.addConstr(this.expr(), GRB.LESS_EQUAL, ub.expr(), "c" + (size + 2))
        model.update()
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: Double, ub: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        val size = numConstraints()
        model.addConstr(expr, GRB.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr, GRB.LESS_EQUAL, ub, "c" + (size + 2))
        model.update()
    }

    override fun Iterable<IVar>.between(lb: Double, ub: Double) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        val size = numConstraints()
        model.addConstr(expr, GRB.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr, GRB.LESS_EQUAL, ub, "c" + (size + 2))
        model.update()
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: IVar, ub: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        val size = numConstraints()
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (size + 1))
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (size + 2))
        model.update()
    }

    override fun Iterable<IVar>.between(lb: IVar, ub: IVar) {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        val size = numConstraints()
        model.addConstr(expr, GRB.GREATER_EQUAL, lb.expr(), "c" + (size + 1))
        model.addConstr(expr, GRB.LESS_EQUAL, ub.expr(), "c" + (size + 2))
        model.update()
    }


    override fun IVar.geIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, this.expr(), GRB.GREATER_EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.geIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, this.expr(), GRB.GREATER_EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.leIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, this.expr(), GRB.LESS_EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.leIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, this.expr(), GRB.LESS_EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }


    override fun IVar.eqIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, this.expr(), GRB.EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.eqIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, this.expr(), GRB.EQUAL, value, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar) {
        val expr = this.expr()
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr, GRB.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr, GRB.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar) {
        val expr = this.expr()
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr, GRB.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr, GRB.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
        model.update()
    }

    override fun Array<out IVar>.sum(): IVar {
        val expr = GRBLinExpr()
        for (it in this) {
            expr.addTerm(it.coeff, it.getDelegate())
        }
        val sum = numVar()
        model.addConstr(expr, GRB.EQUAL, sum.expr(), "c" + (numConstraints() + 1))
        model.update()
        return sum
    }

    override fun Iterable<IVar>.sum(): IVar {
        val expr = GRBLinExpr()
        for (it in this) {
            expr.addTerm(it.coeff, it.getDelegate())
        }
        val sum = numVar()
        model.addConstr(expr, GRB.EQUAL, sum.expr(), "c" + (numConstraints() + 1))
        model.update()
        return sum
    }

    override fun Array<out IVar>.minimize(): IVar {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.setObjective(expr, GRB.MINIMIZE)
        return GurobiObjectiveVar(model)
    }

    override fun Iterable<IVar>.minimize(): IVar {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.setObjective(expr, GRB.MINIMIZE)
        return GurobiObjectiveVar(model)
    }

    override fun Array<out IVar>.maximize(): IVar {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.setObjective(expr, GRB.MAXIMIZE)
        return GurobiObjectiveVar(model)
    }

    override fun Iterable<IVar>.maximize(): IVar {
        val expr = GRBLinExpr()
        for (v in this) {
            expr.addTerm(v.coeff, v.getDelegate())
        }
        model.setObjective(expr, GRB.MAXIMIZE)
        return GurobiObjectiveVar(model)
    }

}