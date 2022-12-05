plugins {
    `java-library`
}

apply {
    plugin("org.springframework.boot")
    plugin("summer.publish")
}

dependencies {
    api(project(":web"))

    //data
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    api("org.mybatis:mybatis")
    api("com.github.jsqlparser:jsqlparser")

    compileOnly("com.querydsl:querydsl-jpa")
    testImplementation("com.querydsl:querydsl-jpa")
//    testAnnotationProcessor("com.querydsl:querydsl-apt")
//    testAnnotationProcessor("jakarta.persistence:jakarta.persistence-api")

    testImplementation(project(":test"))
    testImplementation("org.mybatis:mybatis-spring")
    testImplementation("com.h2database:h2")
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