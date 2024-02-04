package top.bettercode.summer.tools.optimal.solver.`var`

import ilog.concert.IloNumVar
import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class CplexNumVar(
        private val _delegate: IloNumVar,
        val model: IloCplex,
        override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = model.getValue(_delegate).scale()

    override val lb: Double
        get() = _delegate.lb.scale()

    override val ub: Double
        get() = _delegate.ub.scale()

    override fun times(coeff: Double): IVar {
        return CplexNumVar(_delegate = _delegate, model = model, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
