package top.bettercode.gradle.generator

import org.gradle.api.Project

/**
 *
 * @author Peter Wu
 */
object ProjectUtil {

    val Project.isBoot: Boolean
        get() = !isCore && "util" != name && "commons" != name && ((parent == rootProject) || parent?.name == "server" || parent?.name == "service")

    val Project.isCore: Boolean
        get() = name.matches(Regex((findProperty("project.core") as? String) ?: "^(core|.*-core)$"))

    val Project.isCloud: Boolean
        get() = "true" == findProperty("app.cloud")

}