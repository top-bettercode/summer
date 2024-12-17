package top.bettercode.summer.tools.optimal.cplex

import ilog.concert.IloNumVar
import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.IVar
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author Peter Wu
 */
class CplexNumVar(
    private val _delegate: IloNumVar,
    val model: IloCplex,
    override val isInt: Boolean,
    override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = model.getValue(_delegate)

    override var lb: Double
        get() {
            val lb = _delegate.lb * coeff
            val ub = _delegate.ub * coeff
            return min(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.lb = value / coeff
            else
                _delegate.ub = value / coeff
        }

    override var ub: Double
        get() {
            val lb = _delegate.lb * coeff
            val ub = _delegate.ub * coeff
            return max(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.ub = value / coeff
            else
                _delegate.lb = value / coeff
        }

    override fun times(coeff: Double): IVar {
        return CplexNumVar(_delegate = _delegate, model = model, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
