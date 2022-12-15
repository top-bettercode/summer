plugins {
    `java-library`
}

apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.kotlin.plugin.spring")
    plugin("summer.kotlin-publish")
}

dependencies {
    api(project(":tools:generator"))

    api("org.springframework.boot:spring-boot-starter-test")
    api("org.atteo:evo-inflector")

    compileOnly("org.springframework.data:spring-data-commons")

    //test
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("com.h2database:h2")

    compileOnly(project(":security"))
    testImplementation(project(":security"))
}