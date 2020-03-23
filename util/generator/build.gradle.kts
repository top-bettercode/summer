apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("cn.bestwu.kotlin-publish")
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.atteo:evo-inflector")
    compile("org.dom4j:dom4j")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")

    testCompile("org.mybatis.generator:mybatis-generator-core")

    testCompile(kotlin("test-junit"))

    testCompile("com.h2database:h2")
    testCompile("mysql:mysql-connector-java:8.0.16")

}
