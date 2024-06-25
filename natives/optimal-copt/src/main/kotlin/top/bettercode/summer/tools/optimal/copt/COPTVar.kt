package top.bettercode.summer.tools.optimal.copt

import copt.DblInfo
import copt.Var
import top.bettercode.summer.tools.optimal.IVar

/**
 *
 * @author Peter Wu
 */
class COPTVar(
    private val _delegate: Var,
    override val isInt: Boolean,
    override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = _delegate.get(DblInfo.Value)

    override var lb: Double
        get() = _delegate.get(DblInfo.LB)
        set(value) {
            _delegate.set(DblInfo.LB, value)
        }

    override var ub: Double
        get() = _delegate.get(DblInfo.UB)
        set(value) {
            _delegate.set(DblInfo.UB, value)
        }

    override fun times(coeff: Double): IVar {
        return COPTVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
