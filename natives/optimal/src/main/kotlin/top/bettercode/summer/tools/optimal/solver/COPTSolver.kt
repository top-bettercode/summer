package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.solver.`var`.COPTExprVar
import top.bettercode.summer.tools.optimal.solver.`var`.COPTVar
import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 * https://guide.coap.online/copt/zh-doc/
 * https://github.com/COPT-Public/COPT-Release 7.0.6
 *
 * No license found. The size is limited to 2000 variables and 2000 constraints
 * No license found. LP size is limited to 10000 variables and 10000 constraints
 *
 * @author Peter Wu
 */
class COPTSolver @JvmOverloads constructor(
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        logging: Boolean = false,
        name: String = "COPTSolver"
) : Solver(name, epsilon) {

    val model: copt.Model

    init {
        val env = copt.COPTEnvr()
        model = env.createModel(name)
        //解决 presolve 异常,但会导致一些问题求解：free(): invalid next size (fast)
//        model.setIntParam(copt.IntParam.Presolve, 0)
        model.setIntParam(copt.IntParam.Logging, if (logging) 1 else 0)
        model.setIntParam(copt.IntParam.LogToConsole, if (logging) 1 else 0)
        model.setDblParam(copt.DblParam.FeasTol, OptimalUtil.DEFAULT_MIN_EPSILON)
    }

    override fun setTimeLimit(seconds: Int) {
        model.setDblParam("TimeLimit", seconds.toDouble())
    }

    override fun solve() {
        model.solve()
    }

    override fun clear() {
        model.clear()
    }

    override fun isOptimal(): Boolean {
        val status = getStatus()
        return copt.Status.OPTIMAL == status
    }

    override fun getResultStatus(): String {
        val status = getStatus()
        try {
            val fields = copt.Status::class.java.fields
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
        var status = model.getIntAttr(copt.IntAttr.MipStatus)
        if (status == 0) {
            status = model.getIntAttr(copt.IntAttr.LpStatus)
        }
        return status
    }

    override fun numVariables(): Int {
        return model.getIntAttr(copt.IntAttr.Cols)
    }

    override fun numConstraints(): Int {
        return model.getIntAttr(copt.IntAttr.Rows)
    }

    private fun expr(`var`: IVar) =
            if (`var` is COPTExprVar) `var`.getDelegate() else copt.Expr(`var`.getDelegate(), `var`.coeff)

    override fun boolVarArray(count: Int): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = COPTVar(model.addVar(0.0, 1.0, 1.0, copt.Consts.BINARY, "b" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun intVarArray(count: Int, lb: Double, ub: Double): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = COPTVar(model.addVar(lb, ub, 1.0, copt.Consts.INTEGER, "i" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun numVarArray(count: Int, lb: Double, ub: Double): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = COPTVar(model.addVar(lb, ub, 1.0, copt.Consts.CONTINUOUS, "n" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun boolVar(): IVar {
        val numVariables = numVariables()
        return COPTVar(model.addVar(0.0, 1.0, 1.0, copt.Consts.BINARY, "b" + (numVariables + 1)))
    }

    override fun intVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        return COPTVar(model.addVar(lb, ub, 1.0, copt.Consts.INTEGER, "i" + (numVariables + 1)))
    }

    override fun numVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        return COPTVar(model.addVar(lb, ub, 1.0, copt.Consts.CONTINUOUS, "n" + (numVariables + 1)))
    }

    override fun IVar.plus(value: Double): IVar {
        val expr = copt.Expr()
        expr.addTerm(this.getDelegate(), this.coeff)
        expr.addConstant(value)
        val numVariables = numVariables()
        val sum = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, copt.Consts.CONTINUOUS, "n" + (numVariables + 1))
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, sum, "c" + (size + 1))
        return COPTVar(sum)
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, lb, "c" + (size + 1))
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, lb, "c" + (size + 1))
    }

    override fun Array<out IVar>.ge(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun Array<out IVar>.gt(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(-epsilon)
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(-epsilon)
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun Array<out IVar>.le(ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.LESS_EQUAL, ub, "c" + (size + 1))
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.LESS_EQUAL, ub, "c" + (size + 1))
    }

    override fun Array<out IVar>.le(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(epsilon)
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(epsilon)
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun Array<out IVar>.eq(value: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, value, "c" + (size + 1))
    }

    override fun Iterable<IVar>.eq(value: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, value, "c" + (size + 1))
    }

    override fun Array<out IVar>.eq(value: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, expr(value), "c" + (size + 1))
    }

    override fun Iterable<IVar>.eq(value: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, expr(value), "c" + (size + 1))
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: Double, ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr, copt.Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    override fun Iterable<IVar>.between(lb: Double, ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr, copt.Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: IVar, ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 2))
    }

    override fun Iterable<IVar>.between(lb: IVar, ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 2))
    }

    override fun IVar.ge(lb: Double) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.GREATER_EQUAL, lb, "c" + (size + 1))
    }

    override fun IVar.ge(lb: IVar) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun IVar.gt(lb: IVar) {
        val size = numConstraints()
        val expr = expr(this)
        expr.addConstant(-epsilon)
        model.addConstr(expr, copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun IVar.le(ub: Double) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.LESS_EQUAL, ub, "c" + (size + 1))
    }

    override fun IVar.le(ub: IVar) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun IVar.lt(ub: IVar) {
        val size = numConstraints()
        val expr = expr(this)
        expr.addConstant(epsilon)
        model.addConstr(expr, copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun IVar.eq(value: Double) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.EQUAL, value, "c" + (size + 1))
    }

    override fun IVar.eq(value: IVar) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.EQUAL, expr(value), "c" + (size + 1))
    }

    override fun IVar.between(lb: Double, ub: Double) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr(this), copt.Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    override fun IVar.between(lb: IVar, ub: IVar) {
        val size = numConstraints()
        model.addConstr(expr(this), copt.Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
        model.addConstr(expr(this), copt.Consts.LESS_EQUAL, expr(ub), "c" + (size + 2))
    }

    /**
     * <pre>
     * bool为1时：var >= value
     *
     * value <= var <= dub
     * dlb*(1-bool) + value*bool <= var <= dub
     * dlb <= var - value*bool + dlb*bool
     * dlb <= var - (value-dlb)*bool
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.geIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr(this), copt.Consts.GREATER_EQUAL, value)
    }

    /**
     * <pre>
     * bool为0时：var >= value
     *
     * value <= var <= dub
     * dlb*bool + value*(1-bool) <= var <= dub
     * value <= var + value*bool - dlb*bool
     * value <= var + (value-dlb)*bool
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.geIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr(this), copt.Consts.GREATER_EQUAL, value)
    }

    /**
     * <pre>
     * bool为1时：var <= value
     *
     * dlb <= var <= value
     * dlb <= var <= value*bool + dub*(1-bool)
     * var <= value*bool + dub*(1-bool)
     * var - (value-dub)*bool <=  dub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.leIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr(this), copt.Consts.LESS_EQUAL, value)
    }

    /**
     * <pre>
     * bool为0时：var <= value
     *
     * dlb <= var <= value
     * dlb <= var <= value*(1-bool) + dub*bool
     * var + (value-dub)*bool <=  value
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.leIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr(this), copt.Consts.LESS_EQUAL, value)
    }


    /**
     * <pre>
     * bool为1时：var == value
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.eqIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr(this), copt.Consts.EQUAL, value)
    }

    /**
     * <pre>
     * bool为0时：var == value
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.eqIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr(this), copt.Consts.EQUAL, value)
    }

    /**
     * <pre>
     * bool为1时：lb <= var <= ub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar) {
        val expr = expr(this)
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr, copt.Consts.GREATER_EQUAL, lb)
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr, copt.Consts.LESS_EQUAL, ub)
    }

    /**
     * <pre>
     * bool为0时：lb <= var <= ub
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar) {
        val expr = expr(this)
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr, copt.Consts.GREATER_EQUAL, lb)
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr, copt.Consts.LESS_EQUAL, ub)
    }

    override fun Array<out IVar>.sum(): IVar {
        val expr = copt.Expr()
        for (it in this) {
            expr.addTerm(it.getDelegate(), it.coeff)
        }
        val numVariables = numVariables()
        val sum = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, copt.Consts.CONTINUOUS, "n" + (numVariables + 1))
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, sum, "c" + (size + 1))
        return COPTVar(sum)
    }

    override fun Iterable<IVar>.sum(): IVar {
        val expr = copt.Expr()
        for (it in this) {
            expr.addTerm(it.getDelegate(), it.coeff)
        }
        val numVariables = numVariables()
        val sum = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, copt.Consts.CONTINUOUS, "n" + (numVariables + 1))
        val size = numConstraints()
        model.addConstr(expr, copt.Consts.EQUAL, sum, "c" + (size + 1))
        return COPTVar(sum)
    }

    override fun Array<out IVar>.minimize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, copt.Consts.MINIMIZE)
        return COPTExprVar(expr)
    }

    override fun Iterable<IVar>.minimize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, copt.Consts.MINIMIZE)
        return COPTExprVar(expr)
    }

    override fun Array<out IVar>.maximize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, copt.Consts.MAXIMIZE)
        return COPTExprVar(expr)
    }

    override fun Iterable<IVar>.maximize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, copt.Consts.MAXIMIZE)
        return COPTExprVar(expr)
    }

    override fun Array<out IVar>.atMostOne() {
        val varArry: Array<copt.Var> = this.map { it.getDelegate<copt.Var>() }.toTypedArray()
        val weights = this.map { it.coeff }.toTypedArray().toDoubleArray()
        model.addSos(varArry, weights, copt.Consts.SOS_TYPE1)
    }

    override fun Collection<IVar>.atMostOne() {
        val varArry: Array<copt.Var> = this.map { it.getDelegate<copt.Var>() }.toTypedArray()
        val weights = this.map { it.coeff }.toTypedArray().toDoubleArray()
        model.addSos(varArry, weights, copt.Consts.SOS_TYPE1)
    }
}