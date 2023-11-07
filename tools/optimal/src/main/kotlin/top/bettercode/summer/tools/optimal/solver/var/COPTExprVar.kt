package top.bettercode.summer.tools.optimal.solver.`var`

import copt.Expr
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class COPTExprVar(private val _delegate: Expr,
                  override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = _delegate.evaluate().scale()

    override val lb: Double
        get() = _delegate.evaluate().scale()

    override val ub: Double
        get() = _delegate.evaluate().scale()

    override fun coeff(coeff: Double): IVar {
        return COPTExprVar(_delegate, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
