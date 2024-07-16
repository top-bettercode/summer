package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.ProtectionOption
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class ExcelCell(
        private val excel: IExcel,
        private val style: CellStyle,
        private val row: Int,
        private val column: Int) {

    fun headerStyle(): ExcelCell {
        this.style.headerStyle()
        return this
    }

    fun setStyle(): ExcelCell {
        this.excel.setStyle(this.row, this.column, style)
        return this
    }

    fun width(width: Double): ExcelCell {
        this.excel.width(this.column, width)
        return this
    }

    fun height(height: Double): ExcelCell {
        this.excel.height(this.row, height)
        return this
    }

    fun formula(expression: String?): ExcelCell {
        this.excel.formula(this.row, this.column, expression)
        return this
    }

    fun comment(commen: String?): ExcelCell {
        this.excel.comment(this.row, this.column, commen)
        return this
    }

    fun value(): ExcelCell {
        this.excel.value(this.row, this.column)
        return this
    }

    fun value(value: String?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Number?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Boolean?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Date?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: LocalDateTime?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: LocalDate?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: ZonedDateTime?): ExcelCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun dataValidation(dataValidation: Array<out String>): ExcelCell {
        this.excel.dataValidation(this.row, this.column, dataValidation)
        return this
    }

    fun style(style: CellStyle): ExcelCell {
        this.style.style(style)
        return this
    }

    fun format(numberingFormat: String?): ExcelCell {
        style.format(numberingFormat)
        return this
    }

    fun fillColor(rgb: String?): ExcelCell {
        style.fillColor(rgb)
        return this
    }

    fun fontColor(rgb: String?): ExcelCell {
        style.fontColor(rgb)
        return this
    }

    fun fontName(name: String?): ExcelCell {
        style.fontName(name)
        return this
    }

    fun fontSize(size: BigDecimal?): ExcelCell {
        style.fontSize(size)
        return this
    }

    fun fontSize(size: Int): ExcelCell {
        style.fontSize(size)
        return this
    }

    fun bold(): ExcelCell {
        style.bold()
        return this
    }

    fun italic(): ExcelCell {
        style.italic()
        return this
    }

    fun underlined(): ExcelCell {
        style.underlined()
        return this
    }

    fun horizontalAlignment(alignment: String?): ExcelCell {
        style.horizontalAlignment(alignment)
        return this
    }

    fun verticalAlignment(alignment: String?): ExcelCell {
        style.verticalAlignment(alignment)
        return this
    }

    @JvmOverloads
    fun wrapText(wrapText: Boolean = true): ExcelCell {
        style.wrapText(wrapText)
        return this
    }

    fun borderStyle(borderStyle: BorderStyle?): ExcelCell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(borderStyle: String?): ExcelCell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): ExcelCell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: String): ExcelCell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderColor(borderColor: String?): ExcelCell {
        style.borderColor(borderColor)
        return this
    }

    fun borderColor(side: BorderSide, borderColor: String): ExcelCell {
        style.borderColor(side, borderColor)
        return this
    }

    fun protectionOption(option: ProtectionOption, value: Boolean): ExcelCell {
        style.protectionOption(option, value)
        return this
    }

}