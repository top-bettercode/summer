package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.ortools.CBCSolver

/**
 *
 * @author Peter Wu
 */
class CBCSolverTest : SCIPSolverTest() {

    val epsilon = 1e-4
    override val solver: Solver = CBCSolver(epsilon = epsilon, minEpsilon = epsilon)
}