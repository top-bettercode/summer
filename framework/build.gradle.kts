subprojects {
    apply {
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
    }
    if(name.contains("logging")){
        apply {
            plugin("cn.bestwu.kotlin-publish")
            plugin("org.jetbrains.kotlin.jvm")
            plugin("org.jetbrains.kotlin.plugin.spring")
        }
    }else{
        apply {
            plugin("cn.bestwu.publish")
        }
    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.springframework.boot:spring-boot-configuration-processor")

        testCompile("org.springframework.boot:spring-boot-starter-test")
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
