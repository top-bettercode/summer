import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    idea
}

val javaVersion = JavaVersion.VERSION_1_8

allprojects {
    group = "top.bettercode.summer"
    version = "0.0.19-SNAPSHOT"

    if (name != "summer-bom") {
        apply {
            plugin("java")
            plugin("idea")
            if (name.endsWith("-plugin")) {
                plugin("org.jetbrains.kotlin.jvm")
                plugin("summer.plugin-publish")
            } else {
                plugin("org.springframework.boot")
            }
        }

        idea {
            module {
                isDownloadJavadoc = false
                isDownloadSources = true
            }
        }

        java {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
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
                    jvmTarget = javaVersion.toString()
                    freeCompilerArgs = listOf("-Xjvm-default=all")
                }
            }

            if (!name.endsWith("-plugin")) {
                "jar"(Jar::class) {
                    enabled = true
                    archiveClassifier.convention("")
                }
                "bootJar" {
                    enabled = false
                }
            }
        }
    }

}
