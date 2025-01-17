package top.bettercode.summer.tools.lang.util

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLConnection
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.math.absoluteValue


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
    fun format(dateTimeFormatter: DateTimeFormatter = DEFAULT_DATE_TIME_FORMATTER): String {
        return localDateTime.format(dateTimeFormatter)
    }

    fun format(dateFormatPattern: String): String {
        return format(DateTimeFormatter.ofPattern(dateFormatPattern))
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TimeUtil::class.java)
        val DEFAULT_ZONE_ID: ZoneId = ZoneId.systemDefault()
        const val DEFAULT_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss"
        const val DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
        const val ISO8601 = "yyyy-MM-dd'T'HH:mm:ssXXX"
        const val ISO8601_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        val DEFAULT_DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT_PATTERN)
        val DEFAULT_DATE_TIME_SSS_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN)

        @JvmOverloads
        @JvmStatic
        fun format(
            localDateTime: LocalDateTime,
            pattern: String = DEFAULT_DATE_TIME_FORMAT_PATTERN
        ): String {
            return localDateTime.format(DateTimeFormatter.ofPattern(pattern))
        }

        @JvmOverloads
        @JvmStatic
        fun format(timeStamp: Long, pattern: String = DEFAULT_DATE_TIME_FORMAT_PATTERN): String {
            return of(timeStamp).format(DateTimeFormatter.ofPattern(pattern))
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
        fun parse(text: CharSequence, pattern: String): TimeUtil {
            return parse(
                text, DateTimeFormatterBuilder()
                    .appendPattern(pattern)
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

        /**
         * @param server NTP时间服务器地址
         * @param connectTimeout 连接超时时间，单位：秒
         * @param readTimeout 读取超时时间，单位：秒
         */
        @JvmStatic
        @JvmOverloads
        fun getNtpTime(
            server: String = "http://cn.pool.ntp.org",
            connectTimeout: Int = 5,
            readTimeout: Int = 5
        ): Long {
            val connection: URLConnection = URL(server).openConnection()
            connection.connectTimeout = connectTimeout * 1000
            connection.readTimeout = readTimeout * 1000
            return connection.getHeaderFieldDate("Date", 0)
        }

        private fun reGetNtpTime(server: String, times: Int = 0): Long {
            val t = times + 1
            val ntpTime = getNtpTime(server)
            return if (ntpTime > 0 || t > 5)
                ntpTime
            else
                reGetNtpTime(server, t)
        }

        private val customScope = CoroutineScope(Dispatchers.Default)

        private fun getNetworkTime(): Pair<String, Long> {
            return runBlocking {
                val ntpServers = listOf(
                    "http://pool.ntp.org",
                    "http://time.windows.com",
                    "http://time.apple.com",
                    "http://time.google.com",
                )
                // 使用单一的 Deferred 对象，用于保存第一个返回的结果
                val firstResult = CompletableDeferred<Pair<String, Long>>()
                val jobs = mutableListOf<Job>()
                for (server in ntpServers) {
                    val job = customScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            return@withContext reGetNtpTime(server)
                        }
                        // 如果第一个协程返回结果，则将结果保存到 Deferred 对象中
                        if (firstResult.isActive) {
                            firstResult.complete(server to result)
                        }
                    }
                    jobs.add(job)
                }
                val result = firstResult.await()
                jobs.forEach { it.cancel() } // 取消所有其他任务
                result
            }
        }

        /**
         * 检查网络时间是否同步
         * @param acceptedDiffSeconds 可接受的时间差，单位：秒
         */
        @JvmOverloads
        @JvmStatic
        fun checkTime(acceptedDiffSeconds: Int = 5): Boolean {
            var now = Instant.now()
            val (server, time) = getNetworkTime()
            if (time <= 0) {
                return false
            }
            now = now.plusMillis((System.currentTimeMillis() - now.toEpochMilli()) / 2)
            val networkTime = Instant.ofEpochMilli(time)
            val seconds = Duration.between(networkTime, now).seconds
            log.info(
                "$server 网络时间:{},本地时间:{},相差{}s",
                of(networkTime).format(DEFAULT_DATE_TIME_SSS_FORMATTER),
                of(now).format(DEFAULT_DATE_TIME_SSS_FORMATTER),
                seconds
            )
            val diffSeconds = seconds.absoluteValue
            val synchronous = diffSeconds < acceptedDiffSeconds
            if (!synchronous) {
                log.error(
                    "$server 网络时间和本地时间不一致{}s,网络时间:{},本地时间:{}",
                    seconds,
                    of(networkTime).format(DEFAULT_DATE_TIME_SSS_FORMATTER),
                    of(now).format(DEFAULT_DATE_TIME_SSS_FORMATTER)
                )
            }
            return synchronous
        }
    }

}
