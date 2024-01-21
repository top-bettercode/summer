package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.ProtectionOption
import java.math.BigDecimal

/**
 *
 * @author Peter Wu
 */
class ExportRangeCell @JvmOverloads constructor(
        private val excel: IExcel,
        private val style: CellStyle,
        private val top: Int,
        private val left: Int,
        private val bottom: Int = top,
        private val right: Int = left) {

    fun set(): ExportRangeCell {
        this.excel.setCellStyle(this.top, left, bottom, right, style)
        return this
    }

    fun merge(): ExportRangeCell {
        this.excel.merge(this.top, left, bottom, right)
        return this
    }

    fun style(style: CellStyle): ExportRangeCell {
        this.style.style(style)
        return this
    }

    fun format(numberingFormat: String?): ExportRangeCell {
        style.format(numberingFormat)
        return this
    }

    fun fillColor(rgb: String?): ExportRangeCell {
        style.fillColor(rgb)
        return this
    }

    fun fontColor(rgb: String?): ExportRangeCell {
        style.fontColor(rgb)
        return this
    }

    fun fontName(name: String?): ExportRangeCell {
        style.fontName(name)
        return this
    }

    fun fontSize(size: BigDecimal?): ExportRangeCell {
        style.fontSize(size)
        return this
    }

    fun fontSize(size: Int): ExportRangeCell {
        style.fontSize(size)
        return this
    }

    fun bold(): ExportRangeCell {
        style.bold()
        return this
    }

    fun italic(): ExportRangeCell {
        style.italic()
        return this
    }

    fun underlined(): ExportRangeCell {
        style.underlined()
        return this
    }

    fun horizontalAlignment(alignment: String?): ExportRangeCell {
        style.horizontalAlignment(alignment)
        return this
    }

    fun verticalAlignment(alignment: String?): ExportRangeCell {
        style.verticalAlignment(alignment)
        return this
    }

    fun wrapText(wrapText: Boolean): ExportRangeCell {
        style.wrapText(wrapText)
        return this
    }

    fun rotation(degrees: Int): ExportRangeCell {
        style.rotation(degrees)
        return this
    }

    fun borderStyle(borderStyle: BorderStyle?): ExportRangeCell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(borderStyle: String?): ExportRangeCell {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): ExportRangeCell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: String): ExportRangeCell {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderColor(borderColor: String?): ExportRangeCell {
        style.borderColor(borderColor)
        return this
    }

    fun borderColor(side: BorderSide, borderColor: String): ExportRangeCell {
        style.borderColor(side, borderColor)
        return this
    }

    fun protectionOption(option: ProtectionOption, value: Boolean): ExportRangeCell {
        style.protectionOption(option, value)
        return this
    }

}