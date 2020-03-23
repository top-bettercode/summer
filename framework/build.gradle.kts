subprojects {
    apply {
        plugin("cn.bestwu.publish")
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.springframework.boot:spring-boot-configuration-processor")

        testCompile("org.springframework.boot:spring-boot-starter-test")
    }

    tasks {
        "compileJava"(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:unchecked")
            dependsOn("processResources")
        }
        "jar"(Jar::class) {
            enabled = true
        }
        "bootJar" { enabled = false }
    }
}
