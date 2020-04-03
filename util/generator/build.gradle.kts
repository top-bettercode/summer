plugins {
    `java-library`
}

dependencies {
    api(kotlin("stdlib"))
    api("org.atteo:evo-inflector")
    api("org.dom4j:dom4j")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.mybatis.generator:mybatis-generator-core")

    testImplementation(kotlin("test-junit"))

    testImplementation("com.h2database:h2")
    testImplementation("mysql:mysql-connector-java")

}
