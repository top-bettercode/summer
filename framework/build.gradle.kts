subprojects {
    apply {
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
    }
    if(name.contains("logging")){
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            plugin("org.jetbrains.kotlin.plugin.spring")
            plugin("summer.kotlin-publish")
        }
    }else{
        apply {
            plugin("summer.publish")
        }
    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.springframework.boot:spring-boot-configuration-processor")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks {
        "compileJava"(JavaCompile::class) {
            dependsOn("processResources")
        }
        "jar"(Jar::class) {
            enabled = true
        }
        "bootJar" { enabled = false }
    }
}
