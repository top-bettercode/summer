package top.bettercode.summer.tools.recipe.criteria

import top.bettercode.summer.tools.optimal.IVar

data class UsageVar(
        val normal: IVar,
        val overdose: IVar) {

    fun toUsage(): Usage {
        return Usage(normal = normal.value, overdose = overdose.value)
    }
}
