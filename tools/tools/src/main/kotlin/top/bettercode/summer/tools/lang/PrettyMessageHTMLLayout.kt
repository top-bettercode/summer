package top.bettercode.summer.tools.lang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.html.HTMLLayout
import ch.qos.logback.classic.pattern.MessageConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.helpers.Transform
import ch.qos.logback.core.pattern.Converter
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
import java.util.*

/**
 * @author Peter Wu
 */
class PrettyMessageHTMLLayout : HTMLLayout() {

    companion object {
        fun anchor(msg: String): String {
            val s = msg.trim().substringBefore(StringUtil.LINE_SEPARATOR)
            val regex = Regex(".*(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3} +[A-Z]+) (?s).*")
            return if (s.matches(regex)) {
                s.replace(regex, "$1").split(' ').filter { it.isNotBlank() }.joinToString("-")
                        .lowercase(Locale.getDefault())
            } else ""
        }
    }

    init {
        setCssBuilder(CustCssBuilder())
    }

    override fun getPresentationHeader(): String {
        val sbuf = StringBuilder()
        sbuf.append("<hr/>")
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append("<p>Log session start time ")
        sbuf.append(Date())
        sbuf.append("</p><p></p>")
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append("<table id=\"loggingText\" cellspacing=\"0\" cellpadding=\"0\">")
        sbuf.append(StringUtil.LINE_SEPARATOR)

        buildHeaderRowForTable(sbuf)

        return sbuf.toString()
    }

    fun getLogsHeader(): String {
        val sbuf = StringBuilder()
        sbuf.append("<hr/>")
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append("<p>Log session start time ")
        sbuf.append(Date())
        sbuf.append("&nbsp;&nbsp;&nbsp;&nbsp;<a href='./'>../</a>")
        sbuf.append("</p><p></p>")
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append("<table id=\"loggingText\" cellspacing=\"0\" cellpadding=\"0\">")
        sbuf.append(StringUtil.LINE_SEPARATOR)

        buildHeaderRowForTable(sbuf)

        return sbuf.toString()
    }

    private fun buildHeaderRowForTable(sbuf: StringBuilder) {
        sbuf.append("<tr class=\"header\">")
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append("<td class=\"Message\">Message</td>")
        sbuf.append(StringUtil.LINE_SEPARATOR)
        sbuf.append("</tr>")
        sbuf.append(StringUtil.LINE_SEPARATOR)
    }

    override fun doLayout(event: ILoggingEvent): String {
        val buf = StringBuilder()
        startNewTableIfLimitReached(buf)

        var odd = true
        if (counter++ and 1 == 0L) {
            odd = false
        }

        val level = event.level.toString().lowercase(Locale.getDefault())

        buf.append(StringUtil.LINE_SEPARATOR)
        buf.append("<tr class=\"")
        buf.append(level)
        if (odd) {
            buf.append(" odd\">")
        } else {
            buf.append(" even\">")
        }
        buf.append(StringUtil.LINE_SEPARATOR)

        var c: Converter<ILoggingEvent>? = head
        while (c != null) {
            if (c is MessageConverter)
                appendEventToBuffer(buf, c, event)
            c = c.next
        }
        buf.append("</tr>")
        buf.append(StringUtil.LINE_SEPARATOR)

        if (event.throwableProxy != null) {
            throwableRenderer.render(buf, event)
        }
        return buf.toString()
    }

    private fun appendEventToBuffer(
            buf: StringBuilder, c: Converter<ILoggingEvent>,
            event: ILoggingEvent
    ) {
        buf.append("<td class=\"")
        when {
            event.level == Level.WARN -> buf.append("Warn")
            event.level.isGreaterOrEqual(Level.ERROR) -> buf.append("Exception")
            else -> buf.append("Message")
        }
        buf.append("\"><pre>")
        buf.append("${TimeUtil.format(
            timeStamp = event.timeStamp,
            pattern = DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
        )} ")
        buf.append("${event.level} ")
        buf.append("[${event.threadName}] ")
        buf.append("${event.loggerName} :")
        buf.append(Transform.escapeTags(c.convert(event)))
        buf.append("</pre></td>")
        buf.append(StringUtil.LINE_SEPARATOR)
    }


    fun doLayout(msg: String, level: String, collapse: Boolean?, last: Boolean = false): String {
        val buf = StringBuilder()
        startNewTableIfLimitReached(buf)

        var odd = true
        if (counter++ and 1 == 0L) {
            odd = false
        }

        buf.append(StringUtil.LINE_SEPARATOR)
        val anchor = anchor(msg)
        buf.append("<tr id=\"$anchor\" onclick=\"if(window.event.ctrlKey){window.location.href = '#$anchor';}\" class=\"")
        buf.append(level)
        if (odd) {
            buf.append(" odd\">")
        } else {
            buf.append(" even\">")
        }
        buf.append(StringUtil.LINE_SEPARATOR)

        buf.append("<td${if (last) " id=\"last\"" else ""} class=\"")
        when {
            Level.valueOf(level) == Level.WARN -> buf.append("Warn")
            Level.valueOf(level).isGreaterOrEqual(Level.ERROR) -> buf.append("Exception")
            else -> buf.append("Message")
        }
        buf.append("\">")

        val escapeMsg = Transform.escapeTags(msg)
        if (collapse == true && escapeMsg.contains(HttpOperation.SEPARATOR_LINE)) {
            buf.append("<details>")
            buf.append("<summary>")
            buf.append("<pre>")
            buf.append(escapeMsg.substringBefore(HttpOperation.SEPARATOR_LINE))
            buf.append("</pre>")
            buf.append("</summary>")
            buf.append("<pre>")
            buf.append(escapeMsg.substringAfter(HttpOperation.SEPARATOR_LINE))
            buf.append("</pre>")
            buf.append("</details>")
        } else {
            buf.append("<pre>")
            buf.append(escapeMsg)
            buf.append("</pre>")
        }

        buf.append("</td>")
        buf.append(StringUtil.LINE_SEPARATOR)
        buf.append("</tr>")
        buf.append(StringUtil.LINE_SEPARATOR)
        return buf.toString()
    }

    override fun startNewTableIfLimitReached(sbuf: StringBuilder) {
        if (counter >= 10000L) {
            counter = 0L
            sbuf.append("</table>")
            sbuf.append(StringUtil.LINE_SEPARATOR)
            sbuf.append("<p></p>")
            sbuf.append("<table cellspacing=\"0\" cellpadding=\"0\">")
            sbuf.append(StringUtil.LINE_SEPARATOR)
            buildHeaderRowForTable(sbuf)
        }
    }

}
