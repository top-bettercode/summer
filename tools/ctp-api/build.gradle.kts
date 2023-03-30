import org.apache.tools.ant.taskdefs.condition.Os
import java.util.stream.Collectors

plugins {
    `java-library`
}

apply {
    plugin("summer.publish")
}

dependencies {
    api(project(":web"))
    api(fileTree(mapOf("dir" to "libs")))

    testImplementation(project(":test"))
}

tasks {
    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.stream().map { zipTree(it) }
            .collect(Collectors.toList()))
    }
    "test"(Test::class) {
        val nativePath = project.file("native/cp").absolutePath
        jvmArgs = listOf("-Djava.library.path=$nativePath")
        if (Os.isFamily(Os.FAMILY_UNIX))
            environment("LD_LIBRARY_PATH", nativePath)
    }
}