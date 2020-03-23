package cn.bestwu.logging

import org.springframework.core.env.Environment
import java.text.SimpleDateFormat

/**
 *
 * @author Peter Wu
 */
internal const val dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSSZ"
internal val dateFormat = SimpleDateFormat(dateFormatPattern)
internal fun warnSubject(environment: Environment): String = environment.getProperty("logging.warn-subject", "${environment.getProperty("spring.application.name", "")} ${environment.getProperty("spring.profiles.active", "")} system exception")

internal fun existProperty(environment: Environment, key: String) =
        environment.containsProperty(key) && !environment.getProperty(key).isNullOrBlank()