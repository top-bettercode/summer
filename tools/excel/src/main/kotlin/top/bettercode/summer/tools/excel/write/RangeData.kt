package top.bettercode.summer.tools.excel.write

/**
 * @author Peter Wu
 */
class RangeData<E>(
    row: Int,
    column: Int,
    value: Any?,
    var preValue: Any?,
    entity: E,
    index: Int,
    firstRow: Int,
    firstColumn: Int,
    isLastRow: Boolean,
    isIndex: Boolean,
    val needMerge: Boolean,
    val newRange: Boolean,
    val lastRangeTop: Int,
) : CellData<E>(
    row = row,
    column = column,
    value = if (isIndex) if (needMerge) index else row - firstRow + 1 else value,
    entity = entity,
    isFirstColumn = firstColumn == column,
    isLastRow = isLastRow,
    isFilledColor = index % 2 == 0,
) {

    val lastRangeBottom: Int = if (newRange) row - 1 else row

    val needMergeRange: Boolean

    init {
        val mergeLastRange = (newRange || isLastRow) && lastRangeBottom > lastRangeTop
        needMergeRange = mergeLastRange && needMerge
    }

}
