package top.bettercode.logging

import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.boot.context.logging.LoggingApplicationListener
import org.springframework.boot.logging.LoggingSystem
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.GenericApplicationListener
import org.springframework.core.ResolvableType
import top.bettercode.logging.logback.Logback2LoggingSystem

/**
 * 用于配置日志系统的 ApplicationListener
 *
 * @author Peter Wu
 * @since 0.0.1
 */
class BeforeLoggingApplicationListener : GenericApplicationListener {
    override fun supportsSourceType(sourceType: Class<*>?): Boolean {
        return isAssignableFrom(sourceType, SpringApplication::class.java, ApplicationContext::class.java)
    }

    override fun onApplicationEvent(event: ApplicationEvent) {
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, Logback2LoggingSystem::class.java.name)
    }

    override fun getOrder(): Int {
        return LoggingApplicationListener.DEFAULT_ORDER - 1
    }

    override fun supportsEventType(eventType: ResolvableType): Boolean {
        return isAssignableFrom(eventType.rawClass, ApplicationStartingEvent::class.java)
    }

    private fun isAssignableFrom(type: Class<*>?, vararg supportedTypes: Class<*>): Boolean {
        if (type != null) {
            return !supportedTypes.none { it.isAssignableFrom(type) }
        }
        return false
    }
}