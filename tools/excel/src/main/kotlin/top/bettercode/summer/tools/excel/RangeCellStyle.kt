package top.bettercode.summer.tools.excel

/**
 *
 * @author Peter Wu
 */
class RangeCellStyle @JvmOverloads constructor(
        private val excel: IExcel,
        private val top: Int,
        private val left: Int,
        private val bottom: Int = top,
        private val right: Int = left) : CellStyle() {

    fun set() {
        this.excel.setCellStyle(this.top, left, bottom, right, this)
    }
}