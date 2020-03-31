package cn.bestwu.logging.websocket

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Layout
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.util.OptionHelper
import cn.bestwu.logging.logback.Logback2LoggingSystem
import cn.bestwu.logging.logback.PrettyMessageHTMLLayout

/**
 *
 * @author Peter Wu
 */
class WebSocketAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    val layout: Layout<ILoggingEvent> = PrettyMessageHTMLLayout()

    override fun start() {
        layout.context = context
        layout.start()
        super.start()
    }

    override fun append(event: ILoggingEvent) {
        val message = layout.doLayout(event)
        try {
            WebSocketController.send(message)
        } catch (e: Exception) {
            addError(e.message, e)
        }
    }
}