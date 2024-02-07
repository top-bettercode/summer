package top.bettercode.summer.tools.optimal.solver.`var`

import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBVar
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class GurobiVar(
        private val _delegate: GRBVar,
        override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = _delegate.get(GRB.DoubleAttr.X).scale()

    override var lb: Double
        get() = _delegate.get(GRB.DoubleAttr.LB).scale()
        set(value) {
            _delegate.set(GRB.DoubleAttr.LB, value)
        }

    override var ub: Double
        get() = _delegate.get(GRB.DoubleAttr.UB).scale()
        set(value) {
            _delegate.set(GRB.DoubleAttr.UB, value)
        }

    override fun times(coeff: Double): IVar {
        return GurobiVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
