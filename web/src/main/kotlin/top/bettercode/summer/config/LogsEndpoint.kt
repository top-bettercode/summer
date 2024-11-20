package top.bettercode.summer.config

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.lang.Nullable
import org.springframework.util.ClassUtils
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.servlet.HandlerMapping
import top.bettercode.summer.logging.LoggingUtil
import top.bettercode.summer.logging.WebsocketProperties
import top.bettercode.summer.tools.lang.PrettyMessageHTMLLayout
import top.bettercode.summer.tools.lang.log.AlarmMarker
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.loggerContext
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.File
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.math.max

/**
 * 日志
 */
@Endpoint(id = "logs")
class LogsEndpoint(
    private val loggingFilesPath: String,
    env: Environment,
    private val websocketProperties: WebsocketProperties?,
    private val request: HttpServletRequest,
    private val response: HttpServletResponse,
    webEndpointProperties: WebEndpointProperties,
    managementServerProperties: ManagementServerProperties
) {

    private val log = LoggerFactory.getLogger(LogsEndpoint::class.java)

    private val contextPath: String = managementServerProperties.basePath ?: "/"
    private val basePath: String = contextPath + webEndpointProperties.basePath + "/logs"
    private val appName: String = LoggingUtil.warnTitle(env)

    private val useWebSocket: Boolean = ClassUtils.isPresent(
        "org.springframework.web.socket.server.standard.ServerEndpointExporter",
        LogsEndpoint::class.java.classLoader
    ) && ("true" == env.getProperty("summer.logging.websocket.enabled") || env.getProperty(
        "summer.logging.websocket.enabled"
    ).isNullOrBlank())

    @ReadOperation
    fun root() {
        index(File(loggingFilesPath).listFiles(), true, "", true)
    }

    @ReadOperation
    fun path(
        @Selector(match = Selector.Match.ALL_REMAINING) path: String,
        @Nullable collapse: Boolean?,
        @Nullable download: Boolean?,
        @Nullable traceid: String?,
        @Nullable @RequestHeader(value = "User-Agent", required = false) userAgent: String?
    ) {
        val requestPath = path.replace(",", "/")

        if ("real-time" != path) {
            val paths = path.split(",")
            if (paths.contains("daily")) {
                val today = TimeUtil.now().format("yyyy-MM-dd")
                val dir = File(loggingFilesPath, requestPath.substringBeforeLast("daily"))
                val index = paths.indexOf("daily")
                val dailyPath = paths.drop(index)
                if (dailyPath.size == 1) {
                    val filenames =
                        dir.listFiles()?.filter { file -> file.name.startsWith("all-") }
                            ?.map {
                                it.nameWithoutExtension.replace(
                                    Regex("all-(\\d{4}-\\d{2}-\\d{2})-\\d+"),
                                    "$1"
                                )
                            }?.toMutableSet()
                            ?: mutableSetOf()
                    if (!filenames.contains(today)) {
                        filenames.add(today)
                    }
                    index(filenames.map { File(it) }.toTypedArray(), false, requestPath, false)
                    return
                } else if (dailyPath.size == 2) {
                    var logPattern = dailyPath[1]
                    val html =
                        if (logPattern.endsWith(".html")) {
                            logPattern = logPattern.substringBeforeLast(".html")
                            true
                        } else false

                    val matchCurrent = today.startsWith(logPattern)
                    val files =
                        dir.listFiles()
                            ?.filter { file -> file.name.startsWith("all-$logPattern") || matchCurrent && file.name == "all.log" }
                            ?.toList()

                    if (!files.isNullOrEmpty()) {
                        files.sortedWith(compareBy { it.lastModified() })

                        if (html) {
                            val logMsgs = mutableListOf<LogMsg>()
                            files.forEach { file ->
                                logMsgs.addAll(
                                    readLogMsgs(
                                        file.inputStream(),
                                        "gz" == file.extension,
                                        traceid
                                    )
                                )
                            }
                            showLogFile(logPattern, logMsgs, collapse)
                            return
                        } else {
                            logGz(logPattern, userAgent, files)
                            return
                        }
                    } else {
                        response.sendError(HttpStatus.NOT_FOUND.value(), "no log file match")
                        return
                    }
                }
            } else {
                val file = File(loggingFilesPath, requestPath)
                if (!file.exists() && file.name.startsWith("all-")) {
                    val servletPath =
                        (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String?
                            ?: throw IllegalStateException(
                                "Required request attribute '" +
                                        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set"
                            ))
                    response.sendRedirect(servletPath.replace(file.name, "all.log"))
                    return
                }
                if (file.isFile) {
                    if (!file.exists()) {
                        response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
                        return
                    } else {
                        val extension = file.extension
                        if ("json" == extension || download == true) {
                            file(file, userAgent)
                            return
                        } else {
                            val logMsgs = readLogMsgs(
                                file.inputStream(),
                                "gz" == extension,
                                traceid
                            )
                            showLogFile(file.name, logMsgs, collapse)
                            return
                        }
                    }
                } else {
                    index(file.listFiles(), false, requestPath, true)
                    return
                }
            }
        } else {
            if (useWebSocket) {
                webSocket()
                return
            } else {
                response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
                return
            }
        }
    }

    private fun logGz(
        logPattern: String,
        userAgent: String?,
        files: List<File>
    ) {
        val fileName = "$logPattern.log.gz"
        val newFileName: String =
            if (null != userAgent && (userAgent.contains("Trident") || userAgent.contains(
                    "Edge"
                ))
            ) {
                URLEncoder.encode(fileName, "UTF-8")
            } else {
                fileName
            }
        response.setHeader(
            "Content-Disposition",
            "attachment;filename=$newFileName;filename*=UTF-8''" + URLEncoder.encode(
                fileName,
                "UTF-8"
            )
        )
        response.contentType = "application/octet-stream; charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setDateHeader("Expires", 0)

        GZIPOutputStream(response.outputStream).buffered().use { bos ->
            files.forEach { file ->
                bos.write(
                    if ("gz".equals(
                            file.extension,
                            true
                        )
                    ) GZIPInputStream(file.inputStream()).readBytes() else file.inputStream()
                        .readBytes()
                )
            }
        }
    }

    private fun file(file: File, userAgent: String?) {
        val fileName = file.name
        val newFileName: String =
            if (null != userAgent && (userAgent.contains("Trident") || userAgent.contains(
                    "Edge"
                ))
            ) {
                URLEncoder.encode(fileName, "UTF-8")
            } else {
                fileName
            }
        response.setHeader(
            "Content-Disposition",
            "attachment;filename=$newFileName;filename*=UTF-8''" + URLEncoder.encode(
                fileName,
                "UTF-8"
            )
        )
        response.contentType = "application/octet-stream; charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setDateHeader("Expires", 0)
        response.outputStream.bufferedWriter().use { writer ->
            writer.write(file.readText())
        }
    }

    private fun webSocket() {
        response.contentType = "text/html;charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setDateHeader("Expires", 0)
        val prettyMessageHTMLLayout = PrettyMessageHTMLLayout()
        prettyMessageHTMLLayout.context = loggerContext
        prettyMessageHTMLLayout.start()
        prettyMessageHTMLLayout.title = "实时日志"
        response.writer.use { writer ->
            writer.println(prettyMessageHTMLLayout.fileHeader)
            writer.println(prettyMessageHTMLLayout.getLogsHeader())
            writer.println(prettyMessageHTMLLayout.presentationFooter)
            writer.println(
                """
    <script type="text/javascript">
      
    
      function getScrollTop() {
        var scrollTop = 0, bodyScrollTop = 0, documentScrollTop = 0;
        if (document.body) {
          bodyScrollTop = document.body.scrollTop;
        }
        if (document.documentElement) {
          documentScrollTop = document.documentElement.scrollTop;
        }
        scrollTop = (bodyScrollTop - documentScrollTop > 0) ? bodyScrollTop : documentScrollTop;
        return scrollTop;
      }
    
      //文档的总高度
    
      function getScrollHeight() {
        var scrollHeight = 0, bodyScrollHeight = 0, documentScrollHeight = 0;
        if (document.body) {
          bodyScrollHeight = document.body.scrollHeight;
        }
        if (document.documentElement) {
          documentScrollHeight = document.documentElement.scrollHeight;
        }
        scrollHeight = (bodyScrollHeight - documentScrollHeight > 0) ? bodyScrollHeight
            : documentScrollHeight;
        return scrollHeight;
      }
    
      //浏览器视口的高度
    
      function getWindowHeight() {
        var windowHeight = 0;
        if (document.compatMode === "CSS1Compat") {
          windowHeight = document.documentElement.clientHeight;
        } else {
          windowHeight = document.body.clientHeight;
        }
        return windowHeight;
      }
      
      document.onEnd = true
      window.onscroll = function () {
        document.onEnd = getScrollTop() + getWindowHeight() === getScrollHeight();
      };
      
      //websocket对象
      let websocket = null;
      //判断当前浏览器是否支持WebSocket
      if (typeof (WebSocket) == "undefined") {
        console.log("您的浏览器不支持WebSocket");
      } else {
        console.info("连接...")
        
        websocket = new WebSocket("${LoggingUtil.apiAddressWs.first}${"/websocket/logging"}?token=${websocketProperties?.token}");
        
        //连接发生错误的回调方法
        websocket.onerror = function () {
          console.error("WebSocket连接发生错误");
        };
    
        //连接成功建立的回调方法
        websocket.onopen = function () {
          console.log("WebSocket连接成功")
        };
    
        //接收到消息的回调方法
        websocket.onmessage = function (event) {
          if (event.data) {
            let node = document.querySelector('#loggingText');
            node.insertAdjacentHTML("beforeEnd", event.data);
            if (document.onEnd) {
              document.documentElement.scrollIntoView({
                behavior: "smooth",
                block: "end",
                inline: "nearest"
              });
            }
          }
        }
    
        //连接关闭的回调方法
        websocket.onclose = function () {
          console.log("WebSocket连接关闭")
        };
      }
    </script>
                    """.trimIndent()
            )
            writer.println(prettyMessageHTMLLayout.fileFooter)
        }
    }

    private fun showLogFile(filename: String, logMsgs: List<LogMsg>?, collapse: Boolean?) {
        response.contentType = "text/html;charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setHeader("Content-Encoding", "gzip")
        response.setDateHeader("Expires", 0)
        val prettyMessageHTMLLayout = PrettyMessageHTMLLayout()
        prettyMessageHTMLLayout.title = "$appName $filename"
        prettyMessageHTMLLayout.context = loggerContext
        prettyMessageHTMLLayout.start()
        val gzipOutputStream = GZIPOutputStream(response.outputStream).bufferedWriter()
        gzipOutputStream.use { writer ->
            try {
                writer.appendLine(prettyMessageHTMLLayout.fileHeader)
                val header = prettyMessageHTMLLayout.getLogsHeader()
                writer.appendLine(header)

                if (!logMsgs.isNullOrEmpty()) {
                    val size = logMsgs.size
                    logMsgs.forEachIndexed { index, it ->
                        writer.appendLine(
                            prettyMessageHTMLLayout.doLayout(
                                it.msg,
                                it.level,
                                collapse,
                                index == size - 1
                            )
                        )
                    }
                }

                writer.appendLine(prettyMessageHTMLLayout.presentationFooter)
                writer.appendLine(
                    """
        <script type="text/javascript">
            if(!location.hash){
                window.location.href = '#last';
            }
            document.addEventListener('copy', function(event) {
                    // 阻止默认的复制行为
                    event.preventDefault();
        
                    // 获取要复制的内容
                    let originalText = window.getSelection().toString();
        
                    // 修改复制的内容
                    let modifiedText = originalText.replace(/#/g, '');
        
                    // 将修改后的内容放入剪贴板
                    if (event.clipboardData) {
                        event.clipboardData.setData('text/plain', modifiedText);
                    } else if (window.clipboardData) { // 兼容IE
                        window.clipboardData.setData('Text', modifiedText);
                    }
                });
        </script>
        """
                )
                writer.appendLine(prettyMessageHTMLLayout.fileFooter)
            } catch (e: SocketTimeoutException) {
                log.error(AlarmMarker.noAlarmMarker, "Error while writing response", e)
            }
        }
    }


    private fun index(
        files: Array<File>?,
        root: Boolean,
        requestPath: String,
        filterEmpty: Boolean
    ) {
        if (!files.isNullOrEmpty()) {
            val path = if (root) basePath else "$basePath/$requestPath"
            response.contentType = "text/html; charset=utf-8"
            response.setHeader("Pragma", "No-cache")
            response.setHeader("Cache-Control", "no-cache")
            response.setDateHeader("Expires", 0)
            response.writer.use { writer ->
                try {
                    var dir = requestPath
                    dir = if (dir.startsWith("/")) dir else "/$dir"
                    writer.println(
                        """<!DOCTYPE html>
    <html>
    <head><title>$appName Index of $dir</title></head>
    <body>"""
                    )
                    writer.print("<h1>Index of $dir</h1><hr><pre>")

                    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    if (!root)
                        writer.println(
                            "<a href=\"$basePath/${
                                requestPath.substringBeforeLast(
                                    "/",
                                    ""
                                )
                            }\">../</a>"
                        )
                    else {
                        if (useWebSocket) {
                            writer.println(
                                "<a style=\"display:inline-block;width:100px;\" href=\"$basePath/real-time\">实时日志/</a>                                        ${
                                    TimeUtil.now().format(dateTimeFormatter)
                                }       -"
                            )
                        }
                    }
                    val hasAll = files.any { it.name.startsWith("all-") || it.name == "all.log" }
                    if (hasAll)
                        writer.println(
                            "<a style=\"display:inline-block;width:100px;\" href=\"$path/daily\">daily/</a>                                        ${
                                TimeUtil.now().format(dateTimeFormatter)
                            }       -"
                        )

                    files.sortWith(comparator)
                    files.filter { if (filterEmpty) it.length() > 0 else true }.forEach {
                        val millis = it.lastModified()
                        val lastModify =
                            if (millis == 0L) "-" else TimeUtil.of(millis).format(dateTimeFormatter)
                        if (it.isDirectory) {
                            writer.println(
                                "<a style=\"display:inline-block;width:100px;\" href=\"$path/${it.name}/\">${it.name}/</a>                                        $lastModify       -"
                            )
                        } else {
                            writer.println(
                                "<a style=\"display:inline-block;width:100px;\" href=\"$path/${it.name}#last\">${it.name}</a>                                        $lastModify       ${
                                    prettyValue(
                                        it.length()
                                    )
                                }"
                            )
                        }
                    }
                    writer.println("</pre><hr></body>\n</html>")
                } catch (e: SocketTimeoutException) {
                    log.error(AlarmMarker.noAlarmMarker, "Error while writing response", e)
                }
            }
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }

    private val comparator: Comparator<File> = LogFileNameComparator()


    private val units: Array<String> = arrayOf("B", "K", "M", "G", "T", "P", "E")

    /**
     * 返回易读的值
     *
     * @param value 值，单位B
     * @return 易读的值
     */
    private fun prettyValue(value: Long): String {
        if (value == 0L) {
            return "-"
        } else {
            var newValue = value.toDouble()
            var index = 0
            var lastValue = 0.0
            while (newValue / 1024 >= 1 && index < units.size - 1) {
                lastValue = newValue
                newValue /= 1024
                index++
            }
            var newScale = index - 2
            newScale = max(newScale, 0)
            val result =
                if (lastValue == 0.0)
                    newValue.toBigDecimal()
                else
                    lastValue.toBigDecimal().divide(
                        BigDecimal(1024), RoundingMode.UP
                    ).setScale(newScale, RoundingMode.UP)
            return result.stripTrailingZeros().toPlainString() + units[index]
        }
    }

    private fun readLogMsgs(
        inputStream: InputStream,
        gzip: Boolean = false,
        traceid: String?
    ): List<LogMsg> {
        val lines = if (gzip) {
            GZIPInputStream(inputStream).bufferedReader().lines()
        } else {
            inputStream.bufferedReader().lines()
        }

        //2024-09-12 11:55:06.505  INFO [exec-7] t.b.s.s.r.RedisStoreTokenRepository      RedisStoreTokenRepository.kt:138 4385dd54: msg
        val regrex =
            Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}) +([A-Z]+) +(\\[.*?]) +(\\S+) +(\\S+) +(\\S*): (.*)")

        val msgs = mutableListOf<LogMsg>()
        var msg = StringBuilder("")
        var level = "DEFAULT"
        var traceIdMatch = traceid.isNullOrBlank()

        lines.forEach { line ->
            val matchResult = regrex.matchEntire(line)
            if (matchResult != null) {
                val groupValues = matchResult.groupValues
                if (msg.isNotBlank() && traceIdMatch) {
                    msgs.add(LogMsg(level, msg.toString()))
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
            msgs.add(LogMsg(level, msg.toString()))
        }
        return msgs
    }
}
