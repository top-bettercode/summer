package top.bettercode.summer.tools.optimal.solver.`var`

import com.google.ortools.sat.CpSolver
import com.google.ortools.sat.IntVar
import kotlin.math.pow

/**
 *
 * @author Peter Wu
 */
class CPVar(private val _delegate: IntVar,
            /**
             * 变量放大倍数
             */
            private val times: Int,

            private val solver: CpSolver,
            override val coeff: Double = 1.0 * 10.0.pow(times)
) : IVar {

    private var dataTimes = times * 2

    override val value: Double
        get() = solver.value(_delegate).toDouble() / 10.0.pow(dataTimes)

    override val lb: Double
        get() = _delegate.domain.min().toDouble() / 10.0.pow(dataTimes)

    override val ub: Double
        get() = _delegate.domain.max().toDouble() / 10.0.pow(dataTimes)

    override fun times(coeff: Double): IVar {
        dataTimes = times
        return CPVar(_delegate = _delegate, times = times, solver = solver, coeff = coeff * 10.0.pow(times))
    }

    override fun <T> getDelegate(): T {
        @Suppress("UNCHECKED_CAST")
        return _delegate as T
    }

}
