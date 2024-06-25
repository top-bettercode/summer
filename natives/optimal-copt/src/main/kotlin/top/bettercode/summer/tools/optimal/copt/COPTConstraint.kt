package top.bettercode.summer.tools.optimal.copt

import copt.Constraint
import top.bettercode.summer.tools.optimal.IConstraint

/**
 *
 * @author Peter Wu
 */
class COPTConstraint(override val delegate: Constraint) : IConstraint<Constraint> {
}