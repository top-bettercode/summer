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
import ch.qos.logback.core.util.OptionHelper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import top.bettercode.summer.tools.lang.PrettyMessageHTMLLayout
import top.bettercode.summer.tools.lang.util.IPAddressUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import javax.management.ObjectName
import javax.management.Query


abstract class AlarmAppender<T : AlarmProperties>(
    var properties: T,
) : AppenderBase<ILoggingEvent>() {

    companion object {
        const val MAX_DELAY_BETWEEN_STATUS_MESSAGES = 1228800 * CoreConstants.MILLIS_IN_ONE_SECOND

        fun getServerAddress(): String {
            val host = IPAddressUtil.inet4Address
            var port = ""
            try {
                val beanServer = ManagementFactory.getPlatformMBeanServer()
                val objectNames = beanServer.queryNames(
                    ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1"))
                )
                port = ":" + objectNames.iterator().next().getKeyProperty("port")
            } catch (ignored: Exception) {
            }
            return "http://$host$port"
        }

    }

    private val log = LoggerFactory.getLogger(AlarmAppender::class.java)
    private var eventEvaluator: EventEvaluator<ILoggingEvent>? = null
    private val discriminator = DefaultDiscriminator<ILoggingEvent>()
    private var cbTracker: CyclicBufferTracker<ILoggingEvent>? = null
    private var sendErrorCount = 0

    private var lastTrackerStatusPrint: Long = 0
    private var delayBetweenStatusMessages = 300 * CoreConstants.MILLIS_IN_ONE_SECOND
    private var errorCount = 0
    private var asynchronousSending = true
    private val encoder: PatternLayoutEncoder by lazy {
        PatternLayoutEncoder().apply {
            pattern = OptionHelper.substVars(properties.logPattern, context)
        }
    }
    private val cache: Cache<String, Int> by lazy {
        Caffeine.newBuilder()
            .expireAfterWrite(properties.cacheSeconds, TimeUnit.SECONDS)
            .maximumSize(100).build()
    }
    private val timeoutCache: Cache<String, Int> by lazy {
        Caffeine.newBuilder()
            .expireAfterWrite(properties.timeoutCacheSeconds, TimeUnit.SECONDS)
            .maximumSize(100).build()
    }

    override fun start() {
        val alarmEvaluator = object : EventEvaluatorBase<ILoggingEvent>() {
            override fun evaluate(event: ILoggingEvent): Boolean {
                val loggerName = event.loggerName
                for (l in properties.ignoredWarnLogger) {
                    if (loggerName.startsWith(l)) {
                        return false
                    }
                }
                return (event.level.levelInt >= Level.ERROR_INT || event.marker?.contains(
                    AlarmMarker.ALARM_LOG_MARKER
                ) == true || event.formattedMessage.matches(Regex(properties.startedMsg))) && (event.marker == null || !event.marker.contains(
                    AlarmMarker.NO_ALARM_LOG_MARKER
                ))
            }
        }
        alarmEvaluator.context = context
        alarmEvaluator.name = "onAlarm"
        alarmEvaluator.start()
        eventEvaluator = alarmEvaluator
        encoder.context = context
        encoder.start()
        if (cbTracker == null) {
            cbTracker = CyclicBufferTracker()
            cbTracker!!.bufferSize =
                if (properties.cyclicBufferSize > 0) properties.cyclicBufferSize else 1
        }
        super.start()
    }

    override fun append(event: ILoggingEvent?) {
        if (event == null || !isStarted) {
            return
        }
        val key = discriminator.getDiscriminatingValue(event)
        val now = System.currentTimeMillis()


        val cb = cbTracker!!.getOrCreate(key, now)
        event.callerData
        event.prepareForDeferredProcessing()
        if (event.marker?.contains(AlarmMarker.ALARM_LOG_MARKER) == true)
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
            message.add(String(encoder.encode(e)))
            if (i == len - 1) {
                val tp = e.throwableProxy
                initialComment = alarmMarker?.message
                    ?: (if (tp != null) "${tp.className}:${
                        event.formattedMessage ?: tp.message.substringAfter(
                            "${tp.className}: "
                        )
                    }" else event.formattedMessage) ?: ""
            }
        }

        val timeStamp = event.timeStamp
        val needSend = if (cache.getIfPresent(initialComment) == null) {
            cache.put(initialComment, 1)
            true
        } else {
            false
        }

        if (needSend) {
            val level = alarmMarker?.level ?: event.level
            val timeout = alarmMarker?.timeout ?: false
            if (timeout) {
                if (timeoutCache.getIfPresent(initialComment) == null) {
                    timeoutCache.put(initialComment, 1)
                    send(timeStamp, initialComment, message, level, true)
                }
            } else {
                send(timeStamp, initialComment, message, level, false)
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
        level: Level,
        timeout: Boolean
    ) {
        if (sendErrorCount > 0)
            Thread.sleep(2 * 1000L)

        try {
            if (sendMessage(timeStamp, initialComment, message, level, timeout)) {
                sendErrorCount = 0
            } else {
                sendErrorCount++
                if (sendErrorCount > 15) {
                    stop()
                }
            }
        } catch (e: Exception) {
            log.error(
                AlarmMarker.noAlarmMarker,
                "发送信息失败",
                e
            )
        }
    }

    protected fun logUrl(
        actuatorAddress: String,
        message: List<String>
    ): Pair<String, String> {
        val anchor = PrettyMessageHTMLLayout.anchor(message.last())
        val path = File(properties.logsPath)
        val namePattern = "all-${TimeUtil.now().format("yyyy-MM-dd")}-"
        val files =
            path.listFiles { file -> file.name.startsWith(namePattern) && file.extension == "gz" }
        files?.sortBy { -it.lastModified() }
        val existFilename = files?.firstOrNull()?.nameWithoutExtension

        val filename = "$namePattern${
            if (existFilename != null) {
                existFilename.substringAfter(namePattern).toInt() + 1
            } else {
                0
            }
        }"

        val linkTitle = "${filename}.gz#$anchor"
        val logUrl = actuatorAddress + properties.managementLogPath
        return Pair(logUrl, linkTitle)
    }

    fun isPortConnectable(): Boolean {
        return IPAddressUtil.isPortConnectable(
            properties.managementHostName.ifBlank { IPAddressUtil.inet4Address },
            properties.managementPort
        )
    }

    abstract fun sendMessage(
        timeStamp: Long,
        initialComment: String,
        message: List<String>,
        level: Level,
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