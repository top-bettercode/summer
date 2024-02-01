package top.bettercode.summer.tools.optimal.solver.`var`

import copt.DblInfo
import copt.Var
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class COPTVar(
        private val _delegate: Var,
        override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = _delegate.get(DblInfo.Value).scale()

    override val lb: Double
        get() = _delegate.get(DblInfo.LB).scale()

    override val ub: Double
        get() = _delegate.get(DblInfo.UB).scale()

    override fun times(coeff: Double): IVar {
        return COPTVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
