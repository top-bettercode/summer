package top.bettercode.summer.tools.optimal.solver.`var`

import com.google.ortools.linearsolver.MPVariable
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class MPVar(private val _delegate: MPVariable,
            override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = _delegate.solutionValue().scale()

    override var lb: Double
        get() = _delegate.lb().scale()
        set(value) {
            _delegate.setLb(value)
        }

    override var ub: Double
        get() = _delegate.ub().scale()
        set(value) {
            _delegate.setUb(value)
        }

    override fun times(coeff: Double): IVar {
        return MPVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
