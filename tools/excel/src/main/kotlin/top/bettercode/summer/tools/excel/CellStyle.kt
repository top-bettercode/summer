package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.ProtectionOption
import org.dhatim.fastexcel.StyleSetter
import java.math.BigDecimal
import java.util.*

class CellStyle {

    var defaultValueFormatting: String? = null

    /**
     * Value formatting.
     */
    var valueFormatting: String? = null
        get() = field ?: defaultValueFormatting

    /**
     * RGB fill color.
     */
    var fillColor: String? = null

    /**
     * RGB color for shading of alternate rows.
     */
    var alternateShadingFillColor: String? = null

    /**
     * RGB color for shading Nth rows.
     */
    var shadingFillColor: String? = null

    /**
     * Shading row frequency.
     */
    var eachNRows: Int? = null

    /**
     * Bold flag.
     */
    var bold: Boolean? = null

    /**
     * Italic flag.
     */
    var italic: Boolean? = null

    /**
     * Underlined flag.
     */
    var underlined: Boolean? = null

    /**
     * Font name.
     */
    var fontName: String? = null

    /**
     * Font size.
     */
    var fontSize: BigDecimal? = null

    /**
     * RGB font color.
     */
    var fontColor: String? = null

    /**
     * Horizontal alignment.
     */
    var horizontalAlignment: String? = null

    /**
     * Vertical alignment.
     */
    var verticalAlignment: String? = null

    /**
     * Wrap text flag.
     */
    var wrapText: Boolean? = null

    /**
     * Text rotation in degrees
     */
    var rotation: Int? = null


    var borderStyle: BorderStyle? = null
    var borderStyleStr: String? = null
    var borderStyles: MutableMap<BorderSide, BorderStyle>? = null
    var borderStyleStrs: MutableMap<BorderSide, String>? = null
    var borderColor: String? = null
    var borderColors: MutableMap<BorderSide, String>? = null

    /**
     * Protection options.
     */
    var protectionOptions: MutableMap<ProtectionOption, Boolean>? = null

    companion object {

        @JvmStatic
        fun StyleSetter.style(style: CellStyle): StyleSetter {
            style.valueFormatting?.let { format(it) }
            style.fillColor?.let { fillColor(it) }
            style.alternateShadingFillColor?.let { shadeAlternateRows(it) }
            style.shadingFillColor?.let { shadeRows(it, style.eachNRows ?: 0) }
            style.fontColor?.let { fontColor(it) }
            style.fontName?.let { fontName(it) }
            style.fontSize?.let { fontSize(it) }
            style.bold?.let { if (it) bold() }
            style.italic?.let { if (it) italic() }
            style.underlined?.let { if (it) underlined() }
            style.horizontalAlignment?.let { horizontalAlignment(it) }
            style.verticalAlignment?.let { verticalAlignment(it) }
            style.wrapText?.let { wrapText(it) }
            style.rotation?.let { rotation(it) }
            style.borderStyle?.let { borderStyle(it) }
            style.borderStyleStr?.let { borderStyle(it) }
            style.borderStyles?.forEach { (side, borderStyle) -> borderStyle(side, borderStyle) }
            style.borderStyleStrs?.forEach { (side, borderStyle) -> borderStyle(side, borderStyle) }
            style.borderColor?.let { borderColor(it) }
            style.borderColors?.forEach { (side, borderColor) -> borderColor(side, borderColor) }
            style.protectionOptions?.forEach { (option, value) -> protectionOption(option, value) }
            return this
        }
    }

    /**
     * Set numbering format.
     *
     * @param numberingFormat Numbering format. For more information, refer to
     * [this
 * page](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2010/ee857658(v=office.14)?redirectedfrom=MSDN).
     * @return This style setter.
     */
    fun format(numberingFormat: String?): CellStyle {
        valueFormatting = numberingFormat
        return this
    }

    /**
     * Set fill color.
     *
     * @param rgb RGB fill color. See [Color] for predefined values.
     * @return This style setter.
     */
    fun fillColor(rgb: String?): CellStyle {
        fillColor = rgb
        return this
    }

    /**
     * Shade alternate rows.
     *
     * @param rgb RGB shading color.
     * @return This style setter.
     */
    fun shadeAlternateRows(rgb: String?): CellStyle {
        alternateShadingFillColor = rgb
        return this
    }

    /**
     * Shade Nth rows.
     *
     * @param rgb       RGB shading color.
     * @param eachNRows shading frequency.
     * @return This style setter.
     */
    fun shadeRows(rgb: String?, eachNRows: Int): CellStyle {
        shadingFillColor = rgb
        this.eachNRows = eachNRows
        return this
    }

    /**
     * Set font color.
     *
     * @param rgb RGB font color.
     * @return This style setter.
     */
    fun fontColor(rgb: String?): CellStyle {
        fontColor = rgb
        return this
    }

    /**
     * Set font name.
     *
     * @param name Font name.
     * @return This style setter.
     */
    fun fontName(name: String?): CellStyle {
        fontName = name
        return this
    }

    /**
     * Set font size.
     *
     * @param size Font size, in points.
     * @return This style setter.
     */
    fun fontSize(size: BigDecimal?): CellStyle {
        fontSize = size
        return this
    }

    /**
     * Set font size.
     *
     * @param size Font size, in points.
     * @return This style setter.
     */
    fun fontSize(size: Int): CellStyle {
        fontSize = BigDecimal.valueOf(size.toLong())
        return this
    }

    /**
     * Use bold text.
     *
     * @return This style setter.
     */
    fun bold(): CellStyle {
        bold = true
        return this
    }

    /**
     * Use italic text.
     *
     * @return This style setter.
     */
    fun italic(): CellStyle {
        italic = true
        return this
    }

    /**
     * Use underlined text.
     *
     * @return This style setter.
     */
    fun underlined(): CellStyle {
        underlined = true
        return this
    }

    /**
     * Define horizontal alignment.
     *
     * @param alignment Horizontal alignment. Possible values are defined
     * [here](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2010/cc880467(v=office.14)?redirectedfrom=MSDN).
     * @return This style setter.
     */
    fun horizontalAlignment(alignment: String?): CellStyle {
        horizontalAlignment = alignment
        return this
    }

    /**
     * Define vertical alignment.
     *
     * @param alignment Vertical alignment. Possible values are defined
     * [here](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2010/cc802119(v=office.14)?redirectedfrom=MSDN).
     * @return This style setter.
     */
    fun verticalAlignment(alignment: String?): CellStyle {
        verticalAlignment = alignment
        return this
    }

    /**
     * Enable or disable text wrapping in cells.
     *
     * @param wrapText `true` to enable text wrapping (default is `false`).
     * @return This style setter.
     */
    fun wrapText(wrapText: Boolean): CellStyle {
        this.wrapText = wrapText
        return this
    }

    /**
     * Set cell text rotation in degrees.
     *
     * @param degrees rotation of text in cell
     * @return This style setter
     */
    fun rotation(degrees: Int): CellStyle {
        rotation = degrees
        return this
    }


    /**
     * Apply cell border style on all sides, except diagonal.
     *
     * @param borderStyle Border style.
     * @return This style setter.
     */
    fun borderStyle(borderStyle: BorderStyle?): CellStyle {
        this.borderStyle = borderStyle
        return this
    }


    /**
     * Apply cell border style on all sides, except diagonal.
     *
     * @param borderStyle Border style. Possible values are defined
     * [here](https://learn.microsoft.com/en-us/dotnet/api/documentformat.openxml.spreadsheet.borderstylevalues?view=openxml-2.8.1).
     * @return This style setter.
     */
    fun borderStyle(borderStyle: String?): CellStyle {
        borderStyleStr = borderStyle
        return this
    }


    /**
     * Apply cell border style on a side.
     *
     * @param side        Border side where to apply the given style.
     * @param borderStyle Border style.
     * @return This style setter.
     */
    fun borderStyle(side: BorderSide, borderStyle: BorderStyle): CellStyle {
        if (borderStyles == null) {
            borderStyles = EnumMap(BorderSide::class.java)
        }
        borderStyles!![side] = borderStyle
        return this
    }


    /**
     * Apply cell border style on a side.
     *
     * @param side        Border side where to apply the given style.
     * @param borderStyle Border style. Possible values are defined
     * [here](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2010/cc844549(v=office.14)?redirectedfrom=MSDN).
     * @return This style setter.
     */
    fun borderStyle(side: BorderSide, borderStyle: String): CellStyle {
        if (borderStyleStrs == null) {
            borderStyleStrs = EnumMap(BorderSide::class.java)
        }
        borderStyleStrs!![side] = borderStyle
        return this
    }


    /**
     * Set cell border color.
     *
     * @param borderColor RGB border color.
     * @return This style setter.
     */
    fun borderColor(borderColor: String?): CellStyle {
        this.borderColor = borderColor
        return this
    }


    /**
     * Set cell border color.
     *
     * @param side        Border side where to apply the given border color.
     * @param borderColor RGB border color.
     * @return This style setter.
     */
    fun borderColor(side: BorderSide, borderColor: String): CellStyle {
        if (borderColors == null) {
            borderColors = EnumMap(BorderSide::class.java)
        }
        borderColors!![side] = borderColor
        return this
    }

    /**
     * Sets the value for a protection option.
     *
     * @param option The option to set
     * @param value  The value to set for the given option.
     * @return This style setter.
     */
    fun protectionOption(option: ProtectionOption, value: Boolean): CellStyle {
        if (protectionOptions == null) {
            protectionOptions = EnumMap(ProtectionOption::class.java)
        }
        protectionOptions!![option] = value
        return this
    }
}