plugins {
    `java-library`
}

dependencies {
    api(project(":tools:tools"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    api("org.asciidoctor:asciidoctorj")
    api("org.asciidoctor:asciidoctorj-diagram")
    api("org.asciidoctor:asciidoctorj-pdf")

    api("com.github.stuxuhai:jpinyin")
    api("org.dom4j:dom4j")

    testImplementation("org.mybatis.generator:mybatis-generator-core")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testImplementation("com.h2database:h2")
    testImplementation("mysql:mysql-connector-java")
    testImplementation("com.oracle.database.jdbc:ojdbc8")

}
