package top.bettercode.summer.tools.optimal.solver.`var`

import copt.DblInfo
import copt.Var

/**
 *
 * @author Peter Wu
 */
class COPTVar(private val _delegate: Var,
              override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = IVar.convertDecimal(_delegate.get(DblInfo.Value))

    override fun coeff(coeff: Double): IVar {
        return COPTVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
