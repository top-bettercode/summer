package top.bettercode.summer.tools.optimal.gurobi

import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBModel
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class GurobiObjectiveVar(
        val model: GRBModel,
        override val isInt: Boolean = false,
        override val coeff: Double = 1.0
) : IVar {

    override val value: Double
        get() = model[GRB.DoubleAttr.ObjVal].scale()

    override var lb: Double
        get() = model[GRB.DoubleAttr.ObjVal].scale()
        set(_) {}

    override var ub: Double
        get() = model[GRB.DoubleAttr.ObjVal].scale()
        set(_) {}


    override fun times(coeff: Double): IVar {
        return GurobiObjectiveVar(model = model, isInt = isInt, coeff = coeff)
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return model as T
    }

}
