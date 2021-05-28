plugins {
    `java-library`
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")

    api(project(":framework:starter-logging"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    api("org.asciidoctor:asciidoctorj")
    api("org.asciidoctor:asciidoctorj-diagram")
    api("org.asciidoctor:asciidoctorj-pdf")
    api("com.github.stuxuhai:jpinyin")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

}