plugins {
    `java-library`
}

//val ctpVersion = "6.3.13_20181119"
//val ctpVersion = "6.3.15_20190220"
//val ctpVersion = "v6.6.1_P1_20210406"
val ctpVersion = findProperty("V") ?: "v6.6.1_P1_CP_20210406"
version = "$ctpVersion-SNAPSHOT"

dependencies {
    api(project(":tools:tools"))
    api(fileTree("libs/$ctpVersion"))

    testImplementation("org.slf4j:slf4j-simple")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks {

//    test {
//        environment("LD_LIBRARY_PATH", file("build/native").absolutePath)
//    }

    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs/$ctpVersion")).files.map { zipTree(it) })
    }

    @Suppress("UnstableApiUsage")
    "processResources"(ProcessResources::class) {
        from(file("native/$ctpVersion")) {
            into("native")
        }
    }
}

