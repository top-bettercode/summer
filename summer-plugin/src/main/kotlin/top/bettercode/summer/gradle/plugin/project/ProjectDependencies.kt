package top.bettercode.summer.gradle.plugin.project

import isCloud
import org.gradle.api.Project
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
object ProjectDependencies {

    private val summerVersion = ProjectPlugin::class.java.`package`.implementationVersion

    fun config(project: Project) {
        project.configurations.apply {
            if ("false" != project.findProperty("dependencies.disable-cache"))
                all {
                    it.resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
                }
        }

        project.dependencies.apply {
            add("implementation", platform("top.bettercode.summer:${if (project.isCloud) "summer-cloud-bom" else "summer-bom"}:$summerVersion"))
            add("annotationProcessor", platform("top.bettercode.summer:${if (project.isCloud) "summer-cloud-bom" else "summer-bom"}:$summerVersion"))

            add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")
            add("compileOnly", "com.google.code.findbugs:annotations")
        }
    }
}