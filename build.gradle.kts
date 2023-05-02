import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    idea
}

val javaVersion = JavaVersion.VERSION_1_8

allprojects {
    group = "top.bettercode.summer"
    version = "0.0.19-SNAPSHOT"


    val isBomProject = arrayOf(
            "summer-bom",
            "summer-cloud-bom"
    ).contains(name)

    val isPluginProject = name.endsWith("-plugin")

    val isJavaProject = arrayOf(
            "data-jpa",
            "env",
            "security",
            "excel",
            "sap",
            "ueditor"
    ).contains(name)

    if (!isBomProject) {
        apply {
            plugin("java")
            plugin("idea")
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
            maven("https://maven.aliyun.com/repository/public/")
            maven("https://s01.oss.sonatype.org/content/groups/public/")
            mavenCentral()
            gradlePluginPortal()
        }

        dependencies {
            implementation(platform(project(":summer-bom")))
            //runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")

            compileOnly("com.google.code.findbugs:annotations")
        }


        if (isPluginProject) {
            apply {
                plugin("org.jetbrains.kotlin.jvm")
                plugin("summer.plugin-publish")
            }
        } else {
            apply {
                plugin("org.springframework.boot")
            }
            tasks {
                "jar"(Jar::class) {
                    enabled = true
                    archiveClassifier.convention("")
                }
                "bootJar" {
                    enabled = false
                }
            }

            if (isJavaProject) {
                apply {
                    plugin("summer.publish")
                }

                dependencies {
                    annotationProcessor(platform(project(":summer-bom")))
                    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
                }
            } else {
                apply {
                    plugin("org.jetbrains.kotlin.jvm")
                    plugin("org.jetbrains.kotlin.kapt")
                    plugin("org.jetbrains.kotlin.plugin.spring")
                    plugin("summer.kotlin-publish")
                }

                dependencies {
                    "kapt"(platform(project(":summer-bom")))
                    "kapt"("org.springframework.boot:spring-boot-configuration-processor")
                }
            }
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
        }
    }

}
