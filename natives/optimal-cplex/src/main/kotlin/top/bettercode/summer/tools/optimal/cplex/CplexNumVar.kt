package top.bettercode.summer.tools.optimal.cplex

import ilog.concert.IloNumVar
import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.IVar

/**
 *
 * @author Peter Wu
 */
class CplexNumVar(
    private val _delegate: IloNumVar,
    val model: IloCplex,
    override val isInt: Boolean,
    override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = model.getValue(_delegate)

    override var lb: Double
        get() = _delegate.lb
        set(value) {
            _delegate.lb = value
        }

    override var ub: Double
        get() = _delegate.ub
        set(value) {
            _delegate.ub = value
        }

    override fun times(coeff: Double): IVar {
        return CplexNumVar(_delegate = _delegate, model = model, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
