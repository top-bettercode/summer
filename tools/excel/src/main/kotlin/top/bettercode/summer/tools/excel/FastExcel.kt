package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.AbsoluteListDataValidation
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import top.bettercode.summer.tools.excel.CellStyle.Companion.style
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class FastExcel(outputStream: OutputStream) : IExcel {
    /**
     * 工作薄对象
     */
    val workbook: Workbook = Workbook(outputStream, "", "1.0")

    /**
     * 工作表对象
     */
    lateinit var sheet: Worksheet
        private set

    override fun newSheet(sheetname: String) {
        this.sheet = workbook.newWorksheet(sheetname)
    }


    override fun setCellStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle) {
        sheet.range(top, left, bottom, right).style()
                .style(cellStyle)
                .set()
    }

    override fun width(column: Int, width: Double) {
        this.sheet.width(column, width)
    }

    override fun rowHeight(row: Int, height: Double) {
        this.sheet.rowHeight(row, height)
    }

    override fun formula(row: Int, column: Int, expression: String?) {
        sheet.formula(row, column, expression)
    }

    override fun comment(row: Int, column: Int, commen: String?) {
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

    override fun finish() {
        this.workbook.finish()
    }
}