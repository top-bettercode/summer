package top.bettercode.summer.tools.optimal.ortools

import com.google.ortools.linearsolver.MPVariable
import top.bettercode.summer.tools.optimal.IVar
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author Peter Wu
 */
class MPVar(
    private val _delegate: MPVariable,
    override val isInt: Boolean,
    override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = _delegate.solutionValue()

    override var lb: Double
        get() {
            val lb = _delegate.lb() * coeff
            val ub = _delegate.ub() * coeff
            return min(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.setLb(value / coeff)
            else
                _delegate.setUb(value / coeff)
        }

    override var ub: Double
        get() {
            val lb = _delegate.lb() * coeff
            val ub = _delegate.ub() * coeff
            return max(lb, ub)
        }
        set(value) {
            if (coeff > 0)
                _delegate.setUb(value / coeff)
            else
                _delegate.setLb(value / coeff)
        }

    override fun times(coeff: Double): IVar {
        return MPVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
