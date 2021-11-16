package top.bettercode.lang.util

import java.lang.Integer.max
import java.lang.Integer.min

/**
 *
 * @author Peter Wu
 */
object CollectionUtil {

    @JvmStatic
    fun <T> cut(list: List<T>, maxLength: Int): List<List<T>> {
        val listSize = list.size
        return if (listSize > maxLength) {
            val result = mutableListOf<List<T>>()
            val size =
                if (listSize % maxLength == 0) listSize / maxLength else listSize / maxLength + 1
            for (i in 0 until size) {
                result.add(list.subList(i * maxLength, min((i + 1) * maxLength, listSize)))
            }
            result
        } else {
            listOf(list)
        }
    }
}