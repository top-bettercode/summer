package top.bettercode.summer.tools.excel

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ExcelImportException(message: String?,
                           val errors: List<CellError>, e: Throwable?) : Exception(errors.joinToString(",") { "${it.columnName}${it.row}:$message" }, e) {

    class CellError
    /**
     * @param row       行号
     * @param column    列号
     * @param title     表格列名
     * @param value     表格单元格值
     * @param exception 异常
     */(val row: Int, val column: Int, val title: String?, val value: String?,
        val exception: Exception) {
        var message = "excel.cell.typeMismatch"
            private set

        fun setMessage(message: String): CellError {
            this.message = message
            return this
        }

        val columnName: String
            get() {
                var i = column
                val chars = StringBuilder()
                do {
                    chars.append(('A'.code + i % 26).toChar())
                } while ((i / 26 - 1).also { i = it } >= 0)
                return chars.reverse().toString()
            }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
