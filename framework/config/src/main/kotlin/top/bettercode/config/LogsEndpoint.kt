package top.bettercode.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.CoreConstants
import org.slf4j.ILoggerFactory
import org.slf4j.impl.StaticLoggerBinder
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import top.bettercode.lang.PrettyMessageHTMLLayout
import top.bettercode.lang.util.TimeUtil
import top.bettercode.logging.WebsocketProperties
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import javax.servlet.http.HttpServletRequest
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
    private val serverProperties: ServerProperties,
    private val request: HttpServletRequest,
    private val response: HttpServletResponse
) {

    private val useWebSocket: Boolean = ClassUtils.isPresent(
        "org.springframework.web.socket.server.standard.ServerEndpointExporter",
        LogsEndpoint::class.java.classLoader
    ) && ("true" == environment.getProperty("summer.logging.websocket.enabled") || environment.getProperty(
        "summer.logging.websocket.enabled"
    ).isNullOrBlank())
    private val loggerContext: LoggerContext by lazy {
        val factory = StaticLoggerBinder.getSingleton().loggerFactory
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
        index(File(loggingFilesPath), request, response, true)
    }

    @ReadOperation
    fun path(@Selector(match = Selector.Match.ALL_REMAINING) path: String) {
        if ("real-time" != path) {
            response.contentType = "text/plain;charset=UTF-8"
            val file = File(loggingFilesPath, path.replace(",", "/"))
            if (file.isFile) {
                showLogFile(response, file)
            } else {
                index(file, request, response, false)
            }
        } else {
            if (useWebSocket) {
                val wsUrl =
                    "ws://" + request.getHeader(HttpHeaders.HOST)
                        .substringBefore(":") + ":" + serverProperties.port + request.contextPath + "/websocket/logging"
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
    websocket = new WebSocket("$wsUrl?token=${websocketProperties.token}");
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


    private fun showLogFile(response: HttpServletResponse, logFile: File) {
        if (logFile.exists()) {
            if (logFile.isFile) {
                response.contentType = "text/html;charset=utf-8"
                response.setHeader("Pragma", "No-cache")
                response.setHeader("Cache-Control", "no-cache")
                response.setDateHeader("Expires", 0)
                val prettyMessageHTMLLayout = PrettyMessageHTMLLayout()
                prettyMessageHTMLLayout.title = logFile.name
                prettyMessageHTMLLayout.context = loggerContext
                prettyMessageHTMLLayout.start()
                response.writer.use { writer ->
                    writer.println(prettyMessageHTMLLayout.fileHeader)
                    writer.println(prettyMessageHTMLLayout.getLogsHeader())

                    var msg = StringBuilder("")
                    var level: String? = null
                    val lines = if ("gz".equals(logFile.extension, true)) {
                        GZIPInputStream(FileInputStream(logFile)).bufferedReader().lines()
                    } else {
                        logFile.readLines().stream()
                    }
                    lines.forEach {
                        val m = it.substringAfter(" ", "").substringAfter(" ", "").trimStart()
                        val llevel = when {
                            m.startsWith(Level.TRACE.levelStr) -> Level.TRACE.levelStr
                            m.startsWith(Level.DEBUG.levelStr) -> Level.DEBUG.levelStr
                            m.startsWith(Level.INFO.levelStr) -> Level.INFO.levelStr
                            m.startsWith(Level.WARN.levelStr) -> Level.WARN.levelStr
                            m.startsWith(Level.ERROR.levelStr) -> Level.ERROR.levelStr
                            m.startsWith(Level.OFF.levelStr) -> Level.OFF.levelStr
                            else -> null
                        }
                        if (llevel != null) {
                            if (msg.isNotBlank()) {
                                writer.println(
                                    prettyMessageHTMLLayout.doLayout(
                                        msg.toString(), level
                                            ?: Level.INFO.levelStr
                                    )
                                )
                            }
                            msg = java.lang.StringBuilder(it)
                            level = llevel
                        } else {
                            msg.append(CoreConstants.LINE_SEPARATOR)
                            msg.append(it)
                        }
                    }
                    if (msg.isNotBlank()) {
                        writer.println(
                            prettyMessageHTMLLayout.doLayout(
                                msg.toString(), level
                                    ?: Level.INFO.levelStr, true
                            )
                        )
                    }
                    writer.println(prettyMessageHTMLLayout.presentationFooter)
                    writer.println(
                        """
<script type="text/javascript">
    if(!location.hash){
        window.location.href = '#last';
    }
</script>
"""
                    )

                    writer.println(prettyMessageHTMLLayout.fileFooter)
                }
            } else {
                response.sendError(HttpStatus.CONFLICT.value(), "Path is directory")
            }
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }

    private val comparator: Comparator<File> = LogFileNameComparator()

    private fun index(
        file: File,
        request: HttpServletRequest,
        response: HttpServletResponse,
        root: Boolean
    ) {
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
                var dir = servletPath.substringAfterLast("/logs")
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
                    writer.println("<a href=\"$upPath\">../</a>")
                else if (useWebSocket) {
                    writer.println(
                        "<a style=\"display:inline-block;width:100px;\" href=\"$path/real-time\">实时日志/</a>                                        ${
                            TimeUtil.now().format(dateTimeFormatter)
                        }       -"
                    )
                }

                val listFiles = file.listFiles()
                listFiles?.sortWith(comparator)
                listFiles?.forEach { it ->
                    if (it.isDirectory) {
                        writer.println(
                            "<a style=\"display:inline-block;width:100px;\" href=\"$path/${it.name}/\">${it.name}/</a>                                        ${
                                TimeUtil.of(
                                    it.lastModified()
                                ).format(dateTimeFormatter)
                            }       -"
                        )
                    } else {
                        writer.println(
                            "<a style=\"display:inline-block;width:100px;\" href=\"$path/${it.name}#last\">${it.name}</a>                                        ${
                                TimeUtil.of(
                                    it.lastModified()
                                ).format(dateTimeFormatter)
                            }       ${prettyValue(it.length())}"
                        )
                    }
                }
                writer.println("</pre><hr></body>\n</html>")
            }
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }

    private val units: Array<String> = arrayOf("B", "K", "M", "G", "T", "P", "E")

    /**
     * 返回易读的值
     *
     * @param value 值，单位B
     * @return 易读的值
     */
    private fun prettyValue(value: Long): String {
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
        val result = if (lastValue == 0.0) newValue.toString() else BigDecimal(lastValue).divide(
            BigDecimal(1024), RoundingMode.UP
        )
            .setScale(newScale, RoundingMode.UP).toString()
        return trimTrailing(result) + units[index]
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
