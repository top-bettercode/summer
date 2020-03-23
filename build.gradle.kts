plugins {
    java
    idea
    kotlin("jvm") version "1.3.10"
    kotlin("plugin.spring") version "1.3.10"
    id("org.springframework.boot") version "2.1.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"

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

    repositories {
        mavenLocal()
        jcenter()
        maven("http://oss.jfrog.org/oss-snapshot-local")
    }

    dependencyManagement {
        dependencies {
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
