dependencies {
    compile(project(":framework:web"))

    //data
    compile("org.springframework.boot:spring-boot-starter-data-jpa")
    compile("com.querydsl:querydsl-jpa")
    compile("org.mybatis:mybatis")
    compile("org.mybatis:mybatis-spring")
    compile("com.github.pagehelper:pagehelper-spring-boot-starter")

    annotationProcessor("com.querydsl:querydsl-apt:4.2.1:jpa")
    testCompile("com.h2database:h2")
}

val testquerydslSourcesDir = file("src/testquerydsl/java")
tasks {
    create("generateTestJPAQueryDSL", JavaCompile::class.java) {
        source = fileTree("src/test/java/cn/bestwu/simpleframework/data/jpa/domain")
        classpath = configurations.compile.get() + configurations.runtime.get() + configurations.testCompile.get() + configurations.annotationProcessor.get()
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