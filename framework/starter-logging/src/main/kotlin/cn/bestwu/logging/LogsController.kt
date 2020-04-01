package cn.bestwu.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.CoreConstants
import cn.bestwu.lang.util.LocalDateTimeHelper
import cn.bestwu.logging.annotation.NoRequestLogging
import cn.bestwu.logging.logback.Logback2LoggingSystem
import cn.bestwu.logging.logback.PrettyMessageHTMLLayout
import org.slf4j.impl.StaticLoggerBinder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
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
                     private val loggingFilesPath: String, environment: Environment) {

    private val useWebSocket: Boolean = ClassUtils.isPresent("org.springframework.web.socket.server.standard.ServerEndpointExporter", Logback2LoggingSystem::class.java.classLoader) && "true" == environment.getProperty("logging.websocket.enabled")
    private val loggerContext: LoggerContext
        get() {
            val factory = StaticLoggerBinder.getSingleton().loggerFactory
            Assert.isInstanceOf(LoggerContext::class.java, factory,
                    String.format(
                            "LoggerFactory is not a Logback LoggerContext but Logback is on "
                                    + "the classpath. Either remove Logback or the competing "
                                    + "implementation (%s loaded from %s). If you are using "
                                    + "WebLogic you will need to add 'org.slf4j' to "
                                    + "prefer-application-packages in WEB-INF/weblogic.xml",
                            factory.javaClass, Logback2LoggingSystem.getLocation(factory)))
            return factory as LoggerContext
        }

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


    @NoRequestLogging
    @GetMapping(value = ["/real-time"], name = "实时日志")
    @Throws(IOException::class)
    fun websocketLog(request: HttpServletRequest, response: HttpServletResponse) {
        if (useWebSocket) {
            val wsUrl = request.requestURL.toString().replace("https://", "ws://").replace("http://", "ws://").substringBeforeLast("/logs/real-time") + "/websocket/logging"
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
                writer.println(prettyMessageHTMLLayout.presentationHeader)
                writer.println("""</table>
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
    websocket = new WebSocket("$wsUrl");
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
                """.trimIndent())
                writer.println(prettyMessageHTMLLayout.fileFooter)
            }
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Page not found")
        }
    }


    private fun showLogFile(response: HttpServletResponse, logFile: File) {
        if (logFile.exists()) {
            if (logFile.isFile) {
                if (logFile.extension == "log") {
                    response.contentType = "text/html;charset=utf-8"
                } else {
                    response.setHeader("Content-Disposition",
                            "attachment;filename=${logFile.name};filename*=UTF-8''" + URLEncoder
                                    .encode(logFile.name, "UTF-8"))
                    response.contentType = "application/octet-stream"
                }
                response.setHeader("Pragma", "No-cache")
                response.setHeader("Cache-Control", "no-cache")
                response.setDateHeader("Expires", 0)
                if (logFile.extension == "log") {
                    val prettyMessageHTMLLayout = PrettyMessageHTMLLayout()
                    prettyMessageHTMLLayout.title = logFile.name
                    prettyMessageHTMLLayout.context = loggerContext
                    prettyMessageHTMLLayout.start()
                    response.writer.use { writer ->
                        writer.println(prettyMessageHTMLLayout.fileHeader)
                        writer.println(prettyMessageHTMLLayout.presentationHeader)

                        var msg = ""
                        var level: String? = null
                        logFile.readLines().forEach {
                            val llevel = when {
                                it.contains(Level.TRACE.levelStr) -> Level.TRACE.levelStr
                                it.contains(Level.DEBUG.levelStr) -> Level.DEBUG.levelStr
                                it.contains(Level.INFO.levelStr) -> Level.INFO.levelStr
                                it.contains(Level.WARN.levelStr) -> Level.WARN.levelStr
                                it.contains(Level.ERROR.levelStr) -> Level.ERROR.levelStr
                                it.contains(Level.OFF.levelStr) -> Level.OFF.levelStr
                                else -> null
                            }
                            if (llevel != null) {
                                if (!msg.isBlank()) {
                                    writer.println(prettyMessageHTMLLayout.doLayout(msg, level
                                            ?: Level.INFO.levelStr))
                                }
                                msg = it
                                level = llevel
                            } else {
                                msg += CoreConstants.LINE_SEPARATOR
                                msg += it
                            }
                        }
                        writer.println(prettyMessageHTMLLayout.presentationFooter)
                        writer.println(prettyMessageHTMLLayout.fileFooter)
                    }
                } else {
                    StreamUtils.copy(FileInputStream(logFile), response.outputStream)
                }
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
                if (useWebSocket)
                    writer.println("<a style=\"display:inline-block;width:100px;\" href=\"$path/real-time\">实时日志/</a>                                        ${LocalDateTimeHelper.now().format()}       -")
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
