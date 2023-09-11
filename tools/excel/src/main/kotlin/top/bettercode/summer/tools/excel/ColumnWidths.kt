package top.bettercode.summer.tools.excel

import top.bettercode.summer.tools.lang.util.CharUtil.isChinese
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.collections.set
import kotlin.math.max

/**
 * @author Peter Wu
 */
class ColumnWidths @JvmOverloads constructor(private val maxWidth: Int = 50) {
    private val colWidths: MutableMap<Int, Double> = HashMap()
    fun put(column: Int, `val`: Any?) {
        if (`val` != null) {
            var width = getWidth(`val`)
            width = max(colWidths.getOrDefault(column, 0.0), width)
            colWidths[column] = width
        }
    }

    @JvmOverloads
    fun width(column: Int, max: Int = maxWidth): Double {
        val w = colWidths[column]
        return BigDecimal.valueOf(max.toDouble().coerceAtMost(w ?: 0.0))
                .setScale(2, RoundingMode.UP)
                .toDouble()
    }

    fun clear() {
        colWidths.clear()
    }

    companion object {
        @JvmStatic
        fun getWidth(`val`: Any): Double {
            var width = 0.0
            for (c1 in `val`.toString().toCharArray()) {
                width += if (isChinese(c1)) {
                    1.5
                } else {
                    1.0
                }
            }
            width += 20.0 / 7
            return width
        }
    }
}
