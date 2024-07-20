package top.bettercode.summer.tools.excel.read

import top.bettercode.summer.tools.excel.ExcelException

/**
 *
 * @author Peter Wu
 */
class RowGetter<E>(val cellGetters: List<CellGetter<E, *>>) :
    Iterable<CellGetter<E, *>> by cellGetters {


    val entityType: Class<E> by lazy {
        cellGetters.firstNotNullOfOrNull { it.entityType }
            ?: throw ExcelException("RowGetter识别类型失败")
    }

    companion object {

        @SafeVarargs
        @JvmStatic
        fun <E> of(vararg cellGetter: CellGetter<E, *>): RowGetter<E> {
            return RowGetter(cellGetter.toList())
        }
    }
}