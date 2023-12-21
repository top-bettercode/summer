package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Drawing
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
class PoiExcel(private val outputStream: OutputStream) : IExcel {

    val workbook: XSSFWorkbook = XSSFWorkbook()

    lateinit var sheet: XSSFSheet

    private fun XSSFRow.cell(column: Int): XSSFCell =
            this.getCell(column) ?: this.createCell(column)

    private fun XSSFSheet.row(row: Int): XSSFRow = this.getRow(row) ?: this.createRow(row)

    override fun newSheet(sheetname: String) {
        sheet = workbook.createSheet(sheetname)
    }

    override fun createTitle(row: Int, column: Int, title: String, cells: Int, headerStyle: CellStyle) {
        sheet.row(row).cell(column).setCellValue(title)
        val cellStyle = PoiCellStyle(workbook.createCellStyle())
        cellStyle.style(workbook, headerStyle)
        for (i in column until column + cells) {
            sheet.row(row).cell(i).cellStyle = cellStyle.style
        }
    }

    override fun setHeaderStyle(top: Int, left: Int, bottom: Int, right: Int, headerStyle: CellStyle) {
        val cellStyle = PoiCellStyle(workbook.createCellStyle())
        cellStyle.style(workbook, headerStyle)
        for (i in top..bottom) {
            for (j in left..right) {
                sheet.row(i).cell(j).cellStyle = cellStyle.style
            }
        }

    }

    override fun <T : Any> setCellStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle, excelField: ExcelField<T, *>, isFillColor: Boolean, fillColor: String) {
        val poiCellStyle = PoiCellStyle(workbook.createCellStyle())
        poiCellStyle.style(workbook, cellStyle)
        poiCellStyle.style(workbook, excelField.cellStyle)
        if (isFillColor) {
            poiCellStyle.setFillForegroundColor(CellStyle.xssfColor(fillColor))
            poiCellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        for (i in top..bottom) {
            for (j in left..right) {
                sheet.row(i).cell(j).cellStyle = poiCellStyle.style
            }
        }
    }

    override fun width(column: Int, width: Double) {
        sheet.setColumnWidth(column, (width * 256).toInt())
    }

    override fun rowHeight(row: Int, height: Double) {
        sheet.row(row).height = (height * 20).toInt().toShort()
    }

    override fun formula(row: Int, column: Int, expression: String) {
        sheet.row(row).cell(column).setCellFormula(expression)
    }

    override fun comment(row: Int, column: Int, commen: String) {
        val creationHelper: CreationHelper = workbook.creationHelper
        val drawing: Drawing<*> = sheet.createDrawingPatriarch()
        val anchor = creationHelper.createClientAnchor()
        anchor.setCol1(column) // 批注起始列
        anchor.row1 = row // 批注起始行
        val comment: Comment = drawing.createCellComment(anchor)
        val commentText = creationHelper.createRichTextString(commen)
        comment.string = commentText
        sheet.row(row).cell(column).setCellComment(comment)
    }

    override fun value(row: Int, column: Int) {
        this.sheet.row(row).cell(column).setCellValue("")
    }

    override fun value(row: Int, column: Int, value: String) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: Number) {
        this.sheet.row(row).cell(column).setCellValue(value.toDouble())
    }

    override fun value(row: Int, column: Int, value: Boolean) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: Date) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: LocalDateTime) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: LocalDate) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: ZonedDateTime) {
        this.sheet.row(row).cell(column).setCellValue(value.toLocalDateTime())
    }

    override fun dataValidation(row: Int, column: Int, dataValidation: Array<out String>) {
        // 创建数据验证规则
        val validationHelper = sheet.dataValidationHelper
        // 设置下拉列表的可选项
        val constraint = validationHelper.createExplicitListConstraint(dataValidation)
        // 设置数据验证的范围
        val addressList = CellRangeAddressList(row + 1, Worksheet.MAX_ROWS - 1, column, column)
        // 将数据验证对象应用到工作表
        sheet.addValidationData(validationHelper.createValidation(constraint, addressList))
    }

    override fun merge(top: Int, left: Int, bottom: Int, right: Int) {
        val mergedRegion = CellRangeAddress(top, bottom, left, right)
        sheet.addMergedRegion(mergedRegion)
    }

    override fun finish() {
        this.workbook.write(outputStream)
        this.workbook.close()
        this.outputStream.close()
    }
}