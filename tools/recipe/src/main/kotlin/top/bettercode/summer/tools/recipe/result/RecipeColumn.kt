package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.optimal.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class RecipeColumns : ArrayList<Any>() {

    override fun add(element: Any): Boolean {
        return when (element) {
            is Int -> super.add(element.toString())
            is Double -> super.add(element.scale(6).toBigDecimal().stripTrailingZeros().toPlainString())
            else -> super.add(element)
        }
    }

    override fun add(index: Int, element: Any) {
        when (element) {
            is Int -> super.add(index, element.toString())
            is Double -> super.add(
                index,
                element.scale(6).toBigDecimal().stripTrailingZeros().toPlainString()
            )

            else -> super.add(index, element)
        }
    }

    override fun get(index: Int): String {
        return valueOf(super.get(index))
    }

    val isDiff: Boolean
        get() {
            return !this.filterIsInstance<Boolean>().all { it }
        }

    val width: Int
        get() {
            return maxOf { valueOf(it).length }
        }

    private fun valueOf(it: Any) = when (it) {
        is Boolean -> if (it) "=" else "≠"
        else -> it.toString()
    }
}