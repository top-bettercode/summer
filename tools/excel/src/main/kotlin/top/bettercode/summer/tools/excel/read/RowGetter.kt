package top.bettercode.summer.tools.excel.read

import top.bettercode.summer.tools.excel.ExcelException

/**
 *
 * @author Peter Wu
 */
class RowGetter<E> @JvmOverloads constructor(val cellGetters: MutableList<CellGetter<E, *>> = mutableListOf()) :
    MutableList<CellGetter<E, *>> by cellGetters {


    val entityType: Class<E> by lazy {
        cellGetters.firstNotNullOfOrNull { it.entityType }
            ?: throw ExcelException("RowGetter识别类型失败")
    }

    fun <P> converter(index: Int, converter: (Any) -> P?): CellGetter<E, P> {
        @Suppress("UNCHECKED_CAST")
        val cellGetter = this[index] as CellGetter<E, P>
        cellGetter.converter(converter)
        return cellGetter
    }

    fun <P> cell(cellGetter: CellGetter<E, P>): RowGetter<E> {
        this.add(cellGetter)
        return this
    }

    fun <P> cell(index: Int, cellGetter: CellGetter<E, P>): RowGetter<E> {
        this[index] = cellGetter
        return this
    }

    companion object {

        @SafeVarargs
        @JvmStatic
        fun <E> of(vararg cellGetter: CellGetter<E, *>): RowGetter<E> {
            return RowGetter(cellGetter.toMutableList())
        }
    }
}