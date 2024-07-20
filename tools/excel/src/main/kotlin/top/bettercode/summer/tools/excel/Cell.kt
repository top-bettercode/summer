package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.ProtectionOption
import top.bettercode.summer.tools.excel.write.style.CellStyle
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class Cell(
    private val excel: Excel,
    private val style: CellStyle,
    private val row: Int,
    private val column: Int) {

    fun headerStyle(): Cell {
        this.style.headerStyle()
        return this
    }

    fun setStyle(): Cell {
        this.excel.setStyle(this.row, this.column, style)
        return this
    }

    fun width(width: Double): Cell {
        this.excel.width(this.column, width)
        return this
    }

    fun height(height: Double): Cell {
        this.excel.height(this.row, height)
        return this
    }

    fun formula(expression: String?): Cell {
        this.excel.formula(this.row, this.column, expression)
        return this
    }

    fun comment(commen: String?): Cell {
        this.excel.comment(this.row, this.column, commen)
        return this
    }

    fun value(): Cell {
        this.excel.value(this.row, this.column)
        return this
    }

    fun value(value: String?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Number?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Boolean?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: Date?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: LocalDateTime?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: LocalDate?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun value(value: ZonedDateTime?): Cell {
        this.excel.value(this.row, this.column, value)
        return this
    }

    fun dataValidation(dataValidation: Array<out String>): Cell {
        this.excel.dataValidation(this.row, this.column, dataValidation)
        return this
    }

    fun style(style: CellStyle): Cell {
        this.style.style(style)
        return this
    }

    fun format(numberingFormat: String): Cell {
        style.format(numberingFormat)
        return this
    }

    fun fillColor(rgb: String?): Cell {
        style.fillColor(rgb)
        return this
    }

    fun fontColor(rgb: String?): Cell {
        style.fontColor(rgb)
        return this
    }

    fun fontName(name: String?): Cell {
        style.fontName(name)
        return this
    }

    fun fontSize(size: BigDecimal?): Cell {
        style.fontSize(size)
        return this
    }

    fun fontSize(size: Int): Cell {
        style.fontSize(size)
        return this
    }

    fun bold(): Cell {
        style.bold()
        return this
    }

    fun italic(): Cell {
        style.italic()
        return this
    }

    fun underlined(): Cell {
        style.underlined()
        return this
    }

    fun horizontalAlignment(alignment: String?): Cell {
        style.horizontalAlignment(alignment)
        return this
    }

    fun verticalAlignment(alignment: String?): Cell {
        style.verticalAlignment(alignment)
        return this
    }

    @JvmOverloads
    fun wrapText(wrapText: Boolean = true): Cell {
        style.wrapText(wrapText)
        return this
    }

    fun borderStyle(borderStyle: BorderStyle?): Cell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(borderStyle: String?): Cell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): Cell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: String): Cell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderColor(borderColor: String?): Cell {
        style.borderColor(borderColor)
        return this
    }

    fun borderColor(side: BorderSide, borderColor: String): Cell {
        style.borderColor(side, borderColor)
        return this
    }

    fun protectionOption(option: ProtectionOption, value: Boolean): Cell {
        style.protectionOption(option, value)
        return this
    }

}