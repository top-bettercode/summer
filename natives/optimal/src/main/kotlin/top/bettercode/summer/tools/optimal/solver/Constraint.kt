package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 *
 * @author Peter Wu
 */
data class Constraint(val variable: IVar, val sense: Sense, val value: Double)
