package top.bettercode.summer.tools.excel.write

import top.bettercode.summer.tools.excel.read.RowGetter

/**
 *
 * @author Peter Wu
 */
class RowSetter<E> @JvmOverloads constructor(val cellSetters: MutableList<CellSetter<E, *>> = mutableListOf()) :
    MutableList<CellSetter<E, *>> by cellSetters {


    val needMerge: Boolean by lazy { cellSetters.any { it.needMerge } }

    fun toGetter(): RowGetter<E> {
        val cellGetters = cellSetters.filterIsInstance<PropertyCellSetter<E, *>>()
            .map { it.toGetter() }.toMutableList()
        return RowGetter(cellGetters)
    }

    companion object {

        @SafeVarargs
        @JvmStatic
        fun <E> of(vararg cellSetter: CellSetter<E, *>): RowSetter<E> {
            return RowSetter(cellSetter.toMutableList())
        }
    }
}