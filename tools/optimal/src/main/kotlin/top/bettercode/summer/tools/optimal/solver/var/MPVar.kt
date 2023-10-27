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

    override fun coeff(coeff: Double): IVar {
        return MPVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
