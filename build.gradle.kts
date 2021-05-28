plugins {
    `java-library`
    idea
}

allprojects {
    group = "cn.bestwu.summer"
    version = properties["version"] as String

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
        all {
            resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
        }
    }

    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.jfrog.org/oss-snapshot-local")
    }

    configurations {
        filter { arrayOf("implementation", "testImplementation").contains(it.name) }.forEach {
            it.exclude("org.codehaus.jackson")
            it.exclude("org.junit.vintage", "junit-vintage-engine")
        }

    }
    extensions.configure(io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension::class) {
        dependencies {
            val kotlinVersion = property("kotlin.version")
            dependency("org.springframework.boot:spring-boot-gradle-plugin:2.2.5.RELEASE")
            dependency("com.oracle.database.jdbc:ojdbc8:21.1.0.0")
            dependency("gradle.plugin.com.github.alexeylisyutenko:windows-service-plugin:1.1.0")
            dependency("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
            dependency("com.gradle.publish:plugin-publish-plugin:0.10.0")
            dependency("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
            dependency("org.jetbrains.dokka:kotlin-as-java-plugin:$kotlinVersion")
            dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

            dependency("org.springframework.cloud:spring-cloud-starter-config:2.2.5.RELEASE")
            dependency("org.jfrog.buildinfo:build-info-extractor-gradle:4.23.4")

            dependency("cn.bestwu.wechat:weixin-mp:0.9.7")
            dependency("cn.bestwu.wechat:weixin-app:0.9.7")

            dependency("org.javassist:javassist:3.27.0-GA")
            dependency("org.yaml:snakeyaml:1.26")
            dependency("com.google.guava:guava:29.0-jre")
            dependency("org.dom4j:dom4j:2.1.1")
            dependency("org.atteo:evo-inflector:1.2.2")
            dependency("net.sourceforge.plantuml:plantuml:1.2020.4")

            dependency("org.jsoup:jsoup:1.13.1")
            dependency("com.github.stuxuhai:jpinyin:1.1.8")
            dependency("mysql:mysql-connector-java:8.0.19")

            dependency("org.asciidoctor:asciidoctorj:2.4.0")
            dependency("org.asciidoctor:asciidoctorj-diagram:2.0.2")
            dependency("org.asciidoctor:asciidoctorj-pdf:1.5.3")


            dependency("net.logstash.logback:logstash-logback-encoder:6.3")
            dependency("javax.mail:mail:1.4.7")
            dependency("com.github.axet:kaptcha:0.0.9")

            dependency("org.dhatim:fastexcel:0.12.8")
            dependency("org.dhatim:fastexcel-reader:0.12.9")

            dependency("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.5.0")

            dependency("org.mybatis:mybatis:3.5.4")
            dependency("org.mybatis:mybatis-spring:2.0.4")
            dependency("org.mybatis.generator:mybatis-generator-core:1.4.0")
            dependency("com.baomidou:mybatis-plus:2.3.3") {
                exclude("com.baomidou:mybatis-plus-generate")
            }
            dependency("com.baomidou:mybatisplus-spring-boot-starter:1.0.5") {
                exclude("org.springframework.boot:spring-boot-configuration-processor")
            }

            dependency("com.github.pagehelper:pagehelper:5.1.11")

            dependency("jakarta.persistence:jakarta.persistence-api:2.2.3")
        }
    }

    tasks {
        build {
            setDependsOn(listOf("testClasses"))
        }

        "test"(Test::class) {
            useJUnitPlatform()
        }
        "compileJava"(JavaCompile::class) {
//            options.compilerArgs.add("-Xlint:deprecation")
            options.encoding = "UTF-8"
        }
    }

}
