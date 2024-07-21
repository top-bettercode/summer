package top.bettercode.summer.tools.excel.write

/**
 *
 * @author Peter Wu
 */
class RowSetter<E> @JvmOverloads constructor(val cellSetters: MutableList<CellSetter<E, *>> = mutableListOf()) : MutableList<CellSetter<E, *>> by cellSetters {


    val needMerge: Boolean by lazy { cellSetters.any { it.needMerge } }

    companion object {

        @SafeVarargs
        @JvmStatic
        fun <E> of(vararg cellSetter: CellSetter<E, *>): RowSetter<E> {
            return RowSetter(cellSetter.toMutableList())
        }
    }
}