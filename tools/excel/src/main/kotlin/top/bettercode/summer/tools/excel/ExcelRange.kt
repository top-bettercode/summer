package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.ProtectionOption
import java.math.BigDecimal

/**
 *
 * @author Peter Wu
 */
class ExcelRange @JvmOverloads constructor(
        private val excel: IExcel,
        private val style: CellStyle,
        private val top: Int,
        private val left: Int,
        private val bottom: Int = top,
        private val right: Int = left) {

    fun headerStyle(): ExcelRange {
        this.style.headerStyle()
        return this
    }

    fun setStyle(): ExcelRange {
        this.excel.setStyle(this.top, left, bottom, right, style)
        return this
    }

    fun merge(): ExcelRange {
        this.excel.merge(this.top, left, bottom, right)
        return this
    }

    fun style(style: CellStyle): ExcelRange {
        this.style.style(style)
        return this
    }

    fun format(numberingFormat: String?): ExcelRange {
        style.format(numberingFormat)
        return this
    }

    fun fillColor(rgb: String?): ExcelRange {
        style.fillColor(rgb)
        return this
    }

    fun fontColor(rgb: String?): ExcelRange {
        style.fontColor(rgb)
        return this
    }

    fun fontName(name: String?): ExcelRange {
        style.fontName(name)
        return this
    }

    fun fontSize(size: BigDecimal?): ExcelRange {
        style.fontSize(size)
        return this
    }

    fun fontSize(size: Int): ExcelRange {
        style.fontSize(size)
        return this
    }

    fun bold(): ExcelRange {
        style.bold()
        return this
    }

    fun italic(): ExcelRange {
        style.italic()
        return this
    }

    fun underlined(): ExcelRange {
        style.underlined()
        return this
    }

    fun horizontalAlignment(alignment: String?): ExcelRange {
        style.horizontalAlignment(alignment)
        return this
    }

    fun verticalAlignment(alignment: String?): ExcelRange {
        style.verticalAlignment(alignment)
        return this
    }

    @JvmOverloads
    fun wrapText(wrapText: Boolean=true): ExcelRange {
        style.wrapText(wrapText)
        return this
    }

    fun rotation(degrees: Int): ExcelRange {
        style.rotation(degrees)
        return this
    }

    fun borderStyle(borderStyle: BorderStyle?): ExcelRange {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(borderStyle: String?): ExcelRange {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): ExcelRange {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: String): ExcelRange {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderColor(borderColor: String?): ExcelRange {
        style.borderColor(borderColor)
        return this
    }

    fun borderColor(side: BorderSide, borderColor: String): ExcelRange {
        style.borderColor(side, borderColor)
        return this
    }

    fun protectionOption(option: ProtectionOption, value: Boolean): ExcelRange {
        style.protectionOption(option, value)
        return this
    }

}