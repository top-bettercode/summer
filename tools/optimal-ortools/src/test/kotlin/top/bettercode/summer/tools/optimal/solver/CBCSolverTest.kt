package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.ortools.CBCSolver

/**
 *
 * @author Peter Wu
 */
class CBCSolverTest : SCIPSolverTest() {

    override val solver: Solver = CBCSolver(epsilon = 1e-4)
}