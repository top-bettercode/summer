package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dhatim.fastexcel.Worksheet
import top.bettercode.summer.tools.excel.write.CellData
import top.bettercode.summer.tools.excel.write.RangeData
import top.bettercode.summer.tools.excel.write.picture.ByteArrayPicture
import top.bettercode.summer.tools.excel.write.picture.InputStreamPicture
import top.bettercode.summer.tools.excel.write.style.CellStyle
import top.bettercode.summer.tools.excel.write.style.CellStyle.Companion.style
import java.io.File
import java.io.InputStream
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
class PoiExcel @JvmOverloads constructor(
    private val outputStream: OutputStream,
    useSxss: Boolean = true
) : Excel {

    private val styleCache = mutableMapOf<CellStyle, XSSFCellStyle>()

    val workbook: Workbook = if (useSxss)
        SXSSFWorkbook(1000)
    else
        XSSFWorkbook()

    val xssfWorkbook: XSSFWorkbook = if (useSxss)
        (this.workbook as SXSSFWorkbook).xssfWorkbook
    else
        this.workbook as XSSFWorkbook


    lateinit var worksheet: Sheet

    private fun Row.cell(column: Int): Cell =
        this.getCell(column) ?: this.createCell(column)

    private fun Sheet.row(row: Int): Row = this.getRow(row) ?: this.createRow(row)

    override fun sheet(sheetname: String) {
        this.worksheet = workbook.createSheet(sheetname)
    }

    override fun keepInActiveTab() {
        workbook.setActiveSheet(this.workbook.indexOf(this.worksheet))
    }

    override fun setStyle(top: Int, left: Int, bottom: Int, right: Int, cellStyle: CellStyle) {
        val poiCellStyle = styleCache.getOrPut(cellStyle) {
            (xssfWorkbook.createCellStyle()).style(xssfWorkbook, cellStyle)
        }
        for (i in top..bottom) {
            for (j in left..right) {
                val cell = worksheet.row(i).cell(j)
                cell.cellStyle = poiCellStyle
            }
        }

    }

    override fun width(column: Int, width: Double) {
        worksheet.setColumnWidth(column, (width * 256).toInt())
    }

    override fun height(row: Int, height: Double) {
        worksheet.row(row).height = (height * 20).toInt().toShort()
    }

    override fun formula(row: Int, column: Int, expression: String?) {
        worksheet.row(row).cell(column).cellFormula = expression
    }

    override fun comment(row: Int, column: Int, commen: String?) {
        if (!commen.isNullOrBlank()) {
            val creationHelper: CreationHelper = workbook.creationHelper
            val drawing: Drawing<*> = worksheet.createDrawingPatriarch()
            val anchor = creationHelper.createClientAnchor()
            anchor.setCol1(column) // 批注起始列
            anchor.row1 = row // 批注起始行
            val comment: Comment = drawing.createCellComment(anchor)
            val commentText = creationHelper.createRichTextString(commen)
            comment.string = commentText
            worksheet.row(row).cell(column).cellComment = comment
        }
    }

    override fun value(row: Int, column: Int) {
        this.worksheet.row(row).cell(column).setBlank()
    }

    override fun value(row: Int, column: Int, value: String?) {
        this.worksheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: Number?) {
        if (value == null) {
            this.worksheet.row(row).cell(column).setBlank()
        } else
            this.worksheet.row(row).cell(column).setCellValue(value.toDouble())
    }

    override fun value(row: Int, column: Int, value: Boolean?) {
        if (value == null) {
            this.worksheet.row(row).cell(column).setBlank()
        } else
            this.worksheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: Date?) {
        this.worksheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: LocalDateTime?) {
        this.worksheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: LocalDate?) {
        this.worksheet.row(row).cell(column).setCellValue(value)
    }

    override fun value(row: Int, column: Int, value: ZonedDateTime?) {
        this.worksheet.row(row).cell(column).setCellValue(value?.toLocalDateTime())
    }

    override fun dataValidation(row: Int, column: Int, vararg dataValidation: String) {
        // 创建数据验证规则
        val validationHelper = worksheet.dataValidationHelper
        // 设置下拉列表的可选项
        val constraint = validationHelper.createExplicitListConstraint(dataValidation)
        // 设置数据验证的范围
        val addressList = CellRangeAddressList(row + 1, Worksheet.MAX_ROWS - 1, column, column)
        // 将数据验证对象应用到工作表
        worksheet.addValidationData(validationHelper.createValidation(constraint, addressList))
    }

    override fun merge(top: Int, left: Int, bottom: Int, right: Int) {
        val mergedRegion = CellRangeAddress(top, bottom, left, right)
        worksheet.addMergedRegion(mergedRegion)
    }

    override fun close() {
        if (this::worksheet.isInitialized) {
            this.workbook.write(outputStream)
            this.workbook.close()
            this.outputStream.close()
        }
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(outputStream: OutputStream, useSxss: Boolean = true): PoiExcel {
            return PoiExcel(outputStream = outputStream, useSxss = useSxss)
        }

        @JvmStatic
        @JvmOverloads
        fun of(filename: String, useSxss: Boolean = true): PoiExcel {
            return of(file = File(filename), useSxss = useSxss)
        }

        @JvmStatic
        @JvmOverloads
        fun of(file: File, useSxss: Boolean = true): PoiExcel {
            return PoiExcel(outputStream = Files.newOutputStream(file.toPath()), useSxss = useSxss)
        }

        val imageSetter: ((PoiExcel, CellData<*>) -> Unit) = { excel, cell ->
            val workbook = excel.workbook
            val sheet = excel.worksheet
            val helper: CreationHelper = workbook.creationHelper
            val drawing: Drawing<*> = sheet.createDrawingPatriarch()
            if (cell is RangeData && cell.needMerge) {
                var lastRangeTop = cell.lastRangeTop
                val rowHeight = cell.lastRangeBottom - lastRangeTop
                if (rowHeight > 1) {
                    lastRangeTop += rowHeight / 2
                }
                if (cell.newRange) {
                    drawImage(
                        value = cell.preValue,
                        wb = workbook,
                        drawing = drawing,
                        helper = helper,
                        column = cell.column,
                        top = lastRangeTop,
                        bottom = lastRangeTop + 1
                    )
                    if (cell.isLastRow) {
                        drawImage(
                            value = cell.value,
                            wb = workbook,
                            drawing = drawing,
                            helper = helper,
                            column = cell.column,
                            top = cell.row,
                            bottom = cell.row + 1
                        )
                    }
                } else if (cell.isLastRow) {
                    drawImage(
                        value = cell.value,
                        wb = workbook,
                        drawing = drawing,
                        helper = helper,
                        column = cell.column,
                        top = lastRangeTop,
                        bottom = lastRangeTop + 1
                    )
                }
            } else {
                drawImage(
                    value = cell.value,
                    wb = workbook,
                    drawing = drawing,
                    helper = helper,
                    column = cell.column,
                    top = cell.row,
                    bottom = cell.row + 1
                )
            }
        }

        private fun drawImage(
            value: Any?, wb: Workbook, drawing: Drawing<*>,
            helper: CreationHelper, column: Int, top: Int, bottom: Int
        ) {
            if (value == null || "" == value) {
                return
            }
            val pictureIdx: Int = when (value) {
                is ByteArray -> {
                    wb.addPicture(value, Workbook.PICTURE_TYPE_PNG)
                }

                is InputStream -> {
                    wb.addPicture(
                        value.readBytes(),
                        Workbook.PICTURE_TYPE_PNG
                    )
                }

                is ByteArrayPicture -> {
                    wb.addPicture(value.data ?: return, value.pictureType)
                }

                is InputStreamPicture -> {
                    wb.addPicture(value.data?.readBytes() ?: return, value.pictureType)
                }

                else -> {
                    throw ExcelException(
                        "图像单元格数据:[$value]不是有效输入格式（byte[] or InputStream）"
                    )
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