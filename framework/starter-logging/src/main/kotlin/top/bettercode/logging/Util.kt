package top.bettercode.logging

import org.springframework.core.env.Environment

/**
 *
 * @author Peter Wu
 */
object Util {

    internal fun warnSubject(environment: Environment): String = environment.getProperty(
        "summer.logging.warn-subject",
        "${environment.getProperty("spring.application.name", "")}${
            if (existProperty(
                    environment,
                    "summer.web.project-name"
                )
            ) " " + environment.getProperty("summer.web.project-name") else ""
        } ${environment.getProperty("spring.profiles.active", "")}"
    )

    internal fun existProperty(environment: Environment, key: String) =
        environment.containsProperty(key) && !environment.getProperty(key).isNullOrBlank()

}