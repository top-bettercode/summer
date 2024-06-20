package top.bettercode.summer.logging.logback

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.boolex.OnMarkerEvaluator
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.classic.net.SMTPAppender
import ch.qos.logback.classic.net.SSLSocketAppender
import ch.qos.logback.classic.net.SocketAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.boolex.EventEvaluatorBase
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.rolling.StartAndSizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.spi.ContextAware
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.spi.LifeCycle
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.util.OptionHelper
import com.github.benmanes.caffeine.cache.Caffeine
import net.logstash.logback.appender.LogstashTcpSocketAppender
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.logging.LogFile
import org.springframework.boot.logging.LoggingInitializationContext
import org.springframework.boot.logging.LoggingSystem
import org.springframework.boot.logging.logback.LogbackLoggingSystem
import org.springframework.core.env.Environment
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import top.bettercode.summer.logging.*
import top.bettercode.summer.logging.LoggingUtil.existProperty
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.logging.slack.SlackAppender
import top.bettercode.summer.logging.websocket.WebSocketAppender
import top.bettercode.summer.tools.lang.PrettyMessageHTMLLayout
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.io.File
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 自定义 LogbackLoggingSystem
 * @author Peter Wu
 * @since 0.0.1
 */
open class Logback2LoggingSystem(classLoader: ClassLoader) : LogbackLoggingSystem(classLoader) {

    private val log: Logger = LoggerFactory.getLogger(Logback2LoggingSystem::class.java)
    private val loggerContext: LoggerContext by lazy {
        val factory = LoggerFactory.getILoggerFactory()
        Assert.isInstanceOf(
            LoggerContext::class.java, factory
        ) {
            String.format(
                "LoggerFactory is not a Logback LoggerContext but Logback is on the classpath. Either remove Logback or the competing implementation (%s loaded from %s). If you are using WebLogic you will need to add 'org.slf4j' to prefer-application-packages in WEB-INF/weblogic.xml",
                factory.javaClass,
                getLocation(factory)
            )
        }
        factory as LoggerContext
    }

    override fun reinitialize(initializationContext: LoggingInitializationContext?) {
        super.reinitialize(initializationContext)
        if (initializationContext != null) {
            loadDefaults(initializationContext, null)
        }
    }

    override fun loadDefaults(
        initializationContext: LoggingInitializationContext, logFile: LogFile?
    ) {
        super.loadDefaults(initializationContext, null)
        val context = loggerContext
        context.getLogger("org.jboss").level = Level.WARN
        context.getLogger("org.hibernate").level = Level.WARN

        start(context, sqlFilter)

        val environment = initializationContext.environment
        val warnSubject = LoggingUtil.warnSubject(environment)
        //smtp log
        if (existProperty(environment, "summer.logging.smtp.host")) {
            synchronized(context.configurationLock) {
                val smtpProperties = Binder.get(environment).bind(
                    "summer.logging.smtp", SmtpProperties::class.java
                ).get()
                val levelMailAppender = mailAppender(
                    context,
                    smtpProperties,
                    warnSubject
                )
                val mailMarker = smtpProperties.marker
                val markerMailAppender = if (!mailMarker.isNullOrBlank()) mailAppender(
                    context,
                    smtpProperties,
                    warnSubject,
                    mailMarker
                )
                else null
                smtpProperties.logger.map { loggerName -> context.getLogger(loggerName.trim()) }
                    .forEach {
                        it.addAppender(levelMailAppender)
                        if (markerMailAppender != null) it.addAppender(markerMailAppender)
                    }
            }
        }

        val filesProperties =
            if (existProperty(environment, "summer.logging.files.path")) Binder.get(environment)
                .bind(
                    "summer.logging.files", FilesProperties::class.java
                ).get() else FilesProperties()

        val fileLogPattern = environment.getProperty("logging.pattern.file", LOG_PATTERN)
        //sql log
        val sqlAppender = SqlAppender()
        sqlAppender.context = context
        sqlAppender.start()
        arrayOf(
            "org.hibernate.SQL",
            "org.hibernate.type.descriptor.sql.BasicBinder",
            "top.bettercode.summer.SQL"
        ).map { context.getLogger(it) }
            .forEach {
                it.addAppender(sqlAppender)
            }

        //slack log
        if (existProperty(environment, "summer.logging.slack.auth-token") && existProperty(
                environment, "summer.logging.slack.channel"
            )
        ) {
            synchronized(context.configurationLock) {
                val slackProperties = Binder.get(environment).bind(
                    "summer.logging.slack", SlackProperties::class.java
                ).get()
                try {
                    var logsViewPath = environment.getProperty("summer.logging.files.view-path")
                    val logsPath = environment.getProperty("summer.logging.files.path")
                        ?: logsViewPath ?: "logs"
                    logsViewPath = logsViewPath ?: logsPath

                    val managementPath =
                        environment.getProperty("management.endpoints.web.base-path")
                            ?: "/actuator"

                    val cacheMap = Caffeine.newBuilder()
                        .expireAfterWrite(slackProperties.cacheSeconds, TimeUnit.SECONDS)
                        .maximumSize(1000).build<String, Int>().asMap()
                    val timeoutCacheMap =
                        Caffeine.newBuilder()
                            .expireAfterWrite(slackProperties.timeoutCacheSeconds, TimeUnit.SECONDS)
                            .maximumSize(1000).build<String, Int>().asMap()
                    val slackAppender = SlackAppender(
                        slackProperties,
                        warnSubject,
                        logsPath,
                        "$managementPath/logs${logsPath.substringAfter(logsViewPath)}",
                        fileLogPattern,
                        cacheMap,
                        timeoutCacheMap
                    )
                    slackAppender.context = context
                    slackAppender.start()
                    slackProperties.logger.map { loggerName -> context.getLogger(loggerName.trim()) }
                        .forEach {
                            it.addAppender(slackAppender)
                        }
                } catch (e: Exception) {
                    log.error("配置SlackAppender失败", e)
                }
            }
        }

        val rootLevel = environment.getProperty("logging.level.root")
        //websocket log
        if (ClassUtils.isPresent(
                "org.springframework.web.socket.server.standard.ServerEndpointExporter",
                Logback2LoggingSystem::class.java.classLoader
            ) && ("true" == environment.getProperty("summer.logging.websocket.enabled") || environment.getProperty(
                "summer.logging.websocket.enabled"
            ).isNullOrBlank())
        ) {
            synchronized(context.configurationLock) {
                try {
                    val logger = context.getLogger(LoggingSystem.ROOT_LOGGER_NAME)
                    if (rootLevel != null) {
                        logger.level = Level.toLevel(rootLevel)
                    }
                    val appender = WebSocketAppender().apply { addFilter(sqlFilter) }
                    start(context, appender)
                    val asyncAppender = AsyncAppender()
                    asyncAppender.context = context
                    asyncAppender.addAppender(appender)
                    asyncAppender.start()
                    logger.addAppender(asyncAppender)
                } catch (e: Exception) {
                    log.error("配置Websocket失败", e)
                }
            }
        }

        //socket log
        if (existProperty(environment, "summer.logging.socket.remote-host")) {
            synchronized(context.configurationLock) {
                val socketProperties = Binder.get(environment).bind(
                    "summer.logging.socket",
                    SocketLoggingProperties::class.java
                ).get()
                val socketAppender = if (socketProperties.ssl == null) socketAppender(
                    context, socketProperties
                ) else sslSocketAppender(context, socketProperties)
                socketAppender.start()
                val asyncAppender = AsyncAppender()
                asyncAppender.context = context
                asyncAppender.addAppender(socketAppender)
                asyncAppender.start()
                context.getLogger("root").addAppender(asyncAppender)
            }
        }

        //logstashTcpSocketAppender
        if (existProperty(environment, "summer.logging.logstash.destinations[0]")) {
            synchronized(context.configurationLock) {
                val socketProperties = Binder.get(environment).bind(
                    "summer.logging.logstash", LogstashTcpSocketProperties::class.java
                ).get()
                val socketAppender = logstashTcpSocketAppender(
                    context,
                    socketProperties
                )
                socketAppender.start()
                context.getLogger("root").addAppender(socketAppender)
            }
        }

        //console log
        val consoleAppender = ConsoleAppender<ILoggingEvent>()
        consoleAppender.context = context
        consoleAppender.name = "CONSOLE"
        val encoder = PatternLayoutEncoder()
        encoder.charset = Charset.forName("utf-8")
        encoder.pattern =
            OptionHelper.substVars(
                environment.getProperty("logging.pattern.console", LOG_PATTERN),
                context
            )
        start(context, encoder)
        consoleAppender.encoder = encoder
        consoleAppender.addFilter(sqlFilter)
        consoleAppender.start()
        start(context, consoleAppender)
        val logger = context.getLogger(LoggingSystem.ROOT_LOGGER_NAME)
        if (rootLevel != null) {
            logger.level = Level.toLevel(rootLevel)
        }
        logger.detachAppender("CONSOLE")
        logger.addAppender(consoleAppender)

        //file log
        if (existProperty(environment, "summer.logging.files.path")) {

            var defaultPackage = environment.getProperty("summer.logging.spilt-markers.package")
            if (defaultPackage == null) {
                var command = environment.getProperty("sun.java.command")
                if (command?.contains("Gradle Test") == true) {
                    command = null
                }
                defaultPackage = command?.substringBeforeLast('.')
            }
            val spilts = bind(environment, "summer.logging.spilt")
            val defaultLevel = rootLevel ?: "debug"
            val markers =
                defaultSpiltMarkers(defaultPackage).associateWith { defaultLevel }.toMutableMap()
            markers[HttpOperation.REQUEST_LOG_MARKER] = defaultLevel
            markers.putAll(bind(environment, "summer.logging.spilt-marker"))

            val levels = Binder.get(environment)
                .bind("summer.logging.spilt-level", Bindable.setOf(String::class.java))
                .orElseGet { setOf() }

            val rootName = LoggingSystem.ROOT_LOGGER_NAME.lowercase(Locale.getDefault())
            spilts.remove(rootName)

            setAllFileAppender(context, fileLogPattern, filesProperties, rootLevel, logFile)

//            setRootFileAppender( context, fileLogPattern, filesProperties, rootLevel, spilts.keys, markers.keys, levels )

            for ((key, value) in markers) {
                setMarkerFileAppender(context, fileLogPattern, filesProperties, key, value)
            }

            for ((key, value) in spilts) {
                setSpiltFileAppender(context, fileLogPattern, filesProperties, key, value)
            }

            for (level in levels) {
                setLevelFileAppender(context, fileLogPattern, filesProperties, level)
            }
        }
    }


    private fun bind(environment: Environment, key: String): MutableMap<String, String> {
        val bindable = Bindable.mapOf(String::class.java, String::class.java)
        val binder = Binder.get(environment)
        return binder.bind(key, bindable).orElseGet { mutableMapOf() }
    }

    private fun setAllFileAppender(
        context: LoggerContext,
        fileLogPattern: String,
        filesProperties: FilesProperties,
        rootLevel: String?,
        logFile: LogFile?
    ) {
        val appender = RollingFileAppender<ILoggingEvent>()
        val encoder = PatternLayoutEncoder()
        encoder.charset = Charset.forName("utf-8")
        encoder.pattern = OptionHelper.substVars(fileLogPattern, context)
        appender.encoder = encoder
        start(context, encoder)

        val logFilePath = logFile?.toString() ?: (filesProperties.path + File.separator + "all")
        appender.file = "$logFilePath.log"
        setRollingPolicy(appender, context, filesProperties, logFilePath)


        val filter = object : AbstractMatcherFilter<ILoggingEvent>() {

            override fun decide(event: ILoggingEvent): FilterReply {
                if (!isStarted) {
                    return FilterReply.NEUTRAL
                }

                val eventMarker = event.marker
                if (eventMarker != null) {
                    if (eventMarker.contains(RequestLoggingFilter.NOT_IN_ALL)) {
                        return onMismatch
                    }
                }

                return onMatch
            }
        }
        filter.onMismatch = FilterReply.DENY
        start(context, filter)
        appender.addFilter(filter)
        appender.addFilter(sqlFilter)

        start(context, appender)
        synchronized(context.configurationLock) {
            val logger = context.getLogger(LoggingSystem.ROOT_LOGGER_NAME)
            if (rootLevel != null) {
                logger.level = Level.toLevel(rootLevel)
            }
            val asyncAppender = AsyncAppender()
            asyncAppender.context = context
            asyncAppender.addAppender(appender)
            asyncAppender.start()
            logger.addAppender(asyncAppender)
        }
    }

    private fun setRootFileAppender(
        context: LoggerContext,
        fileLogPattern: String,
        filesProperties: FilesProperties,
        rootLevel: String?,
        spilts: Set<String>,
        markers: Set<String>,
        levels: Set<String>
    ) {

        val appender = RollingFileAppender<ILoggingEvent>()
        val encoder = PatternLayoutEncoder()
        encoder.charset = Charset.forName("utf-8")
        encoder.pattern = OptionHelper.substVars(fileLogPattern, context)
        appender.encoder = encoder
        start(context, encoder)

        val name = LoggingSystem.ROOT_LOGGER_NAME.lowercase(Locale.getDefault())
        val logFile = (filesProperties.path + File.separator + "root" + File.separator + name)
        appender.file = "$logFile.log"
        setRollingPolicy(appender, context, filesProperties, logFile)

        val filter = object : AbstractMatcherFilter<ILoggingEvent>() {

            override fun decide(event: ILoggingEvent): FilterReply {
                if (!isStarted) {
                    return FilterReply.NEUTRAL
                }
                val loggerName = event.loggerName
                for (it in spilts) {
                    if (loggerName.startsWith(it)) {
                        return onMismatch
                    }
                }

                val eventMarker = event.marker
                if (eventMarker != null) {
                    for (marker in markers) {
                        if (eventMarker.contains(marker)) {
                            return onMismatch
                        }
                    }
                }

                for (level in levels) {
                    if (event.level == Level.valueOf(level)) {
                        return onMismatch
                    }
                }

                return FilterReply.NEUTRAL
            }
        }
        filter.onMismatch = FilterReply.DENY
        start(context, filter)
        appender.addFilter(filter)
        appender.addFilter(sqlFilter)

        start(context, appender)

        synchronized(context.configurationLock) {
            val logger = context.getLogger(LoggingSystem.ROOT_LOGGER_NAME)
            if (rootLevel != null) {
                logger.level = Level.toLevel(rootLevel)
            }
            val asyncAppender = AsyncAppender()
            asyncAppender.context = context
            asyncAppender.addAppender(appender)
            asyncAppender.start()
            logger.addAppender(asyncAppender)
        }
    }

    private fun setLevelFileAppender(
        context: LoggerContext,
        fileLogPattern: String,
        filesProperties: FilesProperties,
        level: String
    ) {

        val appender = RollingFileAppender<ILoggingEvent>()
        val encoder = PatternLayoutEncoder()
        encoder.charset = Charset.forName("utf-8")
        encoder.pattern = OptionHelper.substVars(fileLogPattern, context)
        appender.encoder = encoder
        start(context, encoder)

        val logFile = filesProperties.path + File.separator + "level" + File.separator + level
        appender.file = "$logFile.log"
        setRollingPolicy(appender, context, filesProperties, logFile)

        val filter = LevelFilter()
        filter.setLevel(Level.toLevel(level))
        filter.onMatch = FilterReply.NEUTRAL
        filter.onMismatch = FilterReply.DENY
        start(context, filter)
        appender.addFilter(filter)
        appender.addFilter(sqlFilter)

        start(context, appender)

        synchronized(context.configurationLock) {
            val logger = context.getLogger(LoggingSystem.ROOT_LOGGER_NAME)
            val asyncAppender = AsyncAppender()
            asyncAppender.context = context
            asyncAppender.addAppender(appender)
            asyncAppender.start()
            logger.addAppender(asyncAppender)
        }
    }

    private fun setMarkerFileAppender(
        context: LoggerContext,
        fileLogPattern: String,
        filesProperties: FilesProperties,
        marker: String,
        level: String
    ) {

        val appender = RollingFileAppender<ILoggingEvent>()
        val encoder = PatternLayoutEncoder()
        encoder.charset = Charset.forName("utf-8")
        encoder.pattern = OptionHelper.substVars(fileLogPattern, context)
        appender.encoder = encoder
        start(context, encoder)

        val logFile = filesProperties.path + File.separator + marker + File.separator + marker
        appender.file = "$logFile.log"
        setRollingPolicy(appender, context, filesProperties, logFile)

        val filter = object : AbstractMatcherFilter<ILoggingEvent>() {

            override fun decide(event: ILoggingEvent): FilterReply {
                if (!isStarted) {
                    return FilterReply.NEUTRAL
                }
                val eventMarker = event.marker ?: return onMismatch

                return if (eventMarker.contains(marker)) {
                    FilterReply.NEUTRAL
                } else {
                    onMismatch
                }
            }
        }
        filter.onMismatch = FilterReply.DENY
        start(context, filter)
        appender.addFilter(filter)
        appender.addFilter(sqlFilter)

        start(context, appender)

        synchronized(context.configurationLock) {
            val logger = context.getLogger(LoggingSystem.ROOT_LOGGER_NAME)
            logger.level = Level.toLevel(level)
            val asyncAppender = AsyncAppender()
            asyncAppender.context = context
            asyncAppender.addAppender(appender)
            asyncAppender.start()
            logger.addAppender(asyncAppender)
        }
    }

    private fun setSpiltFileAppender(
        context: LoggerContext,
        fileLogPattern: String,
        filesProperties: FilesProperties,
        name: String,
        level: String
    ) {
        val appender = RollingFileAppender<ILoggingEvent>()
        val encoder = PatternLayoutEncoder()
        encoder.charset = Charset.forName("utf-8")
        encoder.pattern = OptionHelper.substVars(fileLogPattern, context)
        appender.encoder = encoder
        appender.addFilter(sqlFilter)
        start(context, encoder)

        val logFile = filesProperties.path + File.separator + "spilt" + File.separator + name
        appender.file = "$logFile.log"
        setRollingPolicy(appender, context, filesProperties, logFile)

        start(context, appender)

        synchronized(context.configurationLock) {
            val logger = context.getLogger(name)
            logger.level = Level.toLevel(level)
            val asyncAppender = AsyncAppender()
            asyncAppender.context = context
            asyncAppender.addAppender(appender)
            asyncAppender.start()
            logger.addAppender(asyncAppender)
        }
    }

    private fun setRollingPolicy(
        appender: RollingFileAppender<ILoggingEvent>,
        context: LoggerContext,
        filesProperties: FilesProperties,
        logFile: String
    ) {
        if (filesProperties.isRolloverOnStart) appender.rollingPolicy =
            StartAndSizeAndTimeBasedRollingPolicy<ILoggingEvent>().apply {
                fileNamePattern = "$logFile-%d{yyyy-MM-dd}-%i.gz"
                maxFileSize = FileSize.valueOf(filesProperties.maxFileSize)
                maxHistory = filesProperties.maxHistory
                setParent(appender)
                start(context, this)
            }
        else appender.rollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>().apply {
            fileNamePattern = "$logFile-%d{yyyy-MM-dd}-%i.gz"
            setMaxFileSize(FileSize.valueOf(filesProperties.maxFileSize))
            maxHistory = filesProperties.maxHistory
            setParent(appender)
            start(context, this)
        }
    }


    /**
     * 发送邮件
     */
    private fun mailAppender(
        context: LoggerContext,
        smtpProperties: SmtpProperties,
        warnSubject: String,
        mailMarker: String? = null
    ): Appender<ILoggingEvent> {
        val appender = SMTPAppender()
        with(appender) {
            this.context = context
            name = "mail"

            setSMTPHost(smtpProperties.host)
            localhost = smtpProperties.localhost
            jndiLocation = smtpProperties.jndiLocation
            setSMTPPort(smtpProperties.port)
            username = smtpProperties.username
            password = smtpProperties.password
            from = smtpProperties.from
            addTo(smtpProperties.to)
            isAsynchronousSending = smtpProperties.isAsynchronousSending
            isIncludeCallerData = smtpProperties.isIncludeCallerData
            isSTARTTLS = smtpProperties.isStarttls
            isSSL = smtpProperties.isSsl
            isSessionViaJNDI = smtpProperties.isSessionViaJNDI
            charsetEncoding = smtpProperties.charsetEncoding
            subject = warnSubject

            val htmlLayout = PrettyMessageHTMLLayout()
            start(context, htmlLayout)
            layout = htmlLayout

            if (mailMarker.isNullOrBlank()) {
                val level = smtpProperties.filter
                if (!level.equals("ERROR", true)) {
                    val filter = object : EventEvaluatorBase<ILoggingEvent>() {
                        override fun evaluate(event: ILoggingEvent): Boolean {
                            return event.level.isGreaterOrEqual(Level.valueOf(level))
                        }
                    }
                    start(context, filter)
                    setEvaluator(filter)
                }
            } else {//marker 过滤
                val filter = OnMarkerEvaluator()
                filter.addMarker(mailMarker)
                start(context, filter)
                setEvaluator(filter)
            }
            addFilter(sqlFilter)

            start()
        }
        return appender
    }

    /**
     * 发送Socket
     */
    private fun socketAppender(
        context: LoggerContext,
        socketProperties: SocketLoggingProperties
    ): Appender<ILoggingEvent> {
        val appender = SocketAppender()
        with(appender) {
            this.context = context
            name = "socket"
            setIncludeCallerData(socketProperties.isIncludeCallerData)
            port = socketProperties.port
            reconnectionDelay =
                ch.qos.logback.core.util.Duration(socketProperties.reconnectionDelay.toMillis())
            queueSize = socketProperties.queueSize
            eventDelayLimit =
                ch.qos.logback.core.util.Duration(socketProperties.eventDelayLimit.toMillis())
            remoteHost = socketProperties.remoteHost
            addFilter(sqlFilter)
            start()
        }
        return appender
    }

    /**
     * 发送SSLSocket
     */
    private fun sslSocketAppender(
        context: LoggerContext,
        socketProperties: SocketLoggingProperties
    ): Appender<ILoggingEvent> {
        val appender = SSLSocketAppender()
        with(appender) {
            this.context = context
            name = "socket"
            setIncludeCallerData(socketProperties.isIncludeCallerData)
            port = socketProperties.port
            reconnectionDelay =
                ch.qos.logback.core.util.Duration(socketProperties.reconnectionDelay.toMillis())
            queueSize = socketProperties.queueSize
            eventDelayLimit =
                ch.qos.logback.core.util.Duration(socketProperties.eventDelayLimit.toMillis())
            remoteHost = socketProperties.remoteHost
            ssl = socketProperties.ssl
            addFilter(sqlFilter)
            start()
        }
        return appender
    }

    /**
     * LogstashTcpSocketAppender
     */
    private fun logstashTcpSocketAppender(
        context: LoggerContext, socketProperties: LogstashTcpSocketProperties
    ): Appender<ILoggingEvent> {
        val appender = LogstashTcpSocketAppender()
        with(appender) {
            this.context = context
            name = "logstashTcpSocket"
            isIncludeCallerData = socketProperties.isIncludeCallerData
            reconnectionDelay = socketProperties.reconnectionDelay
            ringBufferSize = socketProperties.ringBufferSize
            socketProperties.destinations?.forEach { addDestination(it) }
            writeBufferSize = socketProperties.writeBufferSize
            encoder = socketProperties.encoderClass.getDeclaredConstructor().newInstance()
            keepAliveDuration = socketProperties.keepAliveDuration
            addFilter(sqlFilter)
            start()
        }
        return appender
    }

    private fun start(context: LoggerContext, lifeCycle: LifeCycle) {
        if (lifeCycle is ContextAware) {
            (lifeCycle as ContextAware).context = context
        }
        lifeCycle.start()
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

    private val sqlFilter = object : AbstractMatcherFilter<ILoggingEvent>() {

        override fun decide(event: ILoggingEvent): FilterReply {
            if (!isStarted) {
                return FilterReply.NEUTRAL
            }
            if (arrayOf(
                    "org.hibernate.SQL",
                    "org.hibernate.type.descriptor.sql.BasicBinder",
                    "top.bettercode.summer.SQL"
                ).contains(event.loggerName)
            ) {
                return onMismatch
            }

            return onMatch
        }

        init {
            onMismatch = FilterReply.DENY
        }
    }


    companion object {
        const val LOG_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} \${LOG_LEVEL_PATTERN:%5p} \${PID: } --- [%-6.6t] %-40.40logger{39} %20file:%-3line : %m%n\${LOG_EXCEPTION_CONVERSION_WORD:%wEx}"
        private val packageScanClassResolver = PackageScanClassResolver()
        fun defaultSpiltMarkers(defaultPackageName: String? = null): List<String> {
            var packageNames = arrayOf("top.bettercode.summer")
            if (defaultPackageName != null) {
                packageNames += defaultPackageName
            }
            val clazzs: Set<Class<*>> = packageScanClassResolver.findByFilter(
                { type ->
                    try {
                        type.isAnnotationPresent(LogMarker::class.java)
                    } catch (e: Exception) {
                        false
                    }
                }, *packageNames
            )
            return clazzs.map { it.getAnnotation(LogMarker::class.java).value }
        }
    }

}
