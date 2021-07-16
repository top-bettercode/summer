package top.bettercode.config.top.bettercode.config

import org.junit.jupiter.api.Test
import top.bettercode.config.LogFileNameComparator
import java.io.File

/**
 *
 * @author Peter Wu
 */
internal class LogFileNameComparatorTest {

    @Test
    internal fun comparator() {
        val listFiles =
            File("/data/repositories/bettercode/wintruelife/template/app/build/logs").listFiles()
        listFiles?.sortWith(LogFileNameComparator())
        listFiles?.forEach {
            println(it.name)
        }
    }
}