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
    api(project(":tools:tools"))
//    api( "org.rationalityfrontline.ktrader:ktrader-broker-api:1.2.0")
    api(project(":tools:ktrader-broker-api"))
    api("org.rationalityfrontline:jctp:6.6.1_P1-1.0.1")
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