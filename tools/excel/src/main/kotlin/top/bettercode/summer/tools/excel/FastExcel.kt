package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.AbsoluteListDataValidation
import org.dhatim.fastexcel.HyperLink
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import top.bettercode.summer.tools.excel.write.HyperlinkType
import top.bettercode.summer.tools.excel.write.style.CellStyle
import top.bettercode.summer.tools.excel.write.style.CellStyle.Companion.style
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class FastExcel(outputStream: OutputStream) : Excel {

    companion object {

        @JvmStatic
        fun of(outputStream: OutputStream): FastExcel {
            return FastExcel(outputStream)
        }

        @JvmStatic
        fun of(filename: String): FastExcel {
            return of(File(filename))
        }

        @JvmStatic
        fun of(file: File): FastExcel {
            return FastExcel(Files.newOutputStream(file.toPath()))
        }
    }

    /**
     * 工作薄对象
     */
    val workbook: Workbook = Workbook(outputStream, "", null)

    /**
     * 工作表对象
     */
    lateinit var worksheet: Worksheet
        private set

    override fun sheet(sheetname: String) {
        this.worksheet = workbook.newWorksheet(sheetname)
    }

    override fun keepInActiveTab() {
        this.worksheet.keepInActiveTab()
    }

    override fun setStyle(row: Int, column: Int, cellStyle: CellStyle) {
        worksheet.style(row, column)
            .style(cellStyle)
            .set()
    }

    override fun setStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle) {
        worksheet.range(top, left, bottom, right).style()
            .style(cellStyle)
            .set()
    }

    override fun width(column: Int, width: Double) {
        this.worksheet.width(column, width)
    }

    override fun height(row: Int, height: Double) {
        this.worksheet.rowHeight(row, height)
    }

    override fun formula(row: Int, column: Int, expression: String?) {
        worksheet.formula(row, column, expression)
    }

    override fun comment(row: Int, column: Int, commen: String?) {
        if (!commen.isNullOrBlank())
            worksheet.comment(row, column, commen)
    }

    override fun value(row: Int, column: Int, value: String?) {
        this.worksheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int) {
        this.worksheet.value(row, column)
    }

    override fun value(row: Int, column: Int, value: Number?) {
        this.worksheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: Boolean?) {
        this.worksheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: Date?) {
        this.worksheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: LocalDateTime?) {
        this.worksheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: LocalDate?) {
        this.worksheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: ZonedDateTime?) {
        this.worksheet.value(row, column, value)
    }

    override fun hyperlink(
        row: Int,
        column: Int,
        hyperlinkType: HyperlinkType,
        displayStr: String,
        linkStr: String,
    ) {
        this.worksheet.hyperlink(
            row, column, when (hyperlinkType) {
                HyperlinkType.DOCUMENT -> HyperLink.internal(linkStr, displayStr)
                else -> HyperLink(linkStr, displayStr)
            }
        )
    }

    override fun dataValidation(row: Int, column: Int, vararg dataValidation: String) {
        val listDataValidation = AbsoluteListDataValidation(
            worksheet.range(row + 1, column, Worksheet.MAX_ROWS - 1, column), dataValidation
        )
        listDataValidation.add(worksheet)
    }

    override fun merge(top: Int, left: Int, bottom: Int, right: Int) {
        this.worksheet.range(top, left, bottom, right).merge()
    }

    override fun close() {
        if (this::worksheet.isInitialized) {
            this.workbook.close()
        }
    }
}