package top.bettercode.summer.tools.recipe.criteria

import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.OptimalUtil.scale

data class UsageVar(
    val normal: IVar,
    val overdose: IVar
) {

    fun toUsage(): Usage {
        return Usage(normal = normal.value.scale(), overdose = overdose.value.scale())
    }
}
