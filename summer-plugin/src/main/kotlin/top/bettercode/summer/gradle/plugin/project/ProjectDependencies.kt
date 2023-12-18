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
            filter {
                arrayOf("implementation", "testImplementation").contains(it.name)
            }.forEach {
                it.exclude(mapOf("group" to "com.vaadin.external.google", "module" to "android-json"))
            }

            if ("false" != project.findProperty("dependencies.disable-cache"))
                all {
                    it.resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
                }
        }

        project.dependencies.apply {
            if (project.isCloud) {
                add("implementation", platform("top.bettercode.summer:summer-cloud-bom:$summerVersion"))
                add("annotationProcessor", platform("top.bettercode.summer:summer-cloud-bom:$summerVersion"))
//                add("implementation", enforcedPlatform("org.springframework.boot:spring-boot-dependencies:2.3.2.RELEASE"))
                add("implementation", enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.6.21"))
                add("implementation", enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.2"))
            } else {
                add("implementation", platform("top.bettercode.summer:summer-bom:$summerVersion"))
                add("annotationProcessor", platform("top.bettercode.summer:summer-bom:$summerVersion"))
            }

            add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")
            add("compileOnly", "top.bettercode.summer:configuration-processor")
            add("annotationProcessor", "top.bettercode.summer:configuration-processor")

            add("compileOnly", "com.google.code.findbugs:annotations")
        }
    }
}