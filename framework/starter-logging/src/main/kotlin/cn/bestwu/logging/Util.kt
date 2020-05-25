package cn.bestwu.logging

import cn.bestwu.lang.util.LocalDateTimeHelper
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
internal val dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSSZ"
private const val dateFormatFilePattern = "yyyy-MM-dd+HH:mm:ss.SSS"
private val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
private val dateFileFormatter = DateTimeFormatter.ofPattern(dateFormatFilePattern)

internal fun formatNow(): String {
    return LocalDateTime.now().format(dateFormatter)
}

internal fun format(localDateTime: LocalDateTime): String {
    return localDateTime.format(dateFormatter)
}

internal fun format(timeStamp: Long): String {
    return LocalDateTimeHelper.of(timeStamp).format(dateFormatter)
}

internal fun formatFile(timeStamp: Long): String {
    return LocalDateTimeHelper.of(timeStamp).format(dateFileFormatter)
}

internal fun warnSubject(environment: Environment): String = environment.getProperty("logging.warn-subject", "${environment.getProperty("spring.application.name", "")} ${environment.getProperty("spring.profiles.active", "")} system exception")

internal fun existProperty(environment: Environment, key: String) =
        environment.containsProperty(key) && !environment.getProperty(key).isNullOrBlank()