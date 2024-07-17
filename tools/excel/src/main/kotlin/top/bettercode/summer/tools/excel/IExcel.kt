package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
interface IExcel {

    companion object {
        val defaultStyle: CellStyle
            get() = CellStyle().apply {
                fontColor(Color.BLACK)
                fontName("Arial")
                horizontalAlignment(Alignment.CENTER.value)
                verticalAlignment(Alignment.CENTER.value)
                borderStyle(BorderStyle.THIN)
                borderColor(Color.GRAY8)
            }
    }

    fun sheet(sheetname: String)

    //--------------------------------------------

    fun cell(row: Int, column: Int): ExcelCell {
        return ExcelCell(this, defaultStyle, row, column)
    }

    fun cell(row: Int, column: Int, style: CellStyle): ExcelCell {
        return ExcelCell(this, style, row, column)
    }

    //--------------------------------------------

    fun range(top: Int, left: Int, bottom: Int, right: Int): ExcelRange {
        return ExcelRange(this, defaultStyle, top, left, bottom, right)
    }

    fun range(top: Int, left: Int, bottom: Int, right: Int, style: CellStyle): ExcelRange {
        return ExcelRange(this, style, top, left, bottom, right)
    }

    //--------------------------------------------

    fun setStyle(row: Int, column: Int, cellStyle: CellStyle) {
        setStyle(row, column, row, column, cellStyle)
    }

    fun setStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle)

    //--------------------------------------------

    fun width(column: Int, width: Double)
    fun height(row: Int, height: Double)

    //--------------------------------------------

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

    //--------------------------------------------

    fun merge(top: Int, left: Int, bottom: Int, right: Int)

    //--------------------------------------------

    fun keepInActiveTab()
    fun finish()
}
