package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont

/**
 *
 * @author Peter Wu
 */
class PoiCellStyle(val style: XSSFCellStyle, var poiFont: XSSFFont? = null) : CellStyle by style