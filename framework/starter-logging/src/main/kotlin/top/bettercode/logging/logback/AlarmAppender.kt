package top.bettercode.logging.logback

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
import ch.qos.logback.core.util.OptionHelper
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.web.util.WebUtils
import top.bettercode.logging.RequestLoggingFilter
import top.bettercode.logging.formatFileNow
import java.io.File
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit


abstract class AlarmAppender(
    private val cyclicBufferSize: Int,
    private val cacheSeconds: Long,
    private val ignoredWarnLogger: Array<String>,
    protected val logAll: Boolean
) : AppenderBase<ILoggingEvent>() {

    companion object {
        const val MAX_DELAY_BETWEEN_STATUS_MESSAGES = 1228800 * CoreConstants.MILLIS_IN_ONE_SECOND
    }

    private lateinit var cacheMap: ConcurrentMap<String, Int>
    private var eventEvaluator: EventEvaluator<ILoggingEvent>? = null
    private val discriminator = DefaultDiscriminator<ILoggingEvent>()
    private var cbTracker: CyclicBufferTracker<ILoggingEvent>? = null
    private val encoder: PatternLayoutEncoder = PatternLayoutEncoder()
    private var sendErrorCount = 0

    private var lastTrackerStatusPrint: Long = 0
    private var delayBetweenStatusMessages = 300 * CoreConstants.MILLIS_IN_ONE_SECOND
    private var errorCount = 0
    private var asynchronousSending = true

    override fun start() {
        val cache: Cache<String, Int> =
            CacheBuilder.newBuilder().expireAfterWrite(cacheSeconds, TimeUnit.SECONDS)
                .maximumSize(100).build()
        cacheMap = cache.asMap()

        val alarmEvaluator = object : EventEvaluatorBase<ILoggingEvent>() {
            override fun evaluate(event: ILoggingEvent): Boolean {
                val loggerName = event.loggerName
                for (l in ignoredWarnLogger) {
                    if (loggerName.startsWith(l)) {
                        return false
                    }
                }
                return (event.level.levelInt >= Level.ERROR_INT || event.marker?.contains(
                    RequestLoggingFilter.ALARM_LOG_MARKER
                ) == true) && (event.marker == null || !event.marker.contains(RequestLoggingFilter.NO_ALARM_LOG_MARKER))
            }
        }
        alarmEvaluator.context = context
        alarmEvaluator.name = "onAlarm"
        alarmEvaluator.start()
        eventEvaluator = alarmEvaluator
        encoder.pattern = OptionHelper.substVars(Logback2LoggingSystem.FILE_LOG_PATTERN, context)
        encoder.context = context
        encoder.start()
        if (cbTracker == null) {
            cbTracker = CyclicBufferTracker()
            cbTracker!!.bufferSize = if (cyclicBufferSize > 0 && !logAll) cyclicBufferSize else 1
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
        if (event.marker?.contains(RequestLoggingFilter.ALARM_LOG_MARKER) == true)
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

    private fun sendBuffer(cbClone: CyclicBuffer<ILoggingEvent>, event: ILoggingEvent) {
        val message = mutableListOf<String>()
        val len = cbClone.length()
        var initialComment = ""

        for (i in 0 until len) {
            val e = cbClone.get()
            message.add(String(encoder.encode(e)))
            if (i == len - 1) {
                val tp = e.throwableProxy
                initialComment = e.mdcPropertyMap[WebUtils.ERROR_MESSAGE_ATTRIBUTE]
                    ?: if (tp != null) "${tp.className}:${tp.message}" else e.formattedMessage
            }
        }

        val timeStamp = event.timeStamp
        if (cacheSeconds > 0) {
            if (!cacheMap.containsKey(initialComment)) {
                cacheMap[initialComment] = 1
                val timeoutMsg = event.mdcPropertyMap[RequestLoggingFilter.TIMEOUT_MSG]
                if (!timeoutMsg.isNullOrBlank()) {
                    initialComment += timeoutMsg
                }
                send(timeStamp, initialComment, message)
            }
        } else {
            val timeoutMsg = event.mdcPropertyMap[RequestLoggingFilter.TIMEOUT_MSG]
            if (!timeoutMsg.isNullOrBlank()) {
                initialComment += timeoutMsg
            }
            send(timeStamp, initialComment, message)
        }
    }

    private fun eventMarksEndOfLife(eventObject: ILoggingEvent): Boolean {
        val marker = eventObject.marker ?: return false

        return marker.contains(ClassicConstants.FINALIZE_SESSION_MARKER)
    }

    private fun send(timeStamp: Long, initialComment: String, message: List<String>) {
        if (sendErrorCount > 0)
            Thread.sleep(2 * 1000L)

        if (sendMessage(timeStamp, initialComment, message)) {
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
        message: List<String>
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