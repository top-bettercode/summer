package top.bettercode.summer.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.logging.LogFile
import org.springframework.boot.logging.LoggingInitializationContext
import org.springframework.boot.logging.logback.LogbackLoggingSystem
import org.springframework.util.Assert
import top.bettercode.summer.logging.LoggingUtil
import top.bettercode.summer.logging.LoggingUtil.existProperty
import top.bettercode.summer.logging.SlackProperties
import top.bettercode.summer.logging.slack.SlackAppender

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
        val environment = initializationContext.environment
        val warnSubject = LoggingUtil.warnSubject(environment)

        val fileLogPattern = environment.getProperty("logging.pattern.file", FILE_LOG_PATTERN)

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
                    val slackAppender = SlackAppender(
                        slackProperties, warnSubject, fileLogPattern
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

    companion object {
        const val FILE_LOG_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} \${LOG_LEVEL_PATTERN:-%5p} \${PID:- } --- [%t] %-40.40logger{39} :%X{id} %m%n\${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"
    }

}
