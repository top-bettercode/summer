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
class ExportCell(
        private val excel: IExcel,
        private val style: CellStyle,
        private val row: Int,
        private val column: Int) {

    fun set(): ExportCell {
        this.excel.setCellStyle(this.row, this.column, style)
        return this
    }

    fun width(width: Double): ExportCell {
        this.excel.width(this.column, width)
        return this
    }

    fun height(height: Double): ExportCell {
        this.excel.height(this.row, height)
        return this
    }

    fun formula(expression: String?): ExportCell {
        this.excel.formula(this.row, this.column, expression)
        return this
    }

    fun comment(commen: String?): ExportCell {
        this.excel.comment(this.row, this.column, commen)
        return this
    }

    fun value(): ExportCell {
        this.excel.value(this.row, this.column)
        return this
    }

    fun value(value: String?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Number?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Boolean?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Date?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: LocalDateTime?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: LocalDate?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: ZonedDateTime?): ExportCell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun dataValidation(dataValidation: Array<out String>): ExportCell {
        this.excel.dataValidation(this.row, this.column, dataValidation)
        return this
    }

    fun style(style: CellStyle): ExportCell {
        this.style.style(style)
        return this
    }

    fun format(numberingFormat: String?): ExportCell {
        style.format(numberingFormat)
        return this
    }

    fun fillColor(rgb: String?): ExportCell {
        style.fillColor(rgb)
        return this
    }

    fun fontColor(rgb: String?): ExportCell {
        style.fontColor(rgb)
        return this
    }

    fun fontName(name: String?): ExportCell {
        style.fontName(name)
        return this
    }

    fun fontSize(size: BigDecimal?): ExportCell {
        style.fontSize(size)
        return this
    }

    fun fontSize(size: Int): ExportCell {
        style.fontSize(size)
        return this
    }

    fun bold(): ExportCell {
        style.bold()
        return this
    }

    fun italic(): ExportCell {
        style.italic()
        return this
    }

    fun underlined(): ExportCell {
        style.underlined()
        return this
    }

    fun horizontalAlignment(alignment: String?): ExportCell {
        style.horizontalAlignment(alignment)
        return this
    }

    fun verticalAlignment(alignment: String?): ExportCell {
        style.verticalAlignment(alignment)
        return this
    }

    @JvmOverloads
    fun wrapText(wrapText: Boolean=true): ExportCell {
        style.wrapText(wrapText)
        return this
    }

    fun rotation(degrees: Int): ExportCell {
        style.rotation(degrees)
        return this
    }

    fun borderStyle(borderStyle: BorderStyle?): ExportCell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(borderStyle: String?): ExportCell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): ExportCell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: String): ExportCell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderColor(borderColor: String?): ExportCell {
        style.borderColor(borderColor)
        return this
    }

    fun borderColor(side: BorderSide, borderColor: String): ExportCell {
        style.borderColor(side, borderColor)
        return this
    }

    fun protectionOption(option: ProtectionOption, value: Boolean): ExportCell {
        style.protectionOption(option, value)
        return this
    }

}