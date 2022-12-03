package top.bettercode.summer.logging.logback

import org.springframework.boot.logging.LoggingSystem
import org.springframework.boot.logging.LoggingSystemFactory
import org.springframework.util.ClassUtils

class Logback2LoggingSystemFactory : LoggingSystemFactory {
    override fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem? {
        return if (PRESENT) {
            Logback2LoggingSystem(classLoader)
        } else null
    }

    companion object {
        private val PRESENT = ClassUtils.isPresent(
            "ch.qos.logback.classic.LoggerContext",
            Logback2LoggingSystemFactory::class.java.classLoader
        )
    }
}