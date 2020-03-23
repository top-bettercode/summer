package cn.bestwu.logging

import cn.bestwu.lang.util.LocalDateTimeHelper
import cn.bestwu.logging.annotation.NoRequestLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 日志
 */
@ConditionalOnWebApplication
@Controller
@RequestMapping(value = ["/logs"], name = "日志")
class LogsController(@Value("\${logging.files.path}")
                     private val loggingFilesPath: String) {

    @NoRequestLogging
    @GetMapping(name = "日志")
    @Throws(IOException::class)
    fun root(request: HttpServletRequest, response: HttpServletResponse) {
        index(File(loggingFilesPath), request, response, true)
    }

    @NoRequestLogging
    @GetMapping(value = ["/{path}"], name = "日志")
    @Throws(IOException::class)
    fun path(@PathVariable path: String, request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = "text/plain;charset=UTF-8"
        val file = File(loggingFilesPath, path)
        if (file.isFile) {
            showLogFile(response, file)
        } else {
            index(file, request, response, false)
        }
    }

    @NoRequestLogging
    @GetMapping(value = ["/{path}/{file}"], name = "日志")
    @Throws(IOException::class)
    fun log(@PathVariable path: String, @PathVariable file: String,
            response: HttpServletResponse) {
        val logFile = File(loggingFilesPath, "$path/$file")
        showLogFile(response, logFile)
    }

    private fun showLogFile(response: HttpServletResponse, logFile: File) {
        if (logFile.exists()) {
            if (logFile.isFile) {
                if (logFile.extension == "log") {
                    response.contentType = "text/plain;charset=UTF-8"
                } else {
                    response.setHeader("Content-Disposition",
                            "attachment;filename=${logFile.name};filename*=UTF-8''" + URLEncoder
                                    .encode(logFile.name, "UTF-8"))
                    response.contentType = "application/octet-stream"
                }
                response.setHeader("Pragma", "No-cache")
                response.setHeader("Cache-Control", "no-cache")
                response.setDateHeader("Expires", 0)
                StreamUtils.copy(FileInputStream(logFile), response.outputStream)
            } else {
                response.sendError(HttpStatus.CONFLICT.value(), "Path is directory")
            }
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }

    private fun index(file: File, request: HttpServletRequest, response: HttpServletResponse, root: Boolean) {
        if (file.exists()) {
            val servletPath = request.servletPath
            val endsWith = servletPath.endsWith("/")
            val upPath = if (endsWith) "../" else "./"
            val path = if (endsWith) "." else "./${servletPath.substringAfterLast("/")}"
            response.contentType = "text/html; charset=utf-8"
            response.setHeader("Pragma", "No-cache")
            response.setHeader("Cache-Control", "no-cache")
            response.setDateHeader("Expires", 0)
            response.writer.use { writer ->
                writer.println("""
<html>
<head><title>Index of /</title></head>
<body>""")
                writer.print("<h1>Index of /</h1><hr><pre>")
                if (!root)
                    writer.println("<a href=\"$upPath\">../</a>")
                file.listFiles()?.forEach { it ->
                    if (it.isDirectory) {
                        writer.println("<a style=\"display:inline-block;width:100px;\" href=\"$path/${it.name}/\">${it.name}/</a>                                        ${LocalDateTimeHelper.of(it.lastModified()).format()}       -")
                    } else {
                        writer.println("<a style=\"display:inline-block;width:100px;\" href=\"$path/${it.name}\">${it.name}</a>                                        ${LocalDateTimeHelper.of(it.lastModified()).format()}       ${prettyValue(it.length())}")
                    }
                }
                writer.println("</pre><hr></body>\n</html>")
            }
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }

    private val units = arrayOf("B", "K", "M", "G")

    /**
     * 返回易读的值
     *
     * @param value 值，单位B
     * @return 易读的值
     */
    fun prettyValue(value: Long): String {
        var prettyValue = value
        var index = 0
        while (prettyValue / 1024 > 0) {
            prettyValue /= 1024
            index++
        }
        return prettyValue.toString() + units[index]
    }
}
