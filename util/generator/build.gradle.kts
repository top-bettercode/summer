plugins {
    `java-library`
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api(project(":util:common-lang"))
    api("org.atteo:evo-inflector")
    api("org.dom4j:dom4j")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.mybatis.generator:mybatis-generator-core")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("com.h2database:h2")
    testImplementation("mysql:mysql-connector-java")
    testImplementation("com.oracle.database.jdbc:ojdbc8")

}
