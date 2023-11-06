plugins {
    `java-library`
}

dependencies {
    api(project(":tools:tools"))
    api("org.springframework:spring-webmvc")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    api("com.jcraft:jsch")

    compileOnly("org.asciidoctor:asciidoctorj")
    compileOnly("org.asciidoctor:asciidoctorj-diagram")
    compileOnly("org.asciidoctor:asciidoctorj-pdf")

    compileOnly("com.github.stuxuhai:jpinyin")
    compileOnly("org.dom4j:dom4j")

    testImplementation("com.github.stuxuhai:jpinyin")
    testImplementation("org.dom4j:dom4j")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testImplementation("com.h2database:h2")
    testImplementation("mysql:mysql-connector-java")
    testImplementation("com.oracle.database.jdbc:ojdbc8")

}
