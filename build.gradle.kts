plugins {
    java
    idea
}

allprojects {
    group = "cn.bestwu.summer"
    version = "0.0.1-SNAPSHOT"

    apply {
        plugin("java")
        plugin("idea")
        plugin("io.spring.dependency-management")
    }

    idea {
        module {
            inheritOutputDirs = false
            isDownloadJavadoc = false
            isDownloadSources = true
            outputDir = the<SourceSetContainer>()["main"].java.outputDir
            testOutputDir = the<SourceSetContainer>()["test"].java.outputDir
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configurations {
        filter { arrayOf("compile", "testCompile").contains(it.name) }.forEach { it.exclude("org.codehaus.jackson") }
        all {
            resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
        }
    }

    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public")
        jcenter()
        gradlePluginPortal()
        maven("http://oss.jfrog.org/oss-snapshot-local")
    }

    extensions.configure(io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension::class) {
        dependencies {
            dependency("org.jsoup:jsoup:1.11.3")
            dependency("cn.bestwu:common-lang:1.1.7-SNAPSHOT")
            dependency("cn.bestwu:api-sign:1.2.4")
            dependency("com.github.stuxuhai:jpinyin:1.1.8")
            dependency("cn.bestwu:generator:0.0.57-SNAPSHOT")
            dependency("cn.bestwu:starter-logging:2.0.12-SNAPSHOT")
            dependency("mysql:mysql-connector-java:5.1.47")
            dependency("org.asciidoctor:asciidoctorj:2.2.0")
            dependency("org.asciidoctor:asciidoctorj-diagram:2.0.0")
            dependency("org.asciidoctor:asciidoctorj-pdf:1.5.0-beta.8")

            dependency("cn.bestwu:common-lang:1.1.7-SNAPSHOT")
            dependency("cn.bestwu:starter-logging:2.0.12-SNAPSHOT")

            dependency("javax.mail:mail:1.4")
            dependency("com.baomidou:mybatis-plus:2.3.3") {
                exclude("com.baomidou:mybatis-plus-generate")
            }
            dependency("com.baomidou:mybatisplus-spring-boot-starter:1.0.5") {
                exclude("org.springframework.boot:spring-boot-configuration-processor")
            }
            dependency("com.github.axet:kaptcha:0.0.9")
            dependency("org.dhatim:fastexcel:0.10.11")
            dependency("org.dhatim:fastexcel-reader:0.10.11")
            dependency("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.1.6.RELEASE")
            dependency("org.springframework.security.oauth:spring-security-oauth2:2.3.6.RELEASE")

            dependency("org.mybatis:mybatis:3.5.1")
            dependency("org.mybatis:mybatis-spring:2.0.1")
            dependency("com.github.pagehelper:pagehelper-spring-boot-starter:1.2.10")
        }
    }

}
