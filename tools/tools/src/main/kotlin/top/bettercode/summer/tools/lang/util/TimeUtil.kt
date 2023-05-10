package top.bettercode.summer.tools.lang.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.*

/**
 * @author Peter Wu
 */
open class TimeUtil(
        private val localDateTime: LocalDateTime
) {

    /**
     * 取得当月第一天
     *
     * @return TimeHelper
     */
    val firstDayOfMonth: TimeUtil by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfMonth()))
    }

    /**
     * 取得下月第一天
     *
     * @return TimeHelper
     */
    val firstDayOfNextMonth: TimeUtil by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfNextMonth()))
    }

    /**
     * 取得当月最后一天
     *
     * @return TimeHelper
     */
    val lastDayOfMonth: TimeUtil by lazy {
        of(localDateTime.with(TemporalAdjusters.lastDayOfMonth()))
    }

    /**
     * 取得当季度第一天
     *
     * @return TimeHelper
     */
    val firstDayOfQuarter: TimeUtil by lazy {
        of(
                localDateTime.withMonth(localDateTime.month.firstMonthOfQuarter().value)
                        .with(TemporalAdjusters.firstDayOfMonth())
        )
    }

    /**
     * 取得下季度第一天
     *
     * @return TimeHelper
     */
    val firstDayOfNextQuarter: TimeUtil by lazy {
        of(
                localDateTime.withMonth(localDateTime.month.firstMonthOfQuarter().plus(3).value)
                        .with(TemporalAdjusters.firstDayOfMonth())
        )
    }

    /**
     * 取得当季度最后一天
     *
     * @return TimeHelper
     */
    val lastDayOfQuarter: TimeUtil by lazy {
        of(
                localDateTime
                        .withMonth(localDateTime.month.firstMonthOfQuarter().plus(2).value)
                        .with(TemporalAdjusters.lastDayOfMonth())
        )
    }

    /**
     * 获取当年的第一天
     *
     * @return TimeHelper
     */
    val firstDayOfYear: TimeUtil by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfYear()))
    }

    /**
     * 获取下年的第一天
     *
     * @return TimeHelper
     */
    val firstDayOfNextYear: TimeUtil by lazy {
        of(localDateTime.with(TemporalAdjusters.firstDayOfNextYear()))
    }

    /**
     * 获取当年的最后一天
     *
     * @return TimeHelper
     */
    val lastDayOfYear: TimeUtil by lazy {
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

    fun toLocalTime(): LocalTime {
        return localDateTime.toLocalTime()
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

    fun format(dateFormatPattern: String): String {
        return format(DateTimeFormatter.ofPattern(dateFormatPattern))
    }

    companion object {

        val DEFAULT_ZONE_ID: ZoneId = ZoneId.systemDefault()
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
        @JvmOverloads
        fun now(zoneId: ZoneId = DEFAULT_ZONE_ID): TimeUtil {
            return TimeUtil(LocalDateTime.now(zoneId))
        }

        @JvmStatic
        fun parse(text: CharSequence): TimeUtil {
            return TimeUtil(LocalDateTime.parse(text))
        }

        @JvmStatic
        fun parse(text: CharSequence, formatter: DateTimeFormatter): TimeUtil {
            return TimeUtil(LocalDateTime.parse(text, formatter))
        }

        @JvmStatic
        fun parse(text: CharSequence, formatter: String): TimeUtil {
            return parse(
                    text, DateTimeFormatterBuilder()
                    .appendPattern(formatter)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .toFormatter()
            )
        }

        @JvmStatic
        fun parseDate(text: CharSequence): TimeUtil {
            return of(LocalDate.parse(text))
        }

        @JvmStatic
        fun parseDate(text: CharSequence, formatter: DateTimeFormatter): TimeUtil {
            return of(LocalDate.parse(text, formatter))
        }

        @JvmStatic
        fun parseDate(text: CharSequence, formatter: String): TimeUtil {
            return of(
                    LocalDate.parse(
                            text, DateTimeFormatterBuilder()
                            .appendPattern(formatter)
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .toFormatter()
                    )
            )
        }

        @JvmStatic
        fun of(year: Int, month: Int, dayOfMonth: Int): TimeUtil {
            return TimeUtil(LocalDate.of(year, month, dayOfMonth).atStartOfDay())
        }

        @JvmStatic
        @JvmOverloads
        fun of(instant: Instant, zoneId: ZoneId = DEFAULT_ZONE_ID): TimeUtil {
            return of(LocalDateTime.ofInstant(instant, zoneId))
        }

        @JvmStatic
        fun of(localDateTime: LocalDateTime): TimeUtil {
            return TimeUtil(localDateTime)
        }

        @JvmStatic
        fun of(localDate: LocalDate): TimeUtil {
            return TimeUtil(localDate.atStartOfDay())
        }

        @JvmStatic
        fun of(date: Date): TimeUtil {
            return of(date.toInstant())
        }

        @JvmStatic
        fun of(millis: Long): TimeUtil {
            return of(Instant.ofEpochMilli(millis))
        }

        @JvmStatic
        fun of(calendar: Calendar): TimeUtil {
            return TimeUtil(
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

        @JvmOverloads
        @JvmStatic
        fun toLocalDateTime(millis: Long, zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDateTime {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId)
        }

        @JvmOverloads
        @JvmStatic
        fun toLocalDateTime(date: Date, zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDateTime {
            return LocalDateTime.ofInstant(date.toInstant(), zoneId)
        }

        @JvmOverloads
        @JvmStatic
        fun toLocalDate(millis: Long, zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDate {
            return toLocalDateTime(millis, zoneId).toLocalDate()
        }

        @JvmOverloads
        @JvmStatic
        fun toLocalDate(date: Date, zoneId: ZoneId = DEFAULT_ZONE_ID): LocalDate {
            return toLocalDateTime(date, zoneId).toLocalDate()
        }

        @JvmStatic
        fun between(startDateInclusive: Date, endDateExclusive: Date): Period {
            return Period.between(toLocalDate(startDateInclusive), toLocalDate(endDateExclusive))
        }
    }

}
