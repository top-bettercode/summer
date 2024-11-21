package top.bettercode.summer.logging

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import top.bettercode.summer.config.LogMsg
import top.bettercode.summer.tools.lang.util.StringUtil
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream


class LogConvertTest {

    private val logs = ConcurrentLinkedQueue<String>()

    suspend fun readFile(file: File) {
        if (file.isFile) {
            if (file.extension == "zip") {
                val zipEntryMap = mutableMapOf<String, ByteArray>()
                ZipInputStream(file.inputStream()).use { zis ->
                    var nextEntry = zis.nextEntry
                    while (nextEntry != null) {
                        if (!nextEntry.isDirectory) {
                            zipEntryMap[nextEntry.name] = zis.readBytes()
                        }
                        nextEntry = zis.nextEntry
                    }
                }
                coroutineScope {
                    for ((name, byteArray) in zipEntryMap) {
                        launch {
                            val entryInputStream = ByteArrayInputStream(byteArray)
                            val lines = if (name.endsWith(".gz", true)) {
                                withContext(Dispatchers.IO) {
                                    GZIPInputStream(entryInputStream).bufferedReader()
                                }
                                    .readLines()
                            } else {
                                entryInputStream.bufferedReader().readLines()
                            }
                            readLines(name, lines)
                        }
                    }
                }
            } else {
                val lines = if (file.extension.equals("gz", true)) {
                    withContext(Dispatchers.IO) {
                        GZIPInputStream(file.inputStream()).bufferedReader()
                    }.readLines()
                } else {
                    file.readLines()
                }
                readLines(file.name, lines)
            }
        } else {
            coroutineScope {
                file.listFiles()?.forEach { f ->
                    launch {
                        readFile(f)
                    }
                }
            }
        }
    }

    fun readLines(name: String, lines: List<String>) {
        print("处理${name}")
        //2023-02-11 12:56:11.967  INFO 30246 --- [pool-3-thread-1] com.cdwintech.npk.task.TaskService       :
//        val regrex =
//            Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}) +([A-Z]+) +(\\d+) +--- +\\[([a-z0-9\\-]+)] +(\\S+) +:(.*)")
        //2024-09-12 11:55:06.505  INFO [exec-7] t.b.s.s.r.RedisStoreTokenRepository      RedisStoreTokenRepository.kt:138 4385dd54: msg
        val regrex =
            Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}) +([A-Z]+) +(\\S+) +(\\S+) +(\\S+) +(\\S*) *: (.*)")

        val msgs = mutableListOf<LogMsg>()
        var msg = StringBuilder("")
        var level = "DEFAULT"
        val traceid = null
        var traceIdMatch = traceid.isNullOrBlank()

        lines.forEach { line ->
            val matchResult = regrex.matchEntire(line)
            if (matchResult != null) {
                val groupValues = matchResult.groupValues
                if (msg.isNotBlank() && traceIdMatch) {
                    process(LogMsg(level, msg.toString()))
                }
                if (!traceIdMatch)
                    traceIdMatch = groupValues[6] == traceid
                msg = java.lang.StringBuilder(line)
                level = groupValues[2]
            } else {
                msg.append(StringUtil.LINE_SEPARATOR)
                msg.append(line)
            }
        }
        if (msg.isNotBlank() && traceIdMatch) {
            process(LogMsg(level, msg.toString()))
        }
    }

    private fun process(log: LogMsg) {
        val msg = log.msg
        if ((msg.contains("POST /npk/appMessages"))) {
            logs.add(msg)
        }
    }

    @Test
    fun test() {
        val source = File("/local/downloads/all-2023-04-15-13.gz")
        val target = File("/local/downloads/result.txt")
        runBlocking {
            readFile(source)
        }
        System.err.println("---------------------------------------")
        System.err.println(logs.joinToString(System.lineSeparator()))
        System.err.println("---------------------------------------")
//        target.writeText(logs.joinToString(System.lineSeparator()))
    }
}
