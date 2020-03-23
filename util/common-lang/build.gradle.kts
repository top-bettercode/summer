dependencies {
    compile(kotlin("stdlib"))
    compile("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("org.jsoup:jsoup")
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    testCompile("org.jsoup:jsoup")
    testCompile("junit:junit")
}

