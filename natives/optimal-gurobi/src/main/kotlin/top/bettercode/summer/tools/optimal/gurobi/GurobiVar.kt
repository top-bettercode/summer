package top.bettercode.summer.tools.optimal.gurobi

import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBVar
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class GurobiVar(
        private val _delegate: GRBVar,
        override val isInt: Boolean,
        override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = _delegate.get(GRB.DoubleAttr.X).scale()

    override var lb: Double
        get() = _delegate.get(GRB.DoubleAttr.LB)
        set(value) {
            _delegate.set(GRB.DoubleAttr.LB, value)
        }

    override var ub: Double
        get() = _delegate.get(GRB.DoubleAttr.UB)
        set(value) {
            _delegate.set(GRB.DoubleAttr.UB, value)
        }

    override fun times(coeff: Double): IVar {
        return GurobiVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
