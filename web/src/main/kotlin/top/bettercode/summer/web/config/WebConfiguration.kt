package top.bettercode.summer.web.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * @author Peter Wu
 */
@EnableConfigurationProperties(JacksonExtProperties::class, SummerWebProperties::class)
@Configuration(proxyBeanMethods = false)
class WebConfiguration {
    private val log: Logger = LoggerFactory.getLogger(WebConfiguration::class.java)

    init {
        try {
            TimeUtil.checkTime()

            // 创建一个 ScheduledExecutorService
            val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

            // 获取当前时间
            val currentTime = LocalDateTime.now()
            // 设置每天的9点
            val scheduledTime = LocalTime.of(9, 0)

            // 计算距离下一次执行的时间
            val localDate = currentTime.toLocalDate()
            val executionTime = LocalDateTime.of(localDate, scheduledTime)

            val nextExecutionTime = if (currentTime.isBefore(executionTime)) {
                executionTime
            } else {
                executionTime.plusDays(1)
            }

            val initialDelay = Duration.between(currentTime, nextExecutionTime).toMinutes()

            scheduler.scheduleAtFixedRate({
                TimeUtil.checkTime()
            }, initialDelay, 24 * 60, TimeUnit.MINUTES)
        } catch (e: Exception) {
            log.error("check time error", e)
        }
    }

    @ConditionalOnMissingBean(IFormkeyService::class)
    @Bean
    fun formkeyService(summerWebProperties: SummerWebProperties): IFormkeyService {
        return FormkeyService(summerWebProperties.formKeyTtl)
    }

    @Bean
    fun packageScanClassResolver(applicationContext: ApplicationContext): PackageScanClassResolver {
        return PackageScanClassResolver(applicationContext.classLoader)
    }

}
