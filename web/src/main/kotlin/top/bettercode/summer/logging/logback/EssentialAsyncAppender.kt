package top.bettercode.summer.logging.logback

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.spi.ILoggingEvent

/**
 *
 * @author Peter Wu
 */
class EssentialAsyncAppender : AsyncAppender() {

    override fun isDiscardable(event: ILoggingEvent?): Boolean {
        return false
    }
}