dependencies {
    compile(kotlin("stdlib"))

    compile(project(":framework:starter-logging"))
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    compile("org.asciidoctor:asciidoctorj")
    compile("org.asciidoctor:asciidoctorj-diagram")
    compile("org.asciidoctor:asciidoctorj-pdf")
    compile("com.github.stuxuhai:jpinyin")

    testCompile(kotlin("test-junit"))

}