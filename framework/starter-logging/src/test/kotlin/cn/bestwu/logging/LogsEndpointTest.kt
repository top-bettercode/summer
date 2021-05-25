package cn.bestwu.logging

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 *
 * @author Peter Wu
 */
internal class LogsEndpointTest {

    @Test
    internal fun comparator() {
        val listFiles =
            File("/data/repositories/bestwu/wintruelife/template/app/build/logs").listFiles()
        listFiles?.sortWith(LogFileNameComparator())
        listFiles?.forEach {
            println(it.name)
        }
    }
}