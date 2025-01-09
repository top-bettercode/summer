package top.bettercode.summer.tools.excel.write.style

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dhatim.fastexcel.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class CellStyle(var valueFormatting: String = DEFAULT_FORMAT) : Cloneable {

    /**
     * RGB fill color.
     */
    var fillColor: String? = null

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
            format(style.valueFormatting)
            style.fillColor?.let { fillColor(it) }
            style.fontColor?.let { fontColor(it) }
            style.fontName?.let { fontName(it) }
            style.fontSize?.let { fontSize(it) }
            style.bold?.let { if (it) bold() }
            style.italic?.let { if (it) italic() }
            style.underlined?.let { if (it) underlined() }
            style.horizontalAlignment?.let { horizontalAlignment(it) }
            style.verticalAlignment?.let { verticalAlignment(it) }
            style.wrapText?.let { wrapText(it) }
            style.borderStyle?.let { borderStyle(it) }
            style.borderStyleStr?.let { borderStyle(it) }
            style.borderStyles?.forEach { (side, borderStyle) -> borderStyle(side, borderStyle) }
            style.borderStyleStrs?.forEach { (side, borderStyle) -> borderStyle(side, borderStyle) }
            style.borderColor?.let { borderColor(it) }
            style.borderColors?.forEach { (side, borderColor) -> borderColor(side, borderColor) }
            style.protectionOptions?.forEach { (option, value) -> protectionOption(option, value) }
            return this
        }

        @JvmStatic
        fun XSSFCellStyle.style(workbook: XSSFWorkbook, style: CellStyle): XSSFCellStyle {
            style.valueFormatting.let {
                val dataFormat = workbook.createDataFormat()
                val currencyFormat = dataFormat.getFormat(it)
                this.dataFormat = currencyFormat
            }
            style.fillColor?.let {
                // 设置背景颜色
                this.setFillForegroundColor(color(it))
                this.fillPattern = FillPatternType.SOLID_FOREGROUND
            }

            // 设置字体样式
            val font = workbook.createFont()
            style.fontColor?.let {
                font.setColor(color(it))
            }
            style.fontName?.let { font.fontName = it }
            style.fontSize?.let { font.fontHeightInPoints = it.toShort() }
            style.bold?.let { font.bold = it }
            style.italic?.let { font.italic = it }
            style.underlined?.let {
                if (it)
                    font.underline = Font.U_SINGLE
            }
            this.setFont(font)

            style.horizontalAlignment?.let {
                alignment = HorizontalAlignment.valueOf(it.uppercase())
            }
            style.verticalAlignment?.let {
                verticalAlignment = VerticalAlignment.valueOf(it.uppercase())
            }
            style.wrapText?.let { this.wrapText = it }

            style.borderStyle?.let {
                val borderStyle = org.apache.poi.ss.usermodel.BorderStyle.valueOf(it.name)
                this.borderBottom = borderStyle
                this.borderLeft = borderStyle
                this.borderRight = borderStyle
                this.borderTop = borderStyle
            }
            style.borderStyleStr?.let {
                val borderStyle = org.apache.poi.ss.usermodel.BorderStyle.valueOf(it)
                this.borderBottom = borderStyle
                this.borderLeft = borderStyle
                this.borderRight = borderStyle
                this.borderTop = borderStyle
            }
            style.borderStyles?.forEach { (side, borderStyle) ->
                val poiBorderStyle =
                    org.apache.poi.ss.usermodel.BorderStyle.valueOf(borderStyle.name)
                when (side) {
                    BorderSide.TOP -> this.borderTop = poiBorderStyle
                    BorderSide.LEFT -> this.borderLeft = poiBorderStyle
                    BorderSide.BOTTOM -> this.borderBottom = poiBorderStyle
                    BorderSide.RIGHT -> this.borderRight = poiBorderStyle
                    else -> {}
                }
            }
            style.borderStyleStrs?.forEach { (side, borderStyle) ->
                val poiBorderStyle = org.apache.poi.ss.usermodel.BorderStyle.valueOf(borderStyle)
                when (side) {
                    BorderSide.TOP -> this.borderTop = poiBorderStyle
                    BorderSide.LEFT -> this.borderLeft = poiBorderStyle
                    BorderSide.BOTTOM -> this.borderBottom = poiBorderStyle
                    BorderSide.RIGHT -> this.borderRight = poiBorderStyle
                    else -> {}
                }
            }
            style.borderColor?.let {
                val color = color(it)
                setLeftBorderColor(color)
                setTopBorderColor(color)
                setRightBorderColor(color)
                setBottomBorderColor(color)
            }
            style.borderColors?.forEach { (side, borderColor) ->
                val color = color(borderColor)
                when (side) {
                    BorderSide.LEFT -> setLeftBorderColor(color)
                    BorderSide.TOP -> setTopBorderColor(color)
                    BorderSide.RIGHT -> setRightBorderColor(color)
                    BorderSide.BOTTOM -> setBottomBorderColor(color)
                    else -> {}
                }
            }
            style.protectionOptions?.forEach { (option, value) ->
                when (option) {
                    ProtectionOption.HIDDEN -> this.hidden = value
                    ProtectionOption.LOCKED -> this.locked = value
                }
            }

            return this
        }

        fun color(hexColor: String): XSSFColor {
            // 将字符串表示的十六进制颜色转换为RGB值
            val red: Int = hexColor.substring(0, 2).toInt(16)
            val green: Int = hexColor.substring(2, 4).toInt(16)
            val blue: Int = hexColor.substring(4, 6).toInt(16)

            val color = XSSFColor(byteArrayOf(red.toByte(), green.toByte(), blue.toByte()))
            return color
        }

        /**
         * 默认日期格式
         */
        const val DEFAULT_DATE_FORMAT = "yyyy-mm-dd"

        /**
         * 默认时间格式
         */
        const val DEFAULT_DATE_TIME_FORMAT = "yyyy-mm-dd hh:mm:ss"

        /**
         * 默认格式
         */
        const val DEFAULT_FORMAT = "@"

        /**
         * 浅灰色
         */
        const val LIGHT_GRAY = "d9d9d9"

        fun defaultFormat(propertyType: Class<*>?): String {
            return when (propertyType) {
                Int::class.javaObjectType, Int::class.java -> {
                    "0"
                }

                Long::class.javaObjectType, Long::class.java -> {
                    "0"
                }

                Double::class.javaObjectType, Double::class.java -> {
                    "0.00"
                }

                Float::class.javaObjectType, Float::class.java -> {
                    "0.00"
                }

                LocalDate::class.java -> {
                    DEFAULT_DATE_FORMAT
                }

                Date::class.java, LocalDateTime::class.java -> {
                    DEFAULT_DATE_TIME_FORMAT
                }

                else -> {
                    DEFAULT_FORMAT
                }
            }
        }
    }

    @JvmOverloads
    fun headerStyle(headerStyle: Pair<String, String> = LIGHT_GRAY to Color.BLACK): CellStyle {
        fillColor(headerStyle.first)
        fontColor(headerStyle.second)
        bold()
        return this
    }

    fun style(style: CellStyle): CellStyle {
        style.valueFormatting.let { this.valueFormatting = it }
        style.fillColor?.let { this.fillColor = it }
        style.fontColor?.let { this.fontColor = it }
        style.fontName?.let { this.fontName = it }
        style.fontSize?.let { this.fontSize = it }
        style.bold?.let { this.bold = it }
        style.italic?.let { this.italic = it }
        style.underlined?.let { this.underlined = it }
        style.horizontalAlignment?.let { this.horizontalAlignment = it }
        style.verticalAlignment?.let { this.verticalAlignment = it }
        style.wrapText?.let { this.wrapText = it }
        style.borderStyle?.let { this.borderStyle = it }
        style.borderStyleStr?.let { this.borderStyleStr = it }
        style.borderStyles?.let { this.borderStyles = it }
        style.borderStyleStrs?.let { this.borderStyleStrs = it }
        style.borderColor?.let { this.borderColor = it }
        style.borderColors?.let { this.borderColors = it }
        style.protectionOptions?.let { this.protectionOptions = it }
        return this
    }

    /**
     * Set numbering format.
     *
     * @param numberingFormat Numbering format. For more information, refer to
     * [this
 * page](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2010/ee857658(v=office.14)?redirectedfrom=MSDN).
     * @return This style setter.
     */
    fun format(numberingFormat: String): CellStyle {
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

    public override fun clone(): CellStyle {
        return super.clone() as CellStyle
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CellStyle) return false

        if (valueFormatting != other.valueFormatting) return false
        if (fillColor != other.fillColor) return false
        if (bold != other.bold) return false
        if (italic != other.italic) return false
        if (underlined != other.underlined) return false
        if (fontName != other.fontName) return false
        if (fontSize != other.fontSize) return false
        if (fontColor != other.fontColor) return false
        if (horizontalAlignment != other.horizontalAlignment) return false
        if (verticalAlignment != other.verticalAlignment) return false
        if (wrapText != other.wrapText) return false
        if (borderStyle != other.borderStyle) return false
        if (borderStyleStr != other.borderStyleStr) return false
        if (borderStyles != other.borderStyles) return false
        if (borderStyleStrs != other.borderStyleStrs) return false
        if (borderColor != other.borderColor) return false
        if (borderColors != other.borderColors) return false
        if (protectionOptions != other.protectionOptions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = valueFormatting.hashCode()
        result = 31 * result + (fillColor?.hashCode() ?: 0)
        result = 31 * result + (bold?.hashCode() ?: 0)
        result = 31 * result + (italic?.hashCode() ?: 0)
        result = 31 * result + (underlined?.hashCode() ?: 0)
        result = 31 * result + (fontName?.hashCode() ?: 0)
        result = 31 * result + (fontSize?.hashCode() ?: 0)
        result = 31 * result + (fontColor?.hashCode() ?: 0)
        result = 31 * result + (horizontalAlignment?.hashCode() ?: 0)
        result = 31 * result + (verticalAlignment?.hashCode() ?: 0)
        result = 31 * result + (wrapText?.hashCode() ?: 0)
        result = 31 * result + (borderStyle?.hashCode() ?: 0)
        result = 31 * result + (borderStyleStr?.hashCode() ?: 0)
        result = 31 * result + (borderStyles?.hashCode() ?: 0)
        result = 31 * result + (borderStyleStrs?.hashCode() ?: 0)
        result = 31 * result + (borderColor?.hashCode() ?: 0)
        result = 31 * result + (borderColors?.hashCode() ?: 0)
        result = 31 * result + (protectionOptions?.hashCode() ?: 0)
        return result
    }


}