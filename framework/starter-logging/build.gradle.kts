import org.jetbrains.dokka.gradle.DokkaTask


plugins {

}


dependencies {
    compile(kotlin("reflect"))

    compile("org.springframework.boot:spring-boot")
    compile("org.springframework.boot:spring-boot-starter-logging")
    compile("javax.mail:mail:1.4.7")
    compile("cn.bestwu:common-lang:1.1.7-SNAPSHOT")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("org.springframework.boot:spring-boot-starter-web")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("net.logstash.logback:logstash-logback-encoder:5.2")
    testCompile("net.logstash.logback:logstash-logback-encoder:5.2")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile(kotlin("test-junit"))
}

tasks {
    "dokkaJavadoc"(DokkaTask::class) {
        noStdlibLink = true
    }
}