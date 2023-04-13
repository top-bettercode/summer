plugins {
    `java-library`
}

dependencies {
    api(project(":tools:generator"))

    api(gradleApi())

    api("org.jetbrains.kotlin:kotlin-gradle-plugin")
    api("org.jetbrains.kotlin:kotlin-allopen")

    api("org.springframework.boot:spring-boot-gradle-plugin")

    api("org.yaml:snakeyaml")
    api("net.sourceforge.plantuml:plantuml")
    api("org.atteo:evo-inflector")

    api("mysql:mysql-connector-java")
    api("com.oracle.database.jdbc:ojdbc8")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}