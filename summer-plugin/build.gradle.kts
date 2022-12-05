plugins {
    `java-library`
}

apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("summer.plugin-publish")
}

dependencies {
    api(project(":tools:generator"))

    api(gradleApi())

    api("org.springframework.boot:spring-boot-gradle-plugin")

    api("top.bettercode.summer:windows-service-plugin")

    api("org.yaml:snakeyaml")
    api("net.sourceforge.plantuml:plantuml")
    api("org.atteo:evo-inflector")

    api("mysql:mysql-connector-java")
    api("com.oracle.database.jdbc:ojdbc8")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}