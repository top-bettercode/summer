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
        get() {
            var projectCore = findProperty("project.core") as? String
            if (projectCore.isNullOrBlank())
                projectCore = "^(core|.*-core)$"
            return name.matches(Regex(projectCore))
        }

    val Project.isCloud: Boolean
        get() = "true" == findProperty("app.cloud")

}