import org.gradle.api.Project

/**
 *
 * @author Peter Wu
 */
object ProjectUtil {

    val Project.isBoot: Boolean
        get() = !isCore && parent?.name != "util" && name != "util"

    val Project.needDoc: Boolean
        get() = parent?.name != "util" && name != "util"

    val Project.isCore: Boolean
        get() = name == (findProperty("tools.project") ?: "core")


    val Project.isCloud: Boolean
        get() = "true" == findProperty("app.cloud")

}