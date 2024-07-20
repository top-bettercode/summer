package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.AbsoluteListDataValidation
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
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
    lateinit var sheet: Worksheet
        private set

    override fun sheet(sheetname: String) {
        this.sheet = workbook.newWorksheet(sheetname)
    }

    override fun keepInActiveTab() {
        this.sheet.keepInActiveTab()
    }

    override fun setStyle(row: Int, column: Int, cellStyle: CellStyle) {
        sheet.style(row, column)
                .style(cellStyle)
                .set()
    }

    override fun setStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle) {
        sheet.range(top, left, bottom, right).style()
                .style(cellStyle)
                .set()
    }

    override fun width(column: Int, width: Double) {
        this.sheet.width(column, width)
    }

    override fun height(row: Int, height: Double) {
        this.sheet.rowHeight(row, height)
    }

    override fun formula(row: Int, column: Int, expression: String?) {
        sheet.formula(row, column, expression)
    }

    override fun comment(row: Int, column: Int, commen: String?) {
        if (!commen.isNullOrBlank())
            sheet.comment(row, column, commen)
    }

    override fun value(row: Int, column: Int, value: String?) {
        this.sheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int) {
        this.sheet.value(row, column)
    }

    override fun value(row: Int, column: Int, value: Number?) {
        this.sheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: Boolean?) {
        this.sheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: Date?) {
        this.sheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: LocalDateTime?) {
        this.sheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: LocalDate?) {
        this.sheet.value(row, column, value)
    }

    override fun value(row: Int, column: Int, value: ZonedDateTime?) {
        this.sheet.value(row, column, value)
    }

    override fun dataValidation(row: Int, column: Int, dataValidation: Array<out String>) {
        val listDataValidation = AbsoluteListDataValidation(
                sheet.range(row + 1, column, Worksheet.MAX_ROWS - 1, column), dataValidation)
        listDataValidation.add(sheet)
    }

    override fun merge(top: Int, left: Int, bottom: Int, right: Int) {
        this.sheet.range(top, left, bottom, right).merge()
    }

    override fun close() {
        this.workbook.close()
    }
}