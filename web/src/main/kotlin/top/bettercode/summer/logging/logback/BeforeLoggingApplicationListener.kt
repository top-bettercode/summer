package top.bettercode.summer.logging.logback

import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.boot.context.logging.LoggingApplicationListener
import org.springframework.boot.logging.LoggingSystem
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.GenericApplicationListener
import org.springframework.core.ResolvableType
import top.bettercode.summer.logging.logback.Logback2LoggingSystem

/**
 * @author Peter Wu
 */
class BeforeLoggingApplicationListener : GenericApplicationListener {
    override fun supportsSourceType(sourceType: Class<*>?): Boolean {
        return sourceType?.let { SpringApplication::class.java.isAssignableFrom(it) || ApplicationContext::class.java.isAssignableFrom(it) }
                ?: false
    }

    override fun supportsEventType(eventType: ResolvableType): Boolean {
        return eventType.rawClass?.let { ApplicationStartingEvent::class.java.isAssignableFrom(it) } ?: false
    }

    override fun getOrder(): Int {
        return LoggingApplicationListener.DEFAULT_ORDER - 1
    }

    override fun onApplicationEvent(event: ApplicationEvent) {
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, Logback2LoggingSystem::class.java.name)
    }
}
