package top.bettercode.summer.tools.optimal.ortools

import com.google.ortools.linearsolver.MPObjective
import top.bettercode.summer.tools.optimal.IVar

/**
 *
 * @author Peter Wu
 */
class MPObjectiveVar(
    private val _delegate: MPObjective,
    override val isInt: Boolean = false,
    override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = _delegate.value()

    override var lb: Double
        get() {
            throw UnsupportedOperationException()
        }
        set(_) {
            throw UnsupportedOperationException()
        }

    override var ub: Double
        get() {
            throw UnsupportedOperationException()
        }
        set(_) {
            throw UnsupportedOperationException()
        }


    override fun times(coeff: Double): IVar {
        return MPObjectiveVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
