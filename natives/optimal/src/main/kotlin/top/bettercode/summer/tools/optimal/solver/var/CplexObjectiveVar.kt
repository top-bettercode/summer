package top.bettercode.summer.tools.optimal.solver.`var`

import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class CplexObjectiveVar(
        val model: IloCplex,
        override val isInt: Boolean = false,
        override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = model.objValue.scale()

    override var lb: Double
        get() = model.objValue.scale()
        set(_) {}

    override var ub: Double
        get() = model.objValue.scale()
        set(_) {}


    override fun times(coeff: Double): IVar {
        return CplexObjectiveVar(model = model, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return model as T
    }

}
