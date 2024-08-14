plugins {
    `java-library`
}

dependencies {
    api(project(":tools:generator"))

    api("org.springframework.boot:spring-boot-starter-test")
    api("org.atteo:evo-inflector")
    api("com.github.javaparser:javaparser-core")

    compileOnly("org.springframework.data:spring-data-commons")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    //test
//    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("com.h2database:h2")

    compileOnly(project(":security"))
    testImplementation(project(":security"))
}