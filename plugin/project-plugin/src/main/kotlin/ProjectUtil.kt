import org.gradle.api.Project

/**
 *
 * @author Peter Wu
 */
object ProjectUtil {

    val Project.isBoot: Boolean
        get() = !isCore && ((parent == rootProject) || parent?.name == "server" || parent?.name == "service")

    val Project.isCore: Boolean
        get() = name == (findProperty("project.core") ?: "core")

    val Project.isCloud: Boolean
        get() = "true" == findProperty("app.cloud")

}