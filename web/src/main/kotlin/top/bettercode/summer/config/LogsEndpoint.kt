package top.bettercode.summer.config

import ch.qos.logback.classic.LoggerContext
import org.slf4j.ILoggerFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.lang.Nullable
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestHeader
import top.bettercode.summer.logging.WebsocketProperties
import top.bettercode.summer.tools.lang.PrettyMessageHTMLLayout
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.File
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.servlet.http.HttpServletResponse
import kotlin.math.max

/**
 * 日志
 */
@Endpoint(id = "logs")
class LogsEndpoint(
        private val loggingFilesPath: String,
        environment: Environment,
        private val websocketProperties: WebsocketProperties,
        serverProperties: ServerProperties,
        private val response: HttpServletResponse,
        webEndpointProperties: WebEndpointProperties
) {

    private val contextPath: String = serverProperties.servlet.contextPath ?: "/"
    private val logsPath: String = contextPath + webEndpointProperties.basePath + "/logs"

    private val useWebSocket: Boolean = ClassUtils.isPresent(
            "org.springframework.web.socket.server.standard.ServerEndpointExporter",
            LogsEndpoint::class.java.classLoader
    ) && ("true" == environment.getProperty("summer.logging.websocket.enabled") || environment.getProperty(
            "summer.logging.websocket.enabled"
    ).isNullOrBlank())
    private val loggerContext: LoggerContext by lazy {
        val factory = LoggerFactory.getILoggerFactory()
        Assert.isInstanceOf(
                LoggerContext::class.java, factory,
                String.format(
                        "LoggerFactory is not a Logback LoggerContext but Logback is on "
                                + "the classpath. Either remove Logback or the competing "
                                + "implementation (%s loaded from %s). If you are using "
                                + "WebLogic you will need to add 'org.slf4j' to "
                                + "prefer-application-packages in WEB-INF/weblogic.xml",
                        factory.javaClass, getLocation(factory)
                )
        )
        factory as LoggerContext
    }

    private fun getLocation(factory: ILoggerFactory): Any {
        try {
            val protectionDomain = factory.javaClass.protectionDomain
            val codeSource = protectionDomain.codeSource
            if (codeSource != null) {
                return codeSource.location
            }
        } catch (ex: SecurityException) {
            // Unable to determine location
        }

        return "unknown location"
    }

    @ReadOperation
    fun root() {
        index(File(loggingFilesPath).listFiles(), true, "")
    }

    @ReadOperation
    fun path(@Selector(match = Selector.Match.ALL_REMAINING) path: String, @Nullable collapse: Boolean?, @Nullable @RequestHeader(value = "User-Agent", required = false) userAgent: String?) {
        val requestPath = path.replace(",", "/")

        if ("real-time" != path) {
            val paths = path.split(",")
            if (paths[0] == "daily") {
                val today = TimeUtil.now().format("yyyy-MM-dd")
                if (paths.size == 1) {
                    val filenames =
                            File(loggingFilesPath).listFiles { _, filename -> filename.startsWith("all-") }
                                    ?.map {
                                        it.nameWithoutExtension.replace(
                                                Regex("all-(\\d{4}-\\d{2}-\\d{2})-\\d+"),
                                                "$1"
                                        )
                                    }?.toMutableSet() ?: mutableSetOf()
                    if (!filenames.contains(today)) {
                        filenames.add(today)
                    }
                    index(filenames.map { File(it) }.toTypedArray(), false, requestPath)
                } else if (paths.size == 2) {
                    var logPattern = paths[1]
                    val html =
                            if (logPattern.endsWith(".html")) {
                                logPattern = logPattern.substringBeforeLast(".html")
                                true
                            } else false

                    val matchCurrent = today.startsWith(logPattern)
                    val files =
                            File(loggingFilesPath).listFiles { _, filename -> filename.startsWith("all-$logPattern") || matchCurrent && filename == "all.log" }

                    if (!files.isNullOrEmpty()) {
                        files.sortWith(compareBy { it.lastModified() })

                        if (html) {
                            val logMsgs = mutableListOf<LogMsg>()
                            files.forEach { file ->
                                logMsgs.addAll(
                                        readLogMsgs(
                                                file.inputStream(),
                                                "gz" == file.extension
                                        )
                                )
                            }
                            showLogFile(logPattern, logMsgs, collapse)
                        } else {
                            val fileName = "$logPattern.log.gz"

                            val newFileName: String =
                                    if (null != userAgent && (userAgent.contains("Trident") || userAgent.contains("Edge"))) {
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
                    } else {
                        response.sendError(HttpStatus.NOT_FOUND.value(), "no log file match")
                    }
                }
            } else {
                var file = File(loggingFilesPath, requestPath)
                if (!file.exists() && file.name.startsWith("all-")) {
                    file = File(loggingFilesPath, "all.log")
                }
                if (file.isFile) {
                    if (!file.exists()) {
                        response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
                    } else {
                        val logMsgs = readLogMsgs(file.inputStream(), "gz" == file.extension)
                        showLogFile(file.name, logMsgs, collapse)
                    }
                } else {
                    index(file.listFiles(), false, requestPath)
                }
            }
        } else {
            if (useWebSocket) {
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
    var loc = window.location, webhost;
    if (loc.protocol === "https:") {
      webhost = "wss:";
    } else {
      webhost = "ws:";
    }
    webhost += "//" + loc.host;

    websocket = new WebSocket(webhost + "${"$contextPath/websocket/logging"}?token=${websocketProperties.token}");
    
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
            } else {
                response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
            }
        }
    }

    private fun showLogFile(name: String, logMsgs: List<LogMsg>?, collapse: Boolean?) {
        response.contentType = "text/html;charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setHeader("Content-Encoding", "gzip")
        response.setDateHeader("Expires", 0)
        val prettyMessageHTMLLayout = PrettyMessageHTMLLayout()
        prettyMessageHTMLLayout.title = name
        prettyMessageHTMLLayout.context = loggerContext
        prettyMessageHTMLLayout.start()
        val gzipOutputStream = GZIPOutputStream(response.outputStream).bufferedWriter()
        gzipOutputStream.use { writer ->
            writer.appendLine(prettyMessageHTMLLayout.fileHeader)
            writer.appendLine(prettyMessageHTMLLayout.getLogsHeader())

            if (!logMsgs.isNullOrEmpty()) {
                val size = logMsgs.size
                logMsgs.forEachIndexed { index, it ->
                    writer.appendLine(
                            prettyMessageHTMLLayout.doLayout(it.msg, it.level, collapse, index == size - 1)
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
</script>
"""
            )
            writer.appendLine(prettyMessageHTMLLayout.fileFooter)
        }
        gzipOutputStream.flush()
        response.flushBuffer()
    }


    private fun index(
            files: Array<File>?,
            root: Boolean,
            requestPath: String
    ) {
        if (!files.isNullOrEmpty()) {
            val path = if (root) logsPath else "$logsPath/$requestPath"
            response.contentType = "text/html; charset=utf-8"
            response.setHeader("Pragma", "No-cache")
            response.setHeader("Cache-Control", "no-cache")
            response.setDateHeader("Expires", 0)
            response.writer.use { writer ->
                var dir = requestPath
                dir = if (dir.startsWith("/")) dir else "/$dir"
                writer.println(
                        """
<html>
<head><title>Index of $dir</title></head>
<body>"""
                )
                writer.print("<h1>Index of $dir</h1><hr><pre>")

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                if (!root)
                    writer.println("<a href=\"$logsPath/${requestPath.substringBeforeLast("/", "")}\">../</a>")
                else {
                    if (useWebSocket) {
                        writer.println(
                                "<a style=\"display:inline-block;width:100px;\" href=\"$logsPath/real-time\">实时日志/</a>                                        ${
                                    TimeUtil.now().format(dateTimeFormatter)
                                }       -"
                        )
                    }
                    writer.println(
                            "<a style=\"display:inline-block;width:100px;\" href=\"$logsPath/daily\">daily/</a>                                        ${
                                TimeUtil.now().format(dateTimeFormatter)
                            }       -"
                    )
                }

                files.sortWith(comparator)
                files.forEach {
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
            }
            response.flushBuffer()
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }

    private fun readLogMsgs(inputStream: InputStream, gzip: Boolean = false): List<LogMsg> {
        val lines = if (gzip) {
            GZIPInputStream(inputStream).bufferedReader().lines()
        } else {
            inputStream.bufferedReader().lines()
        }
        val regrex =
                Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}) +([A-Z]+) +(\\d+) +--- +\\[([a-zA-Z0-9 \\-]+)] +(\\S+) +:(.*)")

        val msgs = mutableListOf<LogMsg>()
        var msg = StringBuilder("")
        var level = "DEFAULT"

        lines.forEach { line ->
            val matchResult = regrex.matchEntire(line)
            if (matchResult != null) {
                val groupValues = matchResult.groupValues

                if (msg.isNotBlank()) {
                    msgs.add(LogMsg(level, msg.toString()))
                }
                msg = java.lang.StringBuilder(line)
                level = groupValues[2]
            } else {
                msg.append(StringUtil.LINE_SEPARATOR)
                msg.append(line)
            }
        }
        if (msg.isNotBlank()) {
            msgs.add(LogMsg(level, msg.toString()))
        }
        return msgs
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
                    if (lastValue == 0.0) newValue.toString() else BigDecimal(lastValue).divide(
                            BigDecimal(1024), RoundingMode.UP
                    )
                            .setScale(newScale, RoundingMode.UP).toString()
            return trimTrailing(result) + units[index]
        }
    }

    private fun trimTrailing(value: String): String {
        return if (value.contains(".")) StringUtils
                .trimTrailingCharacter(
                        StringUtils.trimTrailingCharacter(
                                value, '0'
                        ), '.'
                ) else value
    }

}
