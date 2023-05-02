package top.bettercode.summer.test

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.junit.jupiter.api.AfterAll
import org.slf4j.LoggerFactory

abstract class BaseLogTest {
    companion object {
        @JvmStatic
        @AfterAll
        fun logAfterAll() {
            (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger(
                    "org.hibernate.SQL").level = Level.OFF
        }
    }
}
