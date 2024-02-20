package top.bettercode.summer.tools.optimal.solver.`var`

import com.google.ortools.linearsolver.MPObjective
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class MPObjectiveVar(private val _delegate: MPObjective,
                     override val isInt: Boolean = false,
                     override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = _delegate.value().scale()

    override var lb: Double
        get() = _delegate.value().scale()
        set(_) {}

    override var ub: Double
        get() = _delegate.value().scale()
        set(_) {}

    override fun times(coeff: Double): IVar {
        return MPObjectiveVar(_delegate = _delegate, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
