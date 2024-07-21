package top.bettercode.summer.tools.excel.read

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
class ExcelReaderException(
    message: String?,
    val errors: List<CellError>, e: Throwable?
) : RuntimeException(
    errors.joinToString(",") { "${it.columnName}${it.row}:[${it.value}]$message" },
    e
) {

    class CellError (
        val row: Int,
        val column: Int,
        val title: String,
        private val dateFormat:String,
        value: Any?,
        val exception: Exception
    ) {

        val value: String? by lazy {
            value?.let {
                when (it) {
                    is LocalDateTime -> {
                        it.format(DateTimeFormatter.ofPattern(dateFormat))
                    }

                    is LocalDate -> {
                        it.format(DateTimeFormatter.ofPattern(dateFormat))
                    }

                    is Date -> {
                        TimeUtil.of(it).format(dateFormat)
                    }

                    is ZonedDateTime -> {
                        it.toLocalDateTime().format(DateTimeFormatter.ofPattern(dateFormat))
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
