package top.bettercode.summer.tools.excel

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class ExcelImportException(
    message: String?,
    val errors: List<CellError>, e: Throwable?
) : RuntimeException(
    errors.joinToString(",") { "${it.columnName}${it.row}:[${it.value}]$message" },
    e
) {

    class CellError
    /**
     * @param row       行号
     * @param column    列号
     * @param excelField    excelField
     * @param value     表格单元格值
     * @param exception 异常
     */(
        val row: Int,
        val column: Int,
        val excelField: ExcelField<*, *>,
        value: Any?,
        val exception: Exception
    ) {

        val title: String by lazy {
            excelField.title
        }

        private val formatting by lazy { excelField.cellStyle.valueFormatting!! }

        val value: String? by lazy {
            value?.let {
                when (it) {
                    is LocalDateTime -> {
                        it.format(DateTimeFormatter.ofPattern(formatting))
                    }

                    is LocalDate -> {
                        it.format(DateTimeFormatter.ofPattern(formatting))
                    }

                    is Date -> {
                        TimeUtil.of(it).format(formatting)
                    }

                    is ZonedDateTime -> {
                        it.toLocalDateTime().format(DateTimeFormatter.ofPattern(formatting))
                    }

                    else -> {
                        it.toString()
                    }
                }
            }
        }

        val columnName: String by lazy {
            var i = column
            val chars = StringBuilder()
            do {
                chars.append(('A'.code + i % 26).toChar())
            } while ((i / 26 - 1).also { i = it } >= 0)
            chars.reverse().toString()
        }

    }

}
