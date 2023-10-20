plugins {
    `java-library`
}

dependencies {
    api(project(":dist-plugin"))
    api(project(":tools:generator"))

    api("org.jetbrains.kotlin:kotlin-allopen")

    api("org.springframework.boot:spring-boot-gradle-plugin")

    api("org.asciidoctor:asciidoctorj")
    api("org.asciidoctor:asciidoctorj-diagram")
    api("org.asciidoctor:asciidoctorj-pdf")

    api("net.sourceforge.plantuml:plantuml")
    api("org.atteo:evo-inflector")
    api("org.dom4j:dom4j")
    api("com.github.stuxuhai:jpinyin")

    api("mysql:mysql-connector-java")
    api("com.oracle.database.jdbc:ojdbc8")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    testImplementation("org.jdom:jdom2:2.0.6")
}