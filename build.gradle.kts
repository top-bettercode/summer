import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    idea
}

allprojects {
    group = "top.bettercode.summer"
    version = "0.0.19-SNAPSHOT"

    if (name != "summer-bom") {
        apply {
            plugin("java")
            plugin("idea")
        }

        idea {
            module {
                inheritOutputDirs = false
                isDownloadJavadoc = false
                isDownloadSources = true
                outputDir = the<SourceSetContainer>()["main"].java.classesDirectory.get().asFile
                testOutputDir = the<SourceSetContainer>()["test"].java.classesDirectory.get().asFile
            }
        }

        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        configurations {
            all {
                resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
            }
        }

        repositories {
            mavenLocal()
            maven("https://s01.oss.sonatype.org/content/groups/public/")
            mavenCentral()
            gradlePluginPortal()
        }

        dependencies {
            implementation(platform(project(":summer-bom")))
            annotationProcessor(platform(project(":summer-bom")))
//            runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")

            annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
            compileOnly("org.springframework.boot:spring-boot-configuration-processor")

            compileOnly("com.google.code.findbugs:annotations")
        }

        tasks {
            "test"(Test::class) {
                useJUnitPlatform()
                reports.html.required.set(false)
                reports.junitXml.required.set(false)
            }

            withType(JavaCompile::class) {
                options.compilerArgs.add("-Xlint:deprecation")
                options.compilerArgs.add("-Xlint:unchecked")
                options.compilerArgs.add("-parameters")
                options.encoding = "UTF-8"
                dependsOn("processResources")
            }

            withType(KotlinCompile::class) {
                incremental = true
                kotlinOptions {
                    jvmTarget = "1.8"
                    freeCompilerArgs = listOf("-Xjvm-default=all")
                }
            }
        }
    }

}
