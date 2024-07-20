package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_SSS_FORMATTER

/**
 * @author Peter Wu
 */
class TimeUtilTest {

    private val localDate = TimeUtil.of(2018, 3, 5)

    @Test
    fun getFirstDayOfMonth() {
        assertEquals("2018-03-01 00:00:00.000", localDate.firstDayOfMonth.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getFirstDayOfNextMonth() {
        assertEquals("2018-04-01 00:00:00.000", localDate.firstDayOfNextMonth.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getLastDayOfMonth() {
        assertEquals("2018-03-31 00:00:00.000", localDate.lastDayOfMonth.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getFirstDayOfQuarter() {
        assertEquals("2018-01-01 00:00:00.000", localDate.firstDayOfQuarter.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getFirstDayOfNextQuarter() {
        assertEquals("2018-04-01 00:00:00.000", localDate.firstDayOfNextQuarter.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getLastDayOfQuarter() {
        assertEquals("2018-03-31 00:00:00.000", localDate.lastDayOfQuarter.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getFirstDayOfYear() {
        assertEquals("2018-01-01 00:00:00.000", localDate.firstDayOfYear.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getFirstDayOfNextYear() {
        assertEquals("2019-01-01 00:00:00.000", localDate.firstDayOfNextYear.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun getLastDayOfYear() {
        assertEquals("2018-12-31 00:00:00.000", localDate.lastDayOfYear.format(DEFAULT_DATE_TIME_SSS_FORMATTER))
    }

    @Test
    fun checkTime() {
        TimeUtil.checkTime()
    }
}