package top.bettercode.summer.tools.optimal.gurobi

import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBVar
import top.bettercode.summer.tools.optimal.IVar
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author Peter Wu
 */
class GurobiVar(
    private val _delegate: GRBVar,
    override val isInt: Boolean,
    override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = _delegate.get(GRB.DoubleAttr.X)

    override var lb: Double
        get() {
            val lb = _delegate.get(GRB.DoubleAttr.LB) * coeff
            val ub = _delegate.get(GRB.DoubleAttr.UB) * coeff
            return min(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.set(GRB.DoubleAttr.LB, value / coeff)
            else
                _delegate.set(GRB.DoubleAttr.UB, value / coeff)
        }

    override var ub: Double
        get() {
            val lb = _delegate.get(GRB.DoubleAttr.LB) * coeff
            val ub = _delegate.get(GRB.DoubleAttr.UB) * coeff
            return max(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.set(GRB.DoubleAttr.UB, value / coeff)
            else
                _delegate.set(GRB.DoubleAttr.LB, value / coeff)
        }

    override fun times(coeff: Double): IVar {
        return GurobiVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
