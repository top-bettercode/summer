package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.ProtectionOption
import top.bettercode.summer.tools.excel.write.style.CellStyle
import java.math.BigDecimal

/**
 *
 * @author Peter Wu
 */
class Range @JvmOverloads constructor(
    private val excel: Excel,
    private val style: CellStyle,
    private val top: Int,
    private val left: Int,
    private val bottom: Int = top,
    private val right: Int = left) {

    fun headerStyle(): Range {
        this.style.headerStyle()
        return this
    }

    fun setStyle(): Range {
        this.excel.setStyle(this.top, left, bottom, right, style)
        return this
    }

    fun merge(): Range {
        this.excel.merge(this.top, left, bottom, right)
        return this
    }

    fun style(style: CellStyle): Range {
        this.style.style(style)
        return this
    }

    fun format(numberingFormat: String): Range {
        style.format(numberingFormat)
        return this
    }

    fun fillColor(rgb: String?): Range {
        style.fillColor(rgb)
        return this
    }

    fun fontColor(rgb: String?): Range {
        style.fontColor(rgb)
        return this
    }

    fun fontName(name: String?): Range {
        style.fontName(name)
        return this
    }

    fun fontSize(size: BigDecimal?): Range {
        style.fontSize(size)
        return this
    }

    fun fontSize(size: Int): Range {
        style.fontSize(size)
        return this
    }

    fun bold(): Range {
        style.bold()
        return this
    }

    fun italic(): Range {
        style.italic()
        return this
    }

    fun underlined(): Range {
        style.underlined()
        return this
    }

    fun horizontalAlignment(alignment: String?): Range {
        style.horizontalAlignment(alignment)
        return this
    }

    fun verticalAlignment(alignment: String?): Range {
        style.verticalAlignment(alignment)
        return this
    }

    @JvmOverloads
    fun wrapText(wrapText: Boolean=true): Range {
        style.wrapText(wrapText)
        return this
    }

    fun borderStyle(borderStyle: BorderStyle?): Range {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(borderStyle: String?): Range {
        style.borderStyle(borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): Range {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderStyle(side: BorderSide, borderStyle: String): Range {
        style.borderStyle(side, borderStyle)
        return this
    }

    fun borderColor(borderColor: String?): Range {
        style.borderColor(borderColor)
        return this
    }

    fun borderColor(side: BorderSide, borderColor: String): Range {
        style.borderColor(side, borderColor)
        return this
    }

    fun protectionOption(option: ProtectionOption, value: Boolean): Range {
        style.protectionOption(option, value)
        return this
    }

}