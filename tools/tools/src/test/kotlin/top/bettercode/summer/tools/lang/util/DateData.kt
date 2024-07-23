package top.bettercode.summer.tools.lang.util

import top.bettercode.summer.tools.lang.serializer.PlusDays
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 *
 * @author Peter Wu
 */
class DateData(

    var date: Date? = null,

    @PlusDays
    var localDate: LocalDate? = null,

    var localDateTime: LocalDateTime? = null,
)