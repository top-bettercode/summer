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
                File("/alarm/2022-03-02-10:20:54.673-error-24760.log#last"),
                File("/alarm/2022-03-02-10:21:54.673-error-24760.log#last"),
                File("/alarm/2021-12-31-15:31:51.539-ERROR-21572.log#last"),
                File("all.log-2022-03-02-2.gz"),
                File("all.log-2022-03-02-3.gz"),
                File("all.log-2022-03-02-4.gz"),
                File("all.log"),
                File("root.log-2022-03-02-4.gz"),
                File("root.log")
            )
        listFiles.sortWith(LogFileNameComparator())
        listFiles.forEach {
            println(it.name)
        }
    }

    @Test
    fun compare() {
        val compare = LogFileNameComparator().compare(
            File("all.log-2022-03-02-4.gz"),
            File("all.log")
        )
        System.err.println(compare)
    }
}