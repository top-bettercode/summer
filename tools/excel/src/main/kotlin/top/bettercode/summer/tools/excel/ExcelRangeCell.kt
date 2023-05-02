package top.bettercode.summer.tools.excel

/**
 * @author Peter Wu
 */
class ExcelRangeCell<T>(row: Int, column: Int, index: Int, firstRow: Int, lastRow: Boolean,
                        private val newRange: Boolean, val lastRangeTop: Int,
                        excelField: ExcelField<T, *>, preEntity: T?, entity: T) : ExcelCell<T>(row, column, lastRow, if (excelField.isMerge) index else row - firstRow + 1, index % 2 == 0,
        excelField, entity) {
    val lastRangeBottom: Int

    //--------------------------------------------
    var preCellValue: Any? = null
    private val needSetValue: Boolean
    private val needRange: Boolean

    init {
        lastRangeBottom = if (newRange && index > 1) row - 1 else row
        val mergeLastRange = (newRange || lastRow) && lastRangeBottom > lastRangeTop
        needRange = mergeLastRange && excelField.isMerge
        needSetValue = !excelField.isMerge || newRange
        preCellValue = if (!excelField.isIndexColumn && newRange && preEntity != null) {
            excelField.toCellValue(preEntity)
        } else {
            null
        }
    }

    //--------------------------------------------
    fun newRange(): Boolean {
        return newRange
    }

    override fun needSetValue(): Boolean {
        return needSetValue
    }

    fun needRange(): Boolean {
        return needRange
    }
}
