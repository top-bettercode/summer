package top.bettercode.summer.tools.optimal.solver

import ilog.concert.IloLinearNumExpr
import ilog.concert.IloObjective
import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.solver.Sense.*
import top.bettercode.summer.tools.optimal.solver.`var`.CplexNumVar
import top.bettercode.summer.tools.optimal.solver.`var`.CplexObjectiveVar
import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 * https://www.ibm.com/docs/zh/icos/12.9.0?topic=cplex-users-manual
 *
 * No license found. The size is limited to 1000 variables and 1000 constraints
 *
 * @author Peter Wu
 */
class CplexSolver @JvmOverloads constructor(
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "CplexSolver"
) : Solver(name, epsilon) {

    init {
        CplexNativeLibLoader.loadNativeLib()
    }

    val model: IloCplex = IloCplex()
    var objective: IloObjective? = null

    override fun setTimeLimit(seconds: Int) {
        model.setParam(IloCplex.Param.TimeLimit, seconds.toDouble())
    }

    override fun solve() {
        model.solve()
    }

    override fun clear() {
        model.clearModel()
    }

    override fun isOptimal(): Boolean {
        return IloCplex.Status.Optimal == model.status
    }

    override fun getResultStatus(): String {
        return model.status.toString()
    }

    override fun numVariables(): Int {
        return model.ncols
    }

    override fun numConstraints(): Int {
        return model.nrows
    }


    override fun boolVar(name: String?): IVar {
        return CplexNumVar(model.boolVar(name ?: ("b" + (numVariables() + 1))), model)
    }

    override fun intVar(lb: Double, ub: Double, name: String?): IVar {
        return CplexNumVar(model.intVar(lb.toInt(), ub.toInt(), name
                ?: ("i" + (numVariables() + 1))), model)
    }

    override fun numVar(lb: Double, ub: Double, name: String?): IVar {
        return CplexNumVar(model.numVar(lb, ub, name ?: ("n" + (numVariables() + 1))), model)
    }

    private fun expr(`var`: IVar): IloLinearNumExpr {
        val expr = model.linearNumExpr()
        expr.addTerm(`var`.getDelegate(), `var`.coeff)
        return expr
    }

    override fun IVar.ge(lb: Double) {
        model.addGe(expr(this), lb, "c" + (numConstraints() + 1))
    }

    override fun IVar.ge(lb: IVar) {
        model.addGe(expr(this), expr(lb), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addGe(expr, lb, "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addGe(expr, lb, "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.ge(lb: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addGe(expr, expr(lb), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addGe(expr, expr(lb), "c" + (numConstraints() + 1))
    }

    override fun IVar.gt(lb: IVar) {
        val expr = expr(this)
        expr.addTerm(numVar(-epsilon, -epsilon).getDelegate(), 1.0)
        model.addGe(expr, expr(lb), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.gt(lb: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addTerm(numVar(-epsilon, -epsilon).getDelegate(), 1.0)
        model.addGe(expr, expr(lb), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addTerm(numVar(-epsilon, -epsilon).getDelegate(), 1.0)
        model.addGe(expr, expr(lb), "c" + (numConstraints() + 1))
    }


    override fun IVar.le(ub: Double) {
        model.addLe(expr(this), ub, "c" + (numConstraints() + 1))
    }

    override fun IVar.le(ub: IVar) {
        model.addLe(expr(this), expr(ub), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.le(ub: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addLe(expr, ub, "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addLe(expr, ub, "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.le(ub: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addLe(expr, expr(ub), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addLe(expr, expr(ub), "c" + (numConstraints() + 1))
    }

    override fun IVar.lt(ub: IVar) {
        val expr = expr(this)
        expr.addTerm(numVar(epsilon, epsilon).getDelegate(), 1.0)
        model.addLe(expr, expr(ub), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addTerm(numVar(epsilon, epsilon).getDelegate(), 1.0)
        model.addLe(expr, expr(ub), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        expr.addTerm(numVar(epsilon, epsilon).getDelegate(), 1.0)
        model.addLe(expr, expr(ub), "c" + (numConstraints() + 1))
    }


    override fun IVar.eq(value: Double) {
        model.addEq(expr(this), value, "c" + (numConstraints() + 1))
    }

    override fun IVar.eq(value: IVar) {
        model.addEq(expr(this), expr(value), "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.eq(value: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addEq(expr, value, "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.eq(value: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addEq(expr, value, "c" + (numConstraints() + 1))
    }

    override fun Array<out IVar>.eq(value: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addEq(expr, expr(value), "c" + (numConstraints() + 1))
    }

    override fun Iterable<IVar>.eq(value: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addEq(expr, expr(value), "c" + (numConstraints() + 1))
    }

    override fun IVar.ne(value: Double) {
        model.add(model.not(model.eq(expr(this), value, "c" + (numConstraints() + 1))))
    }

    override fun IVar.between(lb: Double, ub: Double) {
        model.addRange(lb, expr(this), ub, "c" + (numConstraints() + 2))
    }

    override fun IVar.between(lb: IVar, ub: IVar) {
        val size = numConstraints()
        model.addGe(expr(this), expr(lb), "c" + (size + 1))
        model.addLe(expr(this), expr(ub), "c" + (size + 2))
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: Double, ub: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addRange(lb, expr, ub, "c" + (numConstraints() + 2))
    }

    override fun Iterable<IVar>.between(lb: Double, ub: Double) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        model.addRange(lb, expr, ub, "c" + (numConstraints() + 2))
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: IVar, ub: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addGe(expr, expr(lb), "c" + (size + 1))
        model.addLe(expr, expr(ub), "c" + (size + 2))
    }

    override fun Iterable<IVar>.between(lb: IVar, ub: IVar) {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        val size = numConstraints()
        model.addGe(expr, expr(lb), "c" + (size + 1))
        model.addLe(expr, expr(ub), "c" + (size + 2))
    }


    override fun IVar.geIf(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 1.0), model.ge(expr(this), value)))
    }

    override fun IVar.geIfNot(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 0.0), model.ge(expr(this), value)))
    }

    override fun IVar.leIf(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 1.0), model.le(expr(this), value)))
    }

    override fun IVar.leIfNot(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 0.0), model.le(expr(this), value)))
    }


    override fun IVar.eqIf(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 1.0), model.eq(expr(this), value)))
    }

    override fun IVar.eqIfNot(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 0.0), model.eq(expr(this), value)))
    }

    override fun IVar.neIf(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 1.0), model.not(model.eq(expr(this), value))))
    }

    override fun IVar.neIfNot(value: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 0.0), model.not(model.eq(expr(this), value))))
    }

    override fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 1.0), model.range(lb, expr(this), ub)))
    }

    override fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar) {
        model.add(model.ifThen(model.eq(bool.getDelegate(), 0.0), model.range(lb, expr(this), ub)))
    }

    override fun Constraint.onlyEnforceIf(condition: Constraint) {
        val whenCon = when (condition.sense) {
            EQ -> model.eq(condition.variable.getDelegate(), condition.value)
            NE -> model.not(model.eq(condition.variable.getDelegate(), condition.value))
            GE -> model.ge(condition.variable.getDelegate(), condition.value)
            GT -> model.ge(condition.variable.getDelegate(), condition.value + epsilon)
            LE -> model.le(condition.variable.getDelegate(), condition.value)
            LT -> model.le(condition.variable.getDelegate(), condition.value - epsilon)
        }
        val thenCon = when (this.sense) {
            EQ -> model.eq(this.variable.getDelegate(), this.value)
            NE -> model.not(model.eq(this.variable.getDelegate(), this.value))
            GE -> model.ge(this.variable.getDelegate(), this.value)
            GT -> model.ge(this.variable.getDelegate(), this.value + epsilon)
            LE -> model.le(this.variable.getDelegate(), this.value)
            LT -> model.le(this.variable.getDelegate(), this.value - epsilon)
        }
        model.add(model.ifThen(whenCon, thenCon))
    }

    override fun Array<Constraint>.onlyEnforceIf(condition: Constraint) {
        val whenCon = when (condition.sense) {
            EQ -> model.eq(condition.variable.getDelegate(), condition.value)
            NE -> model.not(model.eq(condition.variable.getDelegate(), condition.value))
            GE -> model.ge(condition.variable.getDelegate(), condition.value)
            GT -> model.ge(condition.variable.getDelegate(), condition.value + epsilon)
            LE -> model.le(condition.variable.getDelegate(), condition.value)
            LT -> model.le(condition.variable.getDelegate(), condition.value - epsilon)
        }
        this.forEach {
            val thenCon = when (it.sense) {
                EQ -> model.eq(it.variable.getDelegate(), it.value)
                NE -> model.not(model.eq(it.variable.getDelegate(), it.value))
                GE -> model.ge(it.variable.getDelegate(), it.value)
                GT -> model.ge(it.variable.getDelegate(), it.value + epsilon)
                LE -> model.le(it.variable.getDelegate(), it.value)
                LT -> model.le(it.variable.getDelegate(), it.value - epsilon)
            }
            model.add(model.ifThen(whenCon, thenCon))
        }
    }

    override fun Iterable<Constraint>.onlyEnforceIf(condition: Constraint) {
        val whenCon = when (condition.sense) {
            EQ -> model.eq(condition.variable.getDelegate(), condition.value)
            NE -> model.not(model.eq(condition.variable.getDelegate(), condition.value))
            GE -> model.ge(condition.variable.getDelegate(), condition.value)
            GT -> model.ge(condition.variable.getDelegate(), condition.value + epsilon)
            LE -> model.le(condition.variable.getDelegate(), condition.value)
            LT -> model.le(condition.variable.getDelegate(), condition.value - epsilon)
        }
        this.forEach {
            val thenCon = when (it.sense) {
                EQ -> model.eq(it.variable.getDelegate(), it.value)
                NE -> model.not(model.eq(it.variable.getDelegate(), it.value))
                GE -> model.ge(it.variable.getDelegate(), it.value)
                GT -> model.ge(it.variable.getDelegate(), it.value + epsilon)
                LE -> model.le(it.variable.getDelegate(), it.value)
                LT -> model.le(it.variable.getDelegate(), it.value - epsilon)
            }
            model.add(model.ifThen(whenCon, thenCon))
        }
    }

    override fun Array<out IVar>.sum(): IVar {
        val expr = model.linearNumExpr()
        for (it in this) {
            expr.addTerm(it.getDelegate(), it.coeff)
        }
        val sum = numVar()
        model.addEq(expr, expr(sum), "c" + (numConstraints() + 1))
        return sum
    }

    override fun Iterable<IVar>.sum(): IVar {
        val expr = model.linearNumExpr()
        for (it in this) {
            expr.addTerm(it.getDelegate(), it.coeff)
        }
        val sum = numVar()
        model.addEq(expr, expr(sum), "c" + (numConstraints() + 1))
        return sum
    }

    override fun Array<out IVar>.minimize(): IVar {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        if (objective != null) {
            model.remove(objective)
        }
        objective = model.addMinimize(expr)
        return CplexObjectiveVar(model)
    }

    override fun Iterable<IVar>.minimize(): IVar {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        if (objective != null) {
            model.remove(objective)
        }
        objective = model.addMinimize(expr)
        return CplexObjectiveVar(model)
    }

    override fun Array<out IVar>.maximize(): IVar {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        if (objective != null) {
            model.remove(objective)
        }
        objective = model.addMaximize(expr)
        return CplexObjectiveVar(model)
    }

    override fun Iterable<IVar>.maximize(): IVar {
        val expr = model.linearNumExpr()
        for (v in this) {
            expr.addTerm(v.getDelegate(), v.coeff)
        }
        if (objective != null) {
            model.remove(objective)
        }
        objective = model.addMaximize(expr)
        return CplexObjectiveVar(model)
    }

}