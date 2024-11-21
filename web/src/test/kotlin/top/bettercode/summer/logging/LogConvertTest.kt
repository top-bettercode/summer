package top.bettercode.summer.logging

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
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
        val regrex =
            Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}) +([A-Z]+) +(\\d+) +--- +\\[([a-z0-9\\-]+)] +(\\S+) +:(.*)")

        lateinit var time: String
        lateinit var logLevel: String
        lateinit var pid: String
        lateinit var threadName: String
        lateinit var logName: String
        var msg = StringBuilder("")
        lines.forEach { line ->
            val matchResult = regrex.matchEntire(line)
            if (matchResult != null) {
                val groupValues = matchResult.groupValues
                time = groupValues[1]
                logLevel = groupValues[2]
                pid = groupValues[3]
                threadName = groupValues[4]
                logName = groupValues[5]

                if (msg.isNotBlank()) {
                    process(time, logLevel, pid, threadName, logName, msg.toString())
                }
                msg = java.lang.StringBuilder(groupValues[6])
            } else {
                msg.append(System.lineSeparator())
                msg.append(line)
            }
        }
        if (msg.isNotBlank()) {
            process(time, logLevel, pid, threadName, logName, msg.toString())
        }
    }


    fun process(
        time: String,
        logLevel: String,
        pid: String,
        threadName: String,
        logName: String,
        msg: String
    ) {
        if ((msg.contains("POST /npk/users/resetPassword") && msg.contains("mobile=13948644728")) || (msg.contains(
                "POST /npk/users/password"
            ) && msg.contains("app:CUST00173E"))
        ) {
            logs.add(msg)
        }
    }

    @Test
    fun test() {
        val source = File("/local/downloads/202304")
        val target = File("/local/downloads/result.txt")
        runBlocking {
            readFile(source)
        }
        target.writeText(logs.joinToString(System.lineSeparator()))
    }
}
