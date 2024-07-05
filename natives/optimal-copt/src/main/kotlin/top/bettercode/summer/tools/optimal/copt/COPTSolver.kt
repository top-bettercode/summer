package top.bettercode.summer.tools.optimal.copt

import copt.Consts
import copt.Var
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.OptimalUtil.isInt
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.SolverType
import kotlin.math.max
import kotlin.math.min

/**
 * https://guide.coap.online/copt/zh-doc/
 * https://github.com/COPT-Public/COPT-Release 7.1.0
 *
 * without license the size is limited to 2000 variables and 2000 constraints
 * without license LP size is limited to 10000 variables and 10000 constraints
 *
 * support min epsilon = 1e-7
 *
 * @author Peter Wu
 */
class COPTSolver @JvmOverloads constructor(
    epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
    minEpsilon: Double? = 1e-7,
    logging: Boolean = false,
    name: String = "COPTSolver"
) : Solver(
    name = name,
    type = SolverType.COPT,
    epsilon = epsilon,
    minEpsilon = minEpsilon,
    communityLimits = 2000
) {

    private val env: copt.Envr = copt.Envr()
    var model: copt.Model = env.createModel(name)

    init {
        model.setIntParam(copt.IntParam.Logging, if (logging) 1 else 0)
        model.setIntParam(copt.IntParam.LogToConsole, if (logging) 1 else 0)
        model.setDblParam(copt.DblParam.FeasTol, 1e-9)
    }

    override fun setTimeLimit(seconds: Long) {
        model.setDblParam("TimeLimit", seconds.toDouble())
    }

    override fun triggerLimit(): Boolean {
        val numVariables = numVariables()
        val numConstraints = numConstraints()
        log.info("$name 变量数量：{},约束数量：{}", numVariables, numConstraints)
        val bool = numVariables > communityLimits || numConstraints > communityLimits
        if (bool) {
            log.error("$name 变量或约束过多，变量数量：$numVariables 约束数量：$numConstraints")
        }
        return bool
    }

    override fun writeLp(filename: String) {
        model.writeLp(filename)
    }

    override fun writeMps(filename: String) {
        model.writeMps(filename)
    }

    override fun solve() {
        model.solve()
    }

    override fun close() {
        model.dispose()
        env.dispose()
    }

    override fun reset() {
        model.clear()
    }

    override fun isOptimal(): Boolean {
        return copt.Status.OPTIMAL == getStatus()
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

    private fun IVar.expr() =
        if (this is COPTExprVar) this.getDelegate() else copt.Expr(this.getDelegate(), this.coeff)

    override fun boolVar(name: String?): IVar {
        return COPTVar(
            _delegate = model.addVar(
                0.0, 1.0, 1.0, Consts.BINARY, name
                    ?: ("b" + (numVariables() + 1))
            ), isInt = true
        )
    }

    override fun intVar(lb: Double, ub: Double, name: String?): IVar {
        return COPTVar(
            _delegate = model.addVar(
                lb, ub, 1.0, Consts.INTEGER, name
                    ?: ("i" + (numVariables() + 1))
            ), isInt = true
        )
    }

    override fun numVar(lb: Double, ub: Double, name: String?): IVar {
        return COPTVar(
            _delegate = model.addVar(
                lb, ub, 1.0, Consts.CONTINUOUS, name
                    ?: ("n" + (numVariables() + 1))
            ), isInt = false
        )
    }

    override fun IVar.plus(value: Double): IVar {
        val expr = copt.Expr()
        expr.addTerm(this.getDelegate(), this.coeff)
        expr.addConstant(value)
        val sum = if (this.isInt && this.coeff.isInt && value.isInt) intVar() else numVar()
        model.addConstr(expr, Consts.EQUAL, sum.getDelegate<Var>(), "c" + (numConstraints() + 1))
        return sum
    }

    override fun IVar.ge(lb: Double) {
        model.addConstr(this.expr(), Consts.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
    }

    override fun IVar.ge(lb: IVar) {
        model.addConstr(this.expr(), Consts.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.GREATER_EQUAL, lb, "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.ge(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
    }

    override fun IVar.gt(lb: IVar) {
        val expr = this.expr()
        expr.addConstant(-epsilon)
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.gt(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(-epsilon)
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(-epsilon)
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (numConstraints() + 1))
    }


    override fun IVar.le(ub: Double) {
        model.addConstr(this.expr(), Consts.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
    }

    override fun IVar.le(ub: IVar) {
        model.addConstr(this.expr(), Consts.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.le(ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.LESS_EQUAL, ub, "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.le(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
    }

    override fun IVar.lt(ub: IVar) {
        val expr = this.expr()
        expr.addConstant(epsilon)
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(epsilon)
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addConstant(epsilon)
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (numConstraints() + 1))
    }


    override fun IVar.eq(value: Double) {
        model.addConstr(this.expr(), Consts.EQUAL, value, "c" + (numConstraints() + 1))
    }

    override fun IVar.eq(value: IVar) {
        model.addConstr(this.expr(), Consts.EQUAL, value.expr(), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.eq(value: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.EQUAL, value, "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.eq(value: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.EQUAL, value, "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.eq(value: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.EQUAL, value.expr(), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.eq(value: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addConstr(expr, Consts.EQUAL, value.expr(), "c" + (numConstraints() + 1))
    }


    override fun IVar.between(lb: Double, ub: Double) {
        val size = numConstraints()
        model.addConstr(this.expr(), Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(this.expr(), Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    override fun IVar.between(lb: IVar, ub: IVar) {
        val size = numConstraints()
        model.addConstr(this.expr(), Consts.GREATER_EQUAL, lb.expr(), "c" + (size + 1))
        model.addConstr(this.expr(), Consts.LESS_EQUAL, ub.expr(), "c" + (size + 2))
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
        model.addConstr(expr, Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr, Consts.LESS_EQUAL, ub, "c" + (size + 2))
    }

    override fun Iterable<IVar>.between(lb: Double, ub: Double) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, Consts.GREATER_EQUAL, lb, "c" + (size + 1))
        model.addConstr(expr, Consts.LESS_EQUAL, ub, "c" + (size + 2))
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
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (size + 1))
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (size + 2))
    }

    override fun Iterable<IVar>.between(lb: IVar, ub: IVar) {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addConstr(expr, Consts.GREATER_EQUAL, lb.expr(), "c" + (size + 1))
        model.addConstr(expr, Consts.LESS_EQUAL, ub.expr(), "c" + (size + 2))
    }


    override fun IVar.geIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, this.expr(), Consts.GREATER_EQUAL, value)
    }

    override fun IVar.geIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, this.expr(), Consts.GREATER_EQUAL, value)
    }

    override fun IVar.leIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, this.expr(), Consts.LESS_EQUAL, value)
    }

    override fun IVar.leIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, this.expr(), Consts.LESS_EQUAL, value)
    }


    override fun IVar.eqIf(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 1, this.expr(), Consts.EQUAL, value)
    }

    override fun IVar.eqIfNot(value: Double, bool: IVar) {
        model.addGenConstrIndicator(bool.getDelegate(), 0, this.expr(), Consts.EQUAL, value)
    }

    override fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar) {
        val expr = this.expr()
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr, Consts.GREATER_EQUAL, lb)
        model.addGenConstrIndicator(bool.getDelegate(), 1, expr, Consts.LESS_EQUAL, ub)
    }

    override fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar) {
        val expr = this.expr()
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr, Consts.GREATER_EQUAL, lb)
        model.addGenConstrIndicator(bool.getDelegate(), 0, expr, Consts.LESS_EQUAL, ub)
    }

    override fun Array<out IVar>.sum(): IVar {
        val expr = copt.Expr()
        var isInt = true
        var lb = 0.0
        var ub = 0.0
        for (it in this) {
            if (lb > -INFINITY)
                lb += it.lb * it.coeff
            if (ub < INFINITY)
                ub += it.ub * it.coeff
            if (!it.isInt || !it.coeff.isInt) {
                isInt = false
            }
            expr.addTerm(it.getDelegate(), it.coeff)
        }
        lb = max(lb, -INFINITY)
        ub = min(ub, INFINITY)
        val sum = if (isInt) intVar(lb, ub) else numVar(lb, ub)
        model.addConstr(expr, Consts.EQUAL, sum.expr(), "c" + (numConstraints() + 1))
        return sum
    }

    override fun Iterable<IVar>.sum(): IVar {
        val expr = copt.Expr()
        var isInt = true
        var lb = 0.0
        var ub = 0.0
        for (it in this) {
            if (lb > -INFINITY)
                lb += it.lb * it.coeff
            if (ub < INFINITY)
                ub += it.ub * it.coeff
            if (!it.isInt || !it.coeff.isInt) {
                isInt = false
            }
            expr.addTerm(it.getDelegate(), it.coeff)
        }
        lb = max(lb, -INFINITY)
        ub = min(ub, INFINITY)
        val sum = if (isInt) intVar(lb, ub) else numVar(lb, ub)
        model.addConstr(expr, Consts.EQUAL, sum.expr(), "c" + (numConstraints() + 1))
        return sum
    }

    override fun Array<out IVar>.minimize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, Consts.MINIMIZE)
        return COPTExprVar(expr)
    }

    override fun Iterable<IVar>.minimize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, Consts.MINIMIZE)
        return COPTExprVar(expr)
    }

    override fun Array<out IVar>.maximize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, Consts.MAXIMIZE)
        return COPTExprVar(expr)
    }

    override fun Iterable<IVar>.maximize(): IVar {
        val expr = copt.Expr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.setObjective(expr, Consts.MAXIMIZE)
        return COPTExprVar(expr)
    }

    override fun Array<out IVar>.atMostOne() {
        val varArry: Array<Var> = this.map { it.getDelegate<Var>() }.toTypedArray()
        val weights = this.map { it.coeff }.toTypedArray().toDoubleArray()
        model.addSos(varArry, weights, Consts.SOS_TYPE1)
    }

    override fun Collection<IVar>.atMostOne() {
        val varArry: Array<Var> = this.map { it.getDelegate<Var>() }.toTypedArray()
        val weights = this.map { it.coeff }.toTypedArray().toDoubleArray()
        model.addSos(varArry, weights, Consts.SOS_TYPE1)
    }
}