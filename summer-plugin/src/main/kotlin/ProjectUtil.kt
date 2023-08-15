import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive


val Project.isBoot: Boolean
    get() = !isCore
            && "tools" != name
            && ((parent == rootProject) || parent?.name == "server" || parent?.name == "service")

val Project.isCore: Boolean
    get() {
        var projectCore = findProperty("project.core") as? String
        if (projectCore.isNullOrBlank())
            projectCore = "^(core|.*-core)$"
        return name.matches(Regex(projectCore))
    }

val Project.isCloud: Boolean
    get() = "true" == findProperty("spring.cloud")


/**
 *
 * @author Peter Wu
 */
val Project.profilesActive: String
    get() = this.profilesActive

fun Project.profileClosure(closure: Project.(ProfileExtension) -> Unit) {
    val profile = project.extensions.getByType(ProfileExtension::class.java)
    profile.closure.add(closure)
}

fun Project.profileClosure(active: String, closure: Project.(ProfileExtension) -> Unit) {
    val profile = project.extensions.getByType(ProfileExtension::class.java)
    profile.profileClosure.computeIfAbsent(active) { mutableSetOf() }.add(closure)
}