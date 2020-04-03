plugins { `java-library` }
dependencies {
    api(project(":framework:starter-logging"))

    //web
    api("org.springframework.boot:spring-boot-starter-web")

    compileOnly("com.github.axet:kaptcha")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    compileOnly("org.springframework.boot:spring-boot-starter-jdbc")

    testImplementation("com.github.axet:kaptcha")
}


