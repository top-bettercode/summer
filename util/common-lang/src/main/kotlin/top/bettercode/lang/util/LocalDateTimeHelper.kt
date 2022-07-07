package top.bettercode.lang.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

/**
 * @author Peter Wu
 */
class LocalDateTimeHelper private constructor(
    private val localDateTime: LocalDateTime
) {

    /**
     * 取得当月第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfMonth: LocalDateTimeHelper by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfMonth()))
    }

    /**
     * 取得下月第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfNextMonth: LocalDateTimeHelper by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfNextMonth()))
    }

    /**
     * 取得当月最后一天
     *
     * @return LocalDateHelper
     */
    val lastDayOfMonth: LocalDateTimeHelper by lazy {
        of(localDateTime.with(TemporalAdjusters.lastDayOfMonth()))
    }

    /**
     * 取得当季度第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfQuarter: LocalDateTimeHelper by lazy {
        of(
            localDateTime.withMonth(localDateTime.month.firstMonthOfQuarter().value)
                .with(TemporalAdjusters.firstDayOfMonth())
        )
    }

    /**
     * 取得下季度第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfNextQuarter: LocalDateTimeHelper by lazy {
        of(
            localDateTime.withMonth(localDateTime.month.firstMonthOfQuarter().plus(3).value)
                .with(TemporalAdjusters.firstDayOfMonth())
        )
    }

    /**
     * 取得当季度最后一天
     *
     * @return LocalDateHelper
     */
    val lastDayOfQuarter: LocalDateTimeHelper by lazy {
        of(
            localDateTime
                .withMonth(localDateTime.month.firstMonthOfQuarter().plus(2).value)
                .with(TemporalAdjusters.lastDayOfMonth())
        )
    }

    /**
     * 获取当年的第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfYear: LocalDateTimeHelper by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfYear()))
    }

    /**
     * 获取下年的第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfNextYear: LocalDateTimeHelper by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfNextYear()))
    }

    /**
     * 获取当年的最后一天
     *
     * @return LocalDateHelper
     */
    val lastDayOfYear: LocalDateTimeHelper by lazy {
        of(localDateTime.with(TemporalAdjusters.lastDayOfYear()))
    }

    //--------------------------------------------
    fun toMillis(): Long {
        return toInstant().toEpochMilli()
    }

    fun toDate(): Date {
        return Date.from(toInstant())
    }

    @JvmOverloads
    fun toInstant(zoneId: ZoneId = DEFAULT_ZONE_ID): Instant {
        return localDateTime.atZone(zoneId).toInstant()
    }

    fun toLocalDateTime(): LocalDateTime {
        return localDateTime
    }

    fun toLocalDate(): LocalDate {
        return localDateTime.toLocalDate()
    }

    @JvmOverloads
    fun format(dateTimeFormatter: DateTimeFormatter = dateFormatter): String {
        return localDateTime.format(dateTimeFormatter)
    }

    fun format(dateFormatPattern: String = LocalDateTimeHelper.dateFormatPattern): String {
        return format(DateTimeFormatter.ofPattern(dateFormatPattern))
    }

    companion object {

        private val DEFAULT_ZONE_ID = ZoneId.systemDefault()
        private const val dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS"
        private val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)

        @JvmStatic
        fun format(localDateTime: LocalDateTime): String {
            return localDateTime.format(dateFormatter)
        }

        @JvmStatic
        fun format(timeStamp: Long): String {
            return of(timeStamp).format(dateFormatter)
        }

        @JvmStatic
        fun now(): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDateTime.now())
        }

        @JvmStatic
        fun parse(text: CharSequence): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDateTime.parse(text))
        }

        @JvmStatic
        fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDateTime.parse(text, formatter))
        }

        @JvmStatic
        fun of(year: Int, month: Int, dayOfMonth: Int): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDate.of(year, month, dayOfMonth).atStartOfDay())
        }

        @JvmStatic
        @JvmOverloads
        fun of(instant: Instant, zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDateTimeHelper {
            return of(LocalDateTime.ofInstant(instant, zoneId))
        }

        @JvmStatic
        fun of(localDateTime: LocalDateTime): LocalDateTimeHelper {
            return LocalDateTimeHelper(localDateTime)
        }

        @JvmStatic
        fun of(localDate: LocalDate): LocalDateTimeHelper {
            return LocalDateTimeHelper(localDate.atStartOfDay())
        }

        @JvmStatic
        fun of(date: Date): LocalDateTimeHelper {
            return of(date.toInstant())
        }

        @JvmStatic
        fun of(millis: Long): LocalDateTimeHelper {
            return of(Instant.ofEpochMilli(millis))
        }

        @JvmStatic
        fun of(calendar: Calendar): LocalDateTimeHelper {
            return LocalDateTimeHelper(
                LocalDateTime.ofInstant(calendar.toInstant(), calendar.timeZone.toZoneId())
            )
        }

        @JvmOverloads
        @JvmStatic
        fun toDate(localDate: LocalDate, zoneId: ZoneId = DEFAULT_ZONE_ID): Date {
            return Date.from(localDate.atStartOfDay(zoneId).toInstant())
        }

        @JvmOverloads
        @JvmStatic
        fun toDate(localDateTime: LocalDateTime, zoneId: ZoneId = DEFAULT_ZONE_ID): Date {
            return Date.from(localDateTime.atZone(zoneId).toInstant())
        }

    }

}
