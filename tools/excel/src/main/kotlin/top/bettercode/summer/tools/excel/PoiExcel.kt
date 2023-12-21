package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Drawing
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.*
import org.dhatim.fastexcel.Worksheet
import top.bettercode.summer.tools.excel.CellStyle.Companion.style
import java.io.InputStream
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
        this.sheet = workbook.createSheet(sheetname)
    }

    override fun setCellStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle) {
        val poiCellStyle = PoiCellStyle(workbook.createCellStyle())
        poiCellStyle.style(workbook, cellStyle)
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

    override fun formula(row: Int, column: Int, expression: String?) {
        sheet.row(row).cell(column).setCellFormula(expression)
    }

    override fun comment(row: Int, column: Int, commen: String?) {
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
        this.sheet.row(row).cell(column).setBlank()
    }

    override fun value(row: Int, column: Int, value: String?) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: Number?) {
        if (value == null) {
            this.sheet.row(row).cell(column).setBlank()
        } else
            this.sheet.row(row).cell(column).setCellValue(value.toDouble())
    }

    override fun value(row: Int, column: Int, value: Boolean?) {
        if (value == null) {
            this.sheet.row(row).cell(column).setBlank()
        } else
            this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: Date?) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: LocalDateTime?) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: LocalDate?) {
        this.sheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: ZonedDateTime?) {
        this.sheet.row(row).cell(column).setCellValue(value?.toLocalDateTime())
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

    companion object {
        val imageSetter: ((PoiExcel, ExcelCell<*>) -> Unit) = { excel, cell ->
            val workbook = excel.workbook
            val sheet = excel.sheet
            val helper: CreationHelper = workbook.creationHelper
            val drawing: Drawing<XSSFShape> = sheet.createDrawingPatriarch()
            if (cell is ExcelRangeCell<*> && cell.excelField.isMerge) {
                var lastRangeTop = cell.lastRangeTop
                val rowHeight = cell.lastRangeBottom - lastRangeTop
                if (rowHeight > 1) {
                    lastRangeTop += rowHeight / 2
                }
                if (cell.newRange) {
                    drawImage(cell.preCellValue, workbook, drawing, helper, cell.column,
                            lastRangeTop,
                            lastRangeTop + 1)
                    if (cell.isLastRow) {
                        drawImage(cell.cellValue, workbook, drawing, helper, cell.column,
                                cell.row,
                                cell.row + 1)
                    }
                } else if (cell.isLastRow) {
                    drawImage(cell.cellValue, workbook, drawing, helper, cell.column,
                            lastRangeTop,
                            lastRangeTop + 1)
                }
            } else {
                drawImage(cell.cellValue, workbook, drawing, helper, cell.column,
                        cell.row,
                        cell.row + 1)
            }
        }

        private fun drawImage(cellValue: Any?, wb: XSSFWorkbook, drawing: Drawing<XSSFShape>,
                              helper: CreationHelper, column: Int, top: Int, bottom: Int) {
            if (cellValue == null || "" == cellValue) {
                return
            }
            val pictureIdx: Int = when (cellValue) {
                is ByteArray -> {
                    wb.addPicture(cellValue as ByteArray?, XSSFWorkbook.PICTURE_TYPE_PNG)
                }

                is InputStream -> {
                    wb.addPicture(cellValue as InputStream?, XSSFWorkbook.PICTURE_TYPE_PNG)
                }

                is ByteArrayPicture -> {
                    wb.addPicture(cellValue.data, cellValue.pictureType)
                }

                is InputStreamPicture -> {
                    wb.addPicture(cellValue.data, cellValue.pictureType)
                }

                else -> {
                    throw ExcelException(
                            "图像单元格数据:" + cellValue + "不是有效输入格式（byte[] or InputStream）")
                }
            }
            val anchor = helper.createClientAnchor()
            anchor.setCol1(column)
            anchor.row1 = top
            anchor.setCol2(column + 1)
            anchor.row2 = bottom
            drawing.createPicture(anchor, pictureIdx)
        }
    }
}