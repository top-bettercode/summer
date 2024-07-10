package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font

/**
 *
 * @author Peter Wu
 */
class PoiCellStyle(val style: CellStyle, var poiFont: Font? = null) : CellStyle by style