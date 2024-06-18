package top.bettercode.summer.gradle.plugin.project

import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLoggerContext
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.client.ApiTemplate

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class JenkinsTest {
    private val jenkins: Jenkins = Jenkins("", "")
    private val log: Logger = LoggerFactory.getLogger(ApiTemplate::class.java)

    init {
        val field = OutputEventListenerBackedLogger::class.java.getDeclaredField("context")
        field.isAccessible = true
        val loggerContext = field[log] as OutputEventListenerBackedLoggerContext
        loggerContext.level = LogLevel.INFO
    }

    @Test
    fun config() {
        val config = jenkins.config("物流报价平台-auth-认证-test")
        System.err.println(config)
    }

    @Test
    fun changeBranch() {
        jenkins.changeBranch("物流报价平台-auth-认证-test", "v1.2")
    }

    @Test
    fun description() {
        val description = jenkins.description("运营后台接口")
        System.err.println(description)
    }

    @Disabled
    @Test
    fun build() {
        jenkins.build("test-futures-front")
    }

    @Test
    fun buildInfo() {
        jenkins.buildInfo("test-futures-front")
    }
}