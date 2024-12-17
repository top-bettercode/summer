package top.bettercode.summer.tools.optimal.copt

import copt.DblInfo
import copt.Var
import top.bettercode.summer.tools.optimal.IVar
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author Peter Wu
 */
class COPTVar(
    private val _delegate: Var,
    override val isInt: Boolean,
    override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = _delegate.get(DblInfo.Value)

    override var lb: Double
        get() {
            val lb = _delegate.get(DblInfo.LB) * coeff
            val ub = _delegate.get(DblInfo.UB) * coeff
            return min(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.set(DblInfo.LB, value / coeff)
            else
                _delegate.set(DblInfo.UB, value / coeff)
        }

    override var ub: Double
        get() {
            val lb = _delegate.get(DblInfo.LB) * coeff
            val ub = _delegate.get(DblInfo.UB) * coeff
            return max(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.set(DblInfo.UB, value / coeff)
            else
                _delegate.set(DblInfo.LB, value / coeff)
        }

    override fun times(coeff: Double): IVar {
        return COPTVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
