import cn.bestwu.gradle.profile.ProfileExtension
import cn.bestwu.gradle.profile.findActive
import org.gradle.api.Project

const val profilesDefaultActive: String = "local"
const val simpleProfilesActiveName: String = "P"
const val projectMark: String = "project"
const val profilesActiveName: String = "profiles.active"

/**
 *
 * @author Peter Wu
 */
val Project.profilesActive: String
    get() {
        val systemProperties = System.getProperties()
        val findActive = findActive { name ->
            systemProperties.getProperty(name)
        } ?: findActive { name ->
            rootProject.findProperty(name) as? String
        }
        return if (findActive.isNullOrBlank()) {
            profilesDefaultActive
        } else
            findActive
    }

fun Project.profileClosure(closure: Project.(ProfileExtension) -> Unit) {
    val profile = project.extensions.getByType(ProfileExtension::class.java)
    profile.closure.add(closure)
}

fun Project.profileClosure(active: String, closure: Project.(ProfileExtension) -> Unit) {
    val profile = project.extensions.getByType(ProfileExtension::class.java)
    profile.profileClosure.computeIfAbsent(active) { mutableSetOf() }.add(closure)
}
