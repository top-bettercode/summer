
apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.kotlin.plugin.spring")

    plugin("cn.bestwu.kotlin-publish")
}

dependencies {
    compile(kotlin("stdlib"))


    compile("cn.bestwu:starter-logging")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    compile("org.asciidoctor:asciidoctorj")
    compile("org.asciidoctor:asciidoctorj-diagram")
    compile("org.asciidoctor:asciidoctorj-pdf")
    compile("com.github.stuxuhai:jpinyin")

    testCompile(kotlin("test-junit"))

}