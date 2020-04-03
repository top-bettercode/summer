plugins { `java-library` }
dependencies {
    api(project(":framework:web"))

    //data
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("com.querydsl:querydsl-jpa")
    api("org.mybatis:mybatis")
    api("org.mybatis:mybatis-spring")
    api("com.github.pagehelper:pagehelper-spring-boot-starter")

    annotationProcessor("com.querydsl:querydsl-apt:4.2.1:jpa")
    testImplementation("com.h2database:h2")
}

val testquerydslSourcesDir = file("src/testquerydsl/java")
tasks {
    create("generateTestJPAQueryDSL", JavaCompile::class.java) {
        source = fileTree("src/test/java/cn/bestwu/simpleframework/data/jpa/domain")
        classpath = configurations.api.get() + configurations.runtime.get() + configurations.testImplementation.get() + configurations.annotationProcessor.get()
        options.compilerArgs.addAll(listOf("-proc:only", "-processor", "com.querydsl.apt.jpa.JPAAnnotationProcessor"))
        options.annotationProcessorPath = classpath
        options.encoding = "UTF-8"
        destinationDir = testquerydslSourcesDir
    }
    "compileTestJava"(JavaCompile::class) {
        //        dependsOn("generateTestJPAQueryDSL")
        source += fileTree(testquerydslSourcesDir)
    }
}
idea {
    module {
        testSourceDirs = testSourceDirs + testquerydslSourcesDir
    }
}