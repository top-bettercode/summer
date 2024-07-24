package top.bettercode.summer.tools.lang.util

import top.bettercode.summer.tools.lang.serializer.EndOfDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class DateData(

    var date: Date? = null,

    @field:EndOfDay
    var localDate: LocalDate? = null,

    var localDateTime: LocalDateTime? = null,
)