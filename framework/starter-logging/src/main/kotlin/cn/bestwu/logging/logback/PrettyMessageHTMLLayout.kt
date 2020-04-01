package cn.bestwu.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.html.HTMLLayout
import ch.qos.logback.classic.pattern.MessageConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.CoreConstants.LINE_SEPARATOR
import ch.qos.logback.core.helpers.Transform
import ch.qos.logback.core.pattern.Converter
import cn.bestwu.logging.dateFormat
import java.util.*

/**
 * @author Peter Wu
 */
class PrettyMessageHTMLLayout : HTMLLayout() {

    init {
        setCssBuilder(CustCssBuilder())
    }

    override fun getPresentationHeader(): String {
        val sbuf = StringBuilder()
        sbuf.append("<hr/>")
        sbuf.append(LINE_SEPARATOR)
        sbuf.append("<p>Log session start time ")
        sbuf.append(Date())
        sbuf.append("</p><p></p>")
        sbuf.append(LINE_SEPARATOR)
        sbuf.append(LINE_SEPARATOR)
        sbuf.append("<table id=\"loggingText\" cellspacing=\"0\" cellpadding=\"0\">")
        sbuf.append(LINE_SEPARATOR)

        buildHeaderRowForTable(sbuf)

        return sbuf.toString()
    }

    private fun buildHeaderRowForTable(sbuf: StringBuilder) {
        var c: Converter<*>? = head
        var name: String?
        sbuf.append("<tr class=\"header\">")
        sbuf.append(LINE_SEPARATOR)
        while (c != null) {
            name = computeConverterName(c)
            if (name == null) {
                c = c.next
                continue
            }
            if (c is MessageConverter) {
                sbuf.append("<td class=\"")
                sbuf.append(computeConverterName(c))
                sbuf.append("\">")
                sbuf.append(computeConverterName(c))
                sbuf.append("</td>")
                sbuf.append(LINE_SEPARATOR)
            }
            c = c.next
        }
        sbuf.append("</tr>")
        sbuf.append(LINE_SEPARATOR)
    }

    override fun doLayout(event: ILoggingEvent): String {
        val buf = StringBuilder()
        startNewTableIfLimitReached(buf)

        var odd = true
        if (counter++ and 1 == 0L) {
            odd = false
        }

        val level = event.level.toString().toLowerCase()

        buf.append(LINE_SEPARATOR)
        buf.append("<tr class=\"")
        buf.append(level)
        if (odd) {
            buf.append(" odd\">")
        } else {
            buf.append(" even\">")
        }
        buf.append(LINE_SEPARATOR)

        var c: Converter<ILoggingEvent>? = head
        while (c != null) {
            if (c is MessageConverter)
                appendEventToBuffer(buf, c, event)
            c = c.next
        }
        buf.append("</tr>")
        buf.append(LINE_SEPARATOR)

        if (event.throwableProxy != null) {
            throwableRenderer.render(buf, event)
        }
        return buf.toString()
    }

    private fun appendEventToBuffer(buf: StringBuilder, c: Converter<ILoggingEvent>,
                                    event: ILoggingEvent) {
        buf.append("<td class=\"")
        when {
            event.level == Level.WARN -> buf.append("Warn")
            event.level.isGreaterOrEqual(Level.ERROR) -> buf.append("Exception")
            else -> buf.append(computeConverterName(c))
        }
        buf.append("\"><pre>")
        buf.append("${dateFormat.format(Date(event.timeStamp))} ")
        buf.append("${event.level} ")
        buf.append("[${event.threadName}] ")
        buf.append("${event.loggerName} :")
        buf.append(Transform.escapeTags(c.convert(event)))
        buf.append("</pre></td>")
        buf.append(LINE_SEPARATOR)
    }


    fun doLayout(msg: String, level: String): String {
        val buf = StringBuilder()
        startNewTableIfLimitReached(buf)

        var odd = true
        if (counter++ and 1 == 0L) {
            odd = false
        }

        buf.append(LINE_SEPARATOR)
        buf.append("<tr class=\"")
        buf.append(level)
        if (odd) {
            buf.append(" odd\">")
        } else {
            buf.append(" even\">")
        }
        buf.append(LINE_SEPARATOR)

        var c: Converter<ILoggingEvent>? = head
        while (c != null) {
            if (c is MessageConverter) {
                buf.append("<td class=\"")
                when {
                    Level.valueOf(level) == Level.WARN -> buf.append("Warn")
                    Level.valueOf(level).isGreaterOrEqual(Level.ERROR) -> buf.append("Exception")
                    else -> buf.append(computeConverterName(c))
                }
                buf.append("\"><pre>")
                buf.append(Transform.escapeTags(msg))
                buf.append("</pre></td>")
                buf.append(LINE_SEPARATOR)
            }
            c = c.next
        }
        buf.append("</tr>")
        buf.append(LINE_SEPARATOR)
        return buf.toString()
    }
}
