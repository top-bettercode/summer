package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Drawing
import org.apache.poi.xssf.usermodel.XSSFShape
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Peter Wu
 */
object ExcelImageCellWriterUtil {
    fun setImage(sheetName: String?, imageCells: List<ExcelCell<*>>,
                 inputStream: InputStream?, outputStream: OutputStream?) {
        try {
            val wb = XSSFWorkbook(inputStream)
            val sheet = wb.getSheet(sheetName)
            val helper: CreationHelper = wb.creationHelper
            val drawing: Drawing<XSSFShape> = sheet.createDrawingPatriarch()
            for (cell in imageCells) {
                if (cell is ExcelRangeCell<*> && cell.excelField.isMerge) {
                    var lastRangeTop = cell.lastRangeTop
                    val rowHeight = cell.lastRangeBottom - lastRangeTop
                    if (rowHeight > 1) {
                        lastRangeTop += rowHeight / 2
                    }
                    if (cell.newRange) {
                        drawImage(cell.preCellValue, wb, drawing, helper, cell.column,
                                lastRangeTop,
                                lastRangeTop + 1)
                        if (cell.isLastRow) {
                            drawImage(cell.cellValue, wb, drawing, helper, cell.column,
                                    cell.row,
                                    cell.row + 1)
                        }
                    } else if (cell.isLastRow) {
                        drawImage(cell.cellValue, wb, drawing, helper, cell.column,
                                lastRangeTop,
                                lastRangeTop + 1)
                    }
                } else {
                    drawImage(cell.cellValue, wb, drawing, helper, cell.column,
                            cell.row,
                            cell.row + 1)
                }
            }
            wb.write(outputStream)
            wb.close()
            outputStream!!.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
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
