import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
}

val javaVersion = JavaVersion.VERSION_17

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.rationalityfrontline:kevent:2.1.2")
    api("org.rationalityfrontline:jctp:6.6.1_P1-1.0.5")

    testImplementation(project(":tools:generator"))
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks{
    withType(KotlinCompile::class) {
//                this.outputs.upToDateWhen { false }
        incremental = true
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}