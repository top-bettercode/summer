package top.bettercode.summer.tools.optimal.cplex

import ilog.cplex.IloCplex
import top.bettercode.summer.tools.optimal.IVar

/**
 *
 * @author Peter Wu
 */
class CplexObjectiveVar(
    val model: IloCplex,
    override val isInt: Boolean = false,
    override val coeff: Double = 1.0,
) : IVar {

    override val value: Double
        get() = model.objValue

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
        return CplexObjectiveVar(model = model, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return model as T
    }

}
