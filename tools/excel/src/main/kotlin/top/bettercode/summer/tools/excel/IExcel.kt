package top.bettercode.summer.tools.excel

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
interface IExcel {
    fun newSheet(sheetname: String)
    fun createTitle(row: Int, column: Int, title: String, cells: Int, headerStyle: CellStyle)
    fun setCellStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle)
    fun <T : Any> setCellStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle, excelField: ExcelField<T, *>, isFillColor: Boolean = false, fillColor: String = "")
    fun width(column: Int, width: Double)
    fun rowHeight(row: Int, height: Double)
    fun formula(row: Int, column: Int, expression: String?)
    fun comment(row: Int, column: Int, commen: String?)
    fun value(row: Int, column: Int)
    fun value(row: Int, column: Int, value: String?)
    fun value(row: Int, column: Int, value: Number?)
    fun value(row: Int, column: Int, value: Boolean?)
    fun value(row: Int, column: Int, value: Date?)
    fun value(row: Int, column: Int, value: LocalDateTime?)
    fun value(row: Int, column: Int, value: LocalDate?)
    fun value(row: Int, column: Int, value: ZonedDateTime?)
    fun dataValidation(row: Int, column: Int, dataValidation: Array<out String>)
    fun merge(top: Int, left: Int, bottom: Int, right: Int)
    fun finish()
}
