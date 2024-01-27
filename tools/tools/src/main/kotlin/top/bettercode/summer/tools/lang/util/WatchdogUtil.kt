package top.bettercode.summer.tools.lang.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
object WatchdogUtil {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun schedule(runnable: Runnable) {
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

        scheduler.scheduleAtFixedRate(runnable, initialDelay, 24 * 60, TimeUnit.MINUTES)
    }
}