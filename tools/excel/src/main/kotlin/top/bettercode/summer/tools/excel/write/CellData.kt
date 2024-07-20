package top.bettercode.summer.tools.excel.write

/**
 * @author Peter Wu
 */
open class CellData<E>(
    val row: Int,
    val column: Int,
    val value: Any?,
    val entity: E,
    val isFirstColumn: Boolean,
    val isLastRow: Boolean,
    var isFilledColor: Boolean,
) {

    constructor(
        row: Int,
        column: Int,
        value: Any?,
        entity: E,
        firstRow: Int,
        firstColumn: Int,
        isLastRow: Boolean,
        isIndex: Boolean,
    ) : this(
        row = row,
        column = column,
        value = if (isIndex) row - firstRow + 1 else value,
        entity = entity,
        isFirstColumn = firstColumn == column,
        isLastRow = isLastRow,
        isFilledColor = (row - firstRow + 1) % 2 == 0,
    )

    fun setFillColor(index: Int) {
        isFilledColor = index % 2 == 0
    }

}
