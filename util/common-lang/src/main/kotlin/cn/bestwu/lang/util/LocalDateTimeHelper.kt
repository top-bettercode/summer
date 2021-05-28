package cn.bestwu.lang.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

/**
 * @author Peter Wu
 */
class LocalDateTimeHelper private constructor(private val localDateTime: LocalDateTime, private var zoneOffset: ZoneOffset?) {

    /**
     * 取得当月第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfMonth: LocalDateTimeHelper
        get() = of(localDateTime.with(TemporalAdjusters.firstDayOfMonth()))
                .zoneOffset(zoneOffset)

    /**
     * 取得下月第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfNextMonth: LocalDateTimeHelper
        get() = of(localDateTime.with(TemporalAdjusters.firstDayOfNextMonth()))
                .zoneOffset(zoneOffset)

    /**
     * 取得当月最后一天
     *
     * @return LocalDateHelper
     */
    val lastDayOfMonth: LocalDateTimeHelper
        get() = of(localDateTime.with(TemporalAdjusters.lastDayOfMonth()))
                .zoneOffset(zoneOffset)

    /**
     * 取得当季度第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfQuarter: LocalDateTimeHelper
        get() = of(localDateTime.withMonth(localDateTime.month.firstMonthOfQuarter().value)
                        .with(TemporalAdjusters.firstDayOfMonth())).zoneOffset(zoneOffset)

    /**
     * 取得下季度第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfNextQuarter: LocalDateTimeHelper
        get() = of(localDateTime
                        .withMonth(localDateTime.month.firstMonthOfQuarter().plus(3).value)
                        .with(TemporalAdjusters.firstDayOfMonth())).zoneOffset(zoneOffset)

    /**
     * 取得当季度最后一天
     *
     * @return LocalDateHelper
     */
    val lastDayOfQuarter: LocalDateTimeHelper
        get() = of(localDateTime
                        .withMonth(localDateTime.month.firstMonthOfQuarter().plus(2).value)
                        .with(TemporalAdjusters.lastDayOfMonth())).zoneOffset(zoneOffset)

    /**
     * 获取当年的第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfYear: LocalDateTimeHelper
        get() = of(localDateTime.with(TemporalAdjusters.firstDayOfYear()))
                .zoneOffset(zoneOffset)

    /**
     * 获取下年的第一天
     *
     * @return LocalDateHelper
     */
    val firstDayOfNextYear: LocalDateTimeHelper
        get() = of(localDateTime.with(TemporalAdjusters.firstDayOfNextYear()))
                .zoneOffset(zoneOffset)

    /**
     * 获取当年的最后一天
     *
     * @return LocalDateHelper
     */
    val lastDayOfYear: LocalDateTimeHelper
        get() = of(localDateTime.with(TemporalAdjusters.lastDayOfYear()))
                .zoneOffset(zoneOffset)

    //--------------------------------------------
    fun toMillis(): Long {
        return toInstant().toEpochMilli()
    }

    fun toDate(): Date {
        return Date.from(toInstant())
    }

    fun toInstant(): Instant {
        return toLocalDateTime().toInstant(zoneOffset)
    }

    fun toLocalDateTime(): LocalDateTime {
        return localDateTime
    }

    fun toLocalDate(): LocalDate {
        return localDateTime.toLocalDate()
    }

    fun format(): String {
        return toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME)
    }

    fun format(dateTimeFormatter: DateTimeFormatter): String {
        return localDateTime.format(dateTimeFormatter)
    }

    private fun zoneOffset(zoneOffset: ZoneOffset?): LocalDateTimeHelper {
        this.zoneOffset = zoneOffset
        return this
    }

    companion object {

        private val DEFAULT_ZONE_OFFSET = ZoneOffset.of("+8")!!

        @JvmStatic
        fun now(): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDateTime.now(), DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic
        fun parse(text: CharSequence): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDateTime.parse(text), DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic

        fun parse(text: CharSequence, formatter: DateTimeFormatter): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDateTime.parse(text, formatter), DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic
        fun of(year: Int, month: Int, dayOfMonth: Int): LocalDateTimeHelper {
            return LocalDateTimeHelper(LocalDate.of(year, month, dayOfMonth).atStartOfDay(),
                    DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic
        fun of(localDateTime: LocalDateTime): LocalDateTimeHelper {
            return LocalDateTimeHelper(localDateTime, DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic
        fun of(localDate: LocalDate): LocalDateTimeHelper {
            return LocalDateTimeHelper(localDate.atStartOfDay(), DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic
        fun of(date: Date): LocalDateTimeHelper {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return of(calendar)
        }

        @JvmStatic
        fun of(millis: Long): LocalDateTimeHelper {
            return of(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime())
        }

        @JvmStatic
        fun of(calendar: Calendar): LocalDateTimeHelper {
            return LocalDateTimeHelper(
                    LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND),
                            calendar.get(Calendar.MILLISECOND) * 1000000),
                    DEFAULT_ZONE_OFFSET)
        }

        @JvmStatic
        fun toDate(localDate: LocalDate): Date {
            return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_OFFSET).toInstant())
        }

        @JvmStatic
        fun toDate(localDateTime: LocalDateTime): Date {
            return Date.from(localDateTime.toInstant(DEFAULT_ZONE_OFFSET))
        }

    }

}
