package top.bettercode.summer.tools.excel

/**
 * @author Peter Wu
 */
open class ExcelFieldCell<T>(val row: Int, val column: Int, val isLastRow: Boolean, index: Int, var isFillColor: Boolean,
                             val excelField: ExcelField<T, *>, val entity: T) {
    var cellValue: Any? = null

    constructor(row: Int, column: Int, firstRow: Int, lastRow: Boolean,
                excelField: ExcelField<T, *>, entity: T) : this(row, column, lastRow, row - firstRow + 1, (row - firstRow + 1) % 2 == 0, excelField,
            entity)

    init {
        cellValue = if (excelField.isIndexColumn) {
            index
        }else {
            excelField.toCellValue(entity)
        }
    }

    fun setFillColor(index: Int) {
        isFillColor = index % 2 == 0
    }

    open fun needSetValue(): Boolean {
        return true
    }

    companion object {
        /**
         * 默认日期格式
         */
        const val DEFAULT_DATE_FORMAT = "yyyy-m-dd"

        /**
         * 默认时间格式
         */
        const val DEFAULT_DATE_TIME_FORMAT = "yyyy-m-dd hh:mm"

        /**
         * 默认格式
         */
        const val DEFAULT_FORMAT = "@"
    }
}
