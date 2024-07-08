package top.bettercode.summer.tools.lang.log

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.boolex.EvaluationException
import ch.qos.logback.core.boolex.EventEvaluator
import ch.qos.logback.core.boolex.EventEvaluatorBase
import ch.qos.logback.core.helpers.CyclicBuffer
import ch.qos.logback.core.sift.DefaultDiscriminator
import ch.qos.logback.core.spi.CyclicBufferTracker
import org.slf4j.Marker
import java.util.concurrent.ConcurrentMap


abstract class AlarmAppender(
    private val cyclicBufferSize: Int,
    private val ignoredWarnLogger: Array<String>,
    var encoder: PatternLayoutEncoder? = null,
    private val startedMsg: String = "^Started .*? in .*? seconds \\(.*?\\)$",
    private val cacheMap: ConcurrentMap<String, Int>,
    private val timeoutCacheMap: ConcurrentMap<String, Int>
) : AppenderBase<ILoggingEvent>() {

    companion object {
        const val MAX_DELAY_BETWEEN_STATUS_MESSAGES = 1228800 * CoreConstants.MILLIS_IN_ONE_SECOND
        const val ALARM_LOG_MARKER = "alarm"
        const val NO_ALARM_LOG_MARKER = "no_alarm"
    }


    private var eventEvaluator: EventEvaluator<ILoggingEvent>? = null
    private val discriminator = DefaultDiscriminator<ILoggingEvent>()
    private var cbTracker: CyclicBufferTracker<ILoggingEvent>? = null
    private var sendErrorCount = 0

    private var lastTrackerStatusPrint: Long = 0
    private var delayBetweenStatusMessages = 300 * CoreConstants.MILLIS_IN_ONE_SECOND
    private var errorCount = 0
    private var asynchronousSending = true

    override fun start() {
        val alarmEvaluator = object : EventEvaluatorBase<ILoggingEvent>() {
            override fun evaluate(event: ILoggingEvent): Boolean {
                val loggerName = event.loggerName
                for (l in ignoredWarnLogger) {
                    if (loggerName.startsWith(l)) {
                        return false
                    }
                }
                return (event.level.levelInt >= Level.ERROR_INT || event.marker?.contains(
                    ALARM_LOG_MARKER
                ) == true || event.formattedMessage.matches(Regex(startedMsg))) && (event.marker == null || !event.marker.contains(
                    NO_ALARM_LOG_MARKER
                ))
            }
        }
        alarmEvaluator.context = context
        alarmEvaluator.name = "onAlarm"
        alarmEvaluator.start()
        eventEvaluator = alarmEvaluator
        if (encoder != null) {
            encoder!!.context = context
            encoder!!.start()
        }
        if (cbTracker == null) {
            cbTracker = CyclicBufferTracker()
            cbTracker!!.bufferSize = if (cyclicBufferSize > 0) cyclicBufferSize else 1
        }
        super.start()
    }

    public override fun append(event: ILoggingEvent?) {
        if (event == null || !isStarted) {
            return
        }
        val key = discriminator.getDiscriminatingValue(event)
        val now = System.currentTimeMillis()


        val cb = cbTracker!!.getOrCreate(key, now)
        event.callerData
        event.prepareForDeferredProcessing()
        if (event.marker?.contains(ALARM_LOG_MARKER) == true)
            cb.clear()
        cb.add(event)

        try {
            if (eventEvaluator!!.evaluate(event)) {
                val cbClone = CyclicBuffer(cb)
                cb.clear()
                if (asynchronousSending) {
                    context.scheduledExecutorService.execute(SenderRunnable(cbClone, event))
                } else {
                    sendBuffer(cbClone, event)
                }
            }
        } catch (ex: EvaluationException) {
            errorCount++
            if (errorCount < CoreConstants.MAX_ERROR_COUNT) {
                addError("SlackAppender's EventEvaluator threw an Exception-", ex)
            }
        }
        // immediately remove the buffer if asked by the user
        if (eventMarksEndOfLife(event)) {
            cbTracker!!.endOfLife(key)
        }

        cbTracker!!.removeStaleComponents(now)

        if (lastTrackerStatusPrint + delayBetweenStatusMessages < now) {
            addInfo("SlackAppender [" + name + "] is tracking [" + cbTracker!!.componentCount + "] buffers")
            lastTrackerStatusPrint = now
            // quadruple 'delay' assuming less than max delay
            if (delayBetweenStatusMessages < MAX_DELAY_BETWEEN_STATUS_MESSAGES) {
                delayBetweenStatusMessages *= 4
            }
        }
    }

    private fun findAlarmMarker(marker: Marker?): AlarmMarker? {
        if (marker != null) {
            return if (marker is AlarmMarker) {
                marker
            } else
                marker.iterator().asSequence().findLast { it is AlarmMarker }
                    ?.let { it as AlarmMarker }
        }
        return null
    }

    private fun sendBuffer(cbClone: CyclicBuffer<ILoggingEvent>, event: ILoggingEvent) {
        val message = mutableListOf<String>()
        val len = cbClone.length()
        var initialComment = ""

        val alarmMarker: AlarmMarker? = findAlarmMarker(event.marker)
        for (i in 0 until len) {
            val e = cbClone.get()
            message.add(String(encoder!!.encode(e)))
            if (i == len - 1) {
                val tp = e.throwableProxy
                initialComment = alarmMarker?.initialComment
                    ?: (if (tp != null) "${tp.className}:${tp.message ?: event.message}" else e.formattedMessage
                        ?: event.message)
                            ?: ""
            }
        }

        val timeStamp = event.timeStamp
        val needSend = if (!cacheMap.containsKey(initialComment)) {
            cacheMap[initialComment] = 1
            true
        } else {
            false
        }

        if (needSend) {
            val timeout = alarmMarker?.timeout == true
            if (timeout) {
                if (!timeoutCacheMap.containsKey(initialComment)) {
                    timeoutCacheMap[initialComment] = 1
                    send(timeStamp, initialComment, message, true)
                }
            } else {
                send(timeStamp, initialComment, message, false)
            }
        }
    }

    private fun eventMarksEndOfLife(eventObject: ILoggingEvent): Boolean {
        val marker = eventObject.marker ?: return false

        return marker.contains(ClassicConstants.FINALIZE_SESSION_MARKER)
    }

    private fun send(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        timeout: Boolean
    ) {
        if (sendErrorCount > 0)
            Thread.sleep(2 * 1000L)

        if (sendMessage(timeStamp, initialComment, message, timeout)) {
            sendErrorCount = 0
        } else {
            sendErrorCount++
            if (sendErrorCount > 15) {
                stop()
            }
        }
    }

    abstract fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        timeout: Boolean
    ): Boolean

    internal inner class SenderRunnable(
        private val cyclicBuffer: CyclicBuffer<ILoggingEvent>,
        private val e: ILoggingEvent
    ) : Runnable {
        override fun run() {
            sendBuffer(cyclicBuffer, e)
        }
    }
}