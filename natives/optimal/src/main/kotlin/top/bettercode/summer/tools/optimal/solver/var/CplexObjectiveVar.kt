package top.bettercode.summer.tools.optimal.solver.`var`

import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class CplexObjectiveVar(
        val model: IloCplex,
        override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = model.objValue.scale()

    override val lb: Double
        get() = model.objValue.scale()

    override val ub: Double
        get() = model.objValue.scale()


    override fun times(coeff: Double): IVar {
        return CplexObjectiveVar(model, coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return model as T
    }

}
