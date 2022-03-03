package top.bettercode.config.top.bettercode.config

import org.junit.jupiter.api.Test
import top.bettercode.config.LogFileNameComparator
import java.io.File

/**
 *
 * @author Peter Wu
 */
class LogFileNameComparatorTest {

    @Test
    fun comparator() {
        val listFiles =
            arrayOf(
                "/alarm/2022-03-02-10:20:54.673-error-24760.log#last",
                "/alarm/2022-03-02-10:21:54.673-error-24760.log#last",
                "/alarm/2021-12-31-15:31:51.539-ERROR-21572.log#last",
                "all.log-2022-03-02-2.gz",
                "all.log-2022-03-02-3.gz",
                "all.log-2022-03-02-4.gz",
                "all-2022-03-02-4.gz",
                "all.log",
                "root.log-2022-03-02-4.gz",
                "root.log",
                "sap-2021-07-09-0.gz",
                "sap.log"
            ).map { File(it) }
        listFiles.sortedWith(LogFileNameComparator())
        .forEach {
            println(it.name)
        }
    }

    @Test
    fun compare() {
        System.err.println("1".toInt().compareTo(2))
        val compare = LogFileNameComparator().compare(
            File("/alarm/2022-03-02-10:20:54.673-error-24760.log#last"),
            File("/alarm/2022-03-02-10:21:54.673-error-24760.log#last")
        )
        System.err.println(compare)
    }
}