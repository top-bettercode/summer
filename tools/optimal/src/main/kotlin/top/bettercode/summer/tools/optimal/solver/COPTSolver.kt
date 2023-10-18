package top.bettercode.summer.tools.optimal.solver

import copt.*
import top.bettercode.summer.tools.optimal.OptimalNativeLibLoader
import top.bettercode.summer.tools.optimal.solver.`var`.COPTExprVar
import top.bettercode.summer.tools.optimal.solver.`var`.COPTVar
import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 * https://guide.coap.online/copt/zh-doc/
 * https://github.com/COPT-Public/COPT-Release
 *
 * No license found. The size is limited to 2000 variables and 2000 constraints
 *
 * @author Peter Wu
 */
class COPTSolver @JvmOverloads constructor(
        override val name: String = "COPTSolver",
        logging: Boolean = false) : Solver() {

    val model: Model

    companion object {
        init {
            OptimalNativeLibLoader.loadNativeLib()
        }
    }

    init {
        val env = Envr()
        this.model = env.createModel(name)
        this.model.setIntParam(IntParam.Logging, if (logging) 1 else 0)
    }

    private fun expr(`var`: IVar) =
            if (`var` is COPTExprVar) `var`.getDelegate() else Expr(`var`.getDelegate<Var>(), `var`.coeff)

    override fun boolVarArray(count: Int): Array<IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = COPTVar(this.model.addVar(0.0, 1.0, 1.0, Consts.BINARY, "b" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun intVarArray(count: Int, lb: Double, ub: Double): Array<IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = COPTVar(this.model.addVar(lb, ub, 1.0, Consts.INTEGER, "i" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun numVarArray(count: Int, lb: Double, ub: Double): Array<IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = COPTVar(this.model.addVar(lb, ub, 1.0, Consts.CONTINUOUS, "n" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun boolVar(): IVar {
        val numVariables = numVariables()
        return COPTVar(this.model.addVar(0.0, 1.0, 1.0, Consts.BINARY, "b" + (numVariables + 1)))
    }

    override fun intVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        return COPTVar(this.model.addVar(lb, ub, 1.0, Consts.INTEGER, "i" + (numVariables + 1)))
    }

    override fun numVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        return COPTVar(this.model.addVar(lb, ub, 1.0, Consts.CONTINUOUS, "n" + (numVariables + 1)))
    }

    override fun ge(vars: Array<IVar>, lb: Double) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.GREATER_EQUAL, lb, "c" + (size + 1))
    }

    override fun ge(vars: Array<IVar>, lb: IVar) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun le(vars: Array<IVar>, ub: Double) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.LESS_EQUAL, ub, "c" + (size + 1))
    }

    override fun le(vars: Array<IVar>, ub: IVar) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun eq(vars: Array<IVar>, value: Double) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.EQUAL, value, "c" + (size + 1))
    }

    override fun eq(vars: Array<IVar>, value: IVar) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.EQUAL, expr(value), "c" + (size + 1))
    }

    /**
     * 两数之间
     */
    override fun between(vars: Array<IVar>, lb: Double, ub: Double) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        this.model.addConstr(expr, Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    /**
     * 两数之间
     */
    override fun between(vars: Array<IVar>, lb: IVar, ub: IVar) {
        val expr = Expr()
        for (v in vars) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        this.model.addConstr(expr, Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
        this.model.addConstr(expr, Consts.LESS_EQUAL, expr(ub), "c" + (size + 2))
    }

    override fun ge(`var`: IVar, lb: Double) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.GREATER_EQUAL, lb, "c" + (size + 1))
    }

    override fun ge(`var`: IVar, lb: IVar) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
    }

    override fun le(`var`: IVar, ub: Double) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.LESS_EQUAL, ub, "c" + (size + 1))
    }

    override fun le(`var`: IVar, ub: IVar) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.LESS_EQUAL, expr(ub), "c" + (size + 1))
    }

    override fun eq(`var`: IVar, value: Double) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.EQUAL, value, "c" + (size + 1))
    }

    override fun eq(`var`: IVar, value: IVar) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.EQUAL, expr(value), "c" + (size + 1))
    }

    override fun between(`var`: IVar, lb: Double, ub: Double) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        this.model.addConstr(expr(`var`), Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    override fun between(`var`: IVar, lb: IVar, ub: IVar) {
        val size = numConstraints()
        this.model.addConstr(expr(`var`), Consts.GREATER_EQUAL, expr(lb), "c" + (size + 1))
        this.model.addConstr(expr(`var`), Consts.LESS_EQUAL, expr(ub), "c" + (size + 2))
    }

    /**
     * lb <= dividend/divisor <= ub
     * `var`2*lb <= `var`1
     * `var`1    <= `var`2*ub
     */
    override fun between(dividend: IVar, divisor: IVar, lb: Double, ub: Double) {
        le(divisor.coeff(lb), dividend)
        ge(divisor.coeff(ub), dividend)
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
    override fun geIf(`var`: IVar, value: Double, bool: IVar) {
        this.model.addGenConstrIndicator(bool.getDelegate(), 1, expr(`var`), Consts.GREATER_EQUAL, value)
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
    override fun geIfNot(`var`: IVar, value: Double, bool: IVar) {
        this.model.addGenConstrIndicator(bool.getDelegate(), 0, expr(`var`), Consts.GREATER_EQUAL, value)
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
    override fun leIf(`var`: IVar, value: Double, bool: IVar) {
        this.model.addGenConstrIndicator(bool.getDelegate(), 1, expr(`var`), Consts.LESS_EQUAL, value)
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
    override fun leIfNot(`var`: IVar, value: Double, bool: IVar) {
        this.model.addGenConstrIndicator(bool.getDelegate(), 0, expr(`var`), Consts.LESS_EQUAL, value)
    }


    /**
     * <pre>
     * bool为1时：var == value
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun eqIf(`var`: IVar, value: Double, bool: IVar) {
        this.model.addGenConstrIndicator(bool.getDelegate(), 1, expr(`var`), Consts.EQUAL, value)
    }

    /**
     * <pre>
     * bool为0时：var == value
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun eqIfNot(`var`: IVar, value: Double, bool: IVar) {
        this.model.addGenConstrIndicator(bool.getDelegate(), 0, expr(`var`), Consts.EQUAL, value)
    }

    /**
     * <pre>
     * bool为1时：lb <= var <= ub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun betweenIf(`var`: IVar, lb: Double, ub: Double, bool: IVar) {
        val expr = expr(`var`)
        this.model.addGenConstrIndicator(bool.getDelegate(), 1, expr, Consts.GREATER_EQUAL, lb)
        this.model.addGenConstrIndicator(bool.getDelegate(), 1, expr, Consts.LESS_EQUAL, ub)
    }

    /**
     * <pre>
     * bool为0时：lb <= var <= ub
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun betweenIfNot(`var`: IVar, lb: Double, ub: Double, bool: IVar) {
        val expr = expr(`var`)
        this.model.addGenConstrIndicator(bool.getDelegate(), 0, expr, Consts.GREATER_EQUAL, lb)
        this.model.addGenConstrIndicator(bool.getDelegate(), 0, expr, Consts.LESS_EQUAL, ub)
    }

    override fun sum(vars: Array<IVar>): IVar {
        val expr = Expr()
        for (i in vars.indices) {
            val v = vars[i]
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val numVariables = numVariables()
        val sum = this.model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, Consts.CONTINUOUS, "n" + (numVariables + 1))
        val size = numConstraints()
        this.model.addConstr(expr, Consts.EQUAL, sum, "c" + (size + 1))
        return COPTVar(sum)
    }

    override fun minimize(vars: Array<IVar>): IVar {
        val expr = Expr()
        for (i in vars.indices) {
            val v = vars[i]
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        this.model.setObjective(expr, Consts.MINIMIZE)
        return COPTExprVar(expr)
    }

    override fun maximize(vars: Array<IVar>): IVar {
        val expr = Expr()
        for (i in vars.indices) {
            val v = vars[i]
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        this.model.setObjective(expr, Consts.MAXIMIZE)
        return COPTExprVar(expr)
    }

    override fun atMostOne(vars: Array<IVar>) {
        val varArry: Array<Var> = vars.map { it.getDelegate<Var>() }.toTypedArray()
        val weights = vars.map { it.coeff }.toTypedArray().toDoubleArray()
        model.addSos(varArry, weights, Consts.SOS_TYPE1)
    }

    /**
     * 最多n个非零变量
     */
    override fun atMost(vars: Array<IVar>, n: Int) {
        val count = vars.size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        eq(boolVarArray, n.toDouble())
        for (i in vars.indices) {
            eqIfNot(vars[i], 0.0, boolVarArray[i])
        }
    }

    override fun setTimeLimit(seconds: Int) {
        this.model.setDblParam("TimeLimit", seconds.toDouble())
    }

    override fun solve() {
        this.model.solve()
    }

    override fun isOptimal(): Boolean {
        val status = getStatus()
        return Status.OPTIMAL == status
    }

    override fun getResultStatus(): String {
        val status = getStatus()
        try {
            val fields = Status::class.java.getFields()
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
        var status = model.getIntAttr(IntAttr.MipStatus)
        if (status == 0) {
            status = model.getIntAttr(IntAttr.LpStatus)
        }
        return status
    }

    override fun numVariables(): Int {
        return this.model.getIntAttr(IntAttr.Cols)
    }

    override fun numConstraints(): Int {
        return this.model.getIntAttr(IntAttr.Rows)
    }

}