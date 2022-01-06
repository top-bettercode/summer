package plugin

import isBoot
import isCloud
import isCore
import needDoc
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

@Deprecated(
    message = "replaceWith isBoot",
    replaceWith = ReplaceWith(expression = "isBoot", imports = arrayOf("isBoot"))
)
val Project.mainProject: Boolean
    get() = isBoot

/**
 *
 * @author Peter Wu
 */
class ProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.description = project.findProperty("application.name") as? String

        project.allprojects.forEach { subProject ->
            subProject.plugins.apply("idea")
            subProject.plugins.apply("java")

            subProject.group = subProject.properties["app.packageName"] as String
            subProject.version = subProject.properties["app.version"] as String

            //idea
            subProject.extensions.configure(org.gradle.plugins.ide.idea.model.IdeaModel::class.java) { idea ->
                idea.module {
                    it.inheritOutputDirs = false
                    it.isDownloadJavadoc = false
                    it.isDownloadSources = true
                    val convention = subProject.convention.getPlugin(
                        JavaPluginConvention::class.java
                    )
                    it.outputDir = convention.sourceSets
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.outputDir
                    it.testOutputDir = convention.sourceSets
                        .getByName(SourceSet.TEST_SOURCE_SET_NAME).java.outputDir
                }
            }

            //java
            subProject.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) { java ->
                java.sourceCompatibility = JavaVersion.VERSION_1_8
                java.targetCompatibility = JavaVersion.VERSION_1_8
            }

            //plugins
            subProject.plugins.apply {
                apply("summer.profile")
                apply("summer.packageinfo")
                apply("io.spring.dependency-management")
            }

            if (subProject.needDoc) {
                subProject.plugins.apply {
                    apply("summer.generator")
                    apply("summer.autodoc")
                }
            }
            if (subProject.isBoot) {
                subProject.plugins.apply {
                    apply("application")
                    apply("org.springframework.boot")
                    apply("summer.dist")
                }
            }

            //dependencies
            ProjectDependencies.config(subProject)
            //tasks
            SubProjectTasks.config(subProject)

            if (subProject.isCore) {
                CoreProjectTasks.config(subProject)
            }
        }

        //tasks
        RootProjectTasks.config(project)

        //docker
        if (project.isCloud) {
            project.apply { project.plugins.apply(Docker::class.java) }
        }
    }
}