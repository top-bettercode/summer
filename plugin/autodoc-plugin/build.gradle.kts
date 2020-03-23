import org.apache.tools.ant.filters.ReplaceTokens

dependencies {
    compile(gradleApi())
    compile(project(":util:autodoc-core"))
    testCompile(kotlin("test-junit"))
}

tasks {
    "processResources"(ProcessResources::class) {
        outputs.upToDateWhen { false }
        filesMatching(setOf("**/*.properties")) {
            filter(mapOf("tokens" to mapOf("autodoc.version.default" to project.version)), ReplaceTokens::class.java)
        }
    }
}