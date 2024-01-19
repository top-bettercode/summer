package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 *
 * @author Peter Wu
 */
data class SolutionVar(private val delegate: IVar,
                       var normalDelegate: IVar? = null,
                       var overdoseDelegate: IVar? = null) : IVar by delegate {

    fun solve(): SolutionValue {
        return SolutionValue(delegate.value, normalDelegate?.value, overdoseDelegate?.value)
    }
}

