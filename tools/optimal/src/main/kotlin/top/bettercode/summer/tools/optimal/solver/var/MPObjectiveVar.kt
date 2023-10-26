package top.bettercode.summer.tools.optimal.solver.`var`

import com.google.ortools.linearsolver.MPObjective

/**
 *
 * @author Peter Wu
 */
class MPObjectiveVar(private val _delegate: MPObjective,
                     override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = IVar.convertDecimal(_delegate.value())

    override fun coeff(coeff: Double): IVar {
        return MPObjectiveVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
