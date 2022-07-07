plugins { `java-library` }
apply {
    plugin("org.springframework.boot")
    plugin("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    api(project(":framework:web"))

    compileOnly("org.bouncycastle:bcprov-jdk18on")


    testImplementation(project(":util:test"))
}

