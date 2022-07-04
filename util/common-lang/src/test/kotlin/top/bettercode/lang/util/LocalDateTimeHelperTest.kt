package top.bettercode.lang.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class LocalDateTimeHelperTest {

    private val localDate = LocalDateTimeHelper.of(2018, 3, 5)

    @Test
    fun getFirstDayOfMonth() {
        assertEquals("2018-03-01 00:00:00.000", localDate.firstDayOfMonth.format())
    }

    @Test
    fun getFirstDayOfNextMonth() {
        assertEquals("2018-04-01 00:00:00.000", localDate.firstDayOfNextMonth.format())
    }

    @Test
    fun getLastDayOfMonth() {
        assertEquals("2018-03-31 00:00:00.000", localDate.lastDayOfMonth.format())
    }

    @Test
    fun getFirstDayOfQuarter() {
        assertEquals("2018-01-01 00:00:00.000", localDate.firstDayOfQuarter.format())
    }

    @Test
    fun getFirstDayOfNextQuarter() {
        assertEquals("2018-04-01 00:00:00.000", localDate.firstDayOfNextQuarter.format())
    }

    @Test
    fun getLastDayOfQuarter() {
        assertEquals("2018-03-31 00:00:00.000", localDate.lastDayOfQuarter.format())
    }

    @Test
    fun getFirstDayOfYear() {
        assertEquals("2018-01-01 00:00:00.000", localDate.firstDayOfYear.format())
    }

    @Test
    fun getFirstDayOfNextYear() {
        assertEquals("2019-01-01 00:00:00.000", localDate.firstDayOfNextYear.format())
    }

    @Test
    fun getLastDayOfYear() {
        assertEquals("2018-12-31 00:00:00.000", localDate.lastDayOfYear.format())
    }
}