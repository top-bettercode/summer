import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.generator.ProjectUtil.isBoot
import top.bettercode.summer.gradle.plugin.generator.ProjectUtil.isCloud
import top.bettercode.summer.gradle.plugin.generator.ProjectUtil.isCore
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive

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

val Project.isBoot: Boolean
    get() = this.isBoot


val Project.isCore: Boolean
    get() = this.isCore

val Project.isCloud: Boolean
    get() = this.isCloud