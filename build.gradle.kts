plugins {
    `java-library`
    idea
}

allprojects {
    group = "top.bettercode.summer"
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
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://s01.oss.sonatype.org/content/groups/public/")
        mavenCentral()
        gradlePluginPortal()
    }

    configurations {
        filter { arrayOf("implementation", "testImplementation").contains(it.name) }.forEach {
            it.exclude("org.codehaus.jackson")
            it.exclude("org.junit.vintage", "junit-vintage-engine")
        }

    }
    extensions.configure(io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension::class) {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }

        dependencies {
            val kotlinVersion = property("kotlin.version")
            dependency("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            dependency("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
            dependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            dependency("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
            dependency("org.jetbrains.dokka:kotlin-as-java-plugin:$kotlinVersion")
            dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

            dependency("org.springframework.boot:spring-boot-gradle-plugin:2.5.0")
            dependency("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
            dependency("gradle.plugin.com.github.alexeylisyutenko:windows-service-plugin:1.1.0")
            dependency("com.gradle.publish:plugin-publish-plugin:0.15.0")
            dependency("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")

            dependency("org.springframework.cloud:spring-cloud-starter-config:3.0.3")
            dependency("org.springframework.security:spring-security-rsa:1.0.10.RELEASE")

            dependency("top.bettercode.wechat:weixin-mp:0.9.8-SNAPSHOT")
            dependency("top.bettercode.wechat:weixin-app:0.9.8-SNAPSHOT")

            dependency("commons-codec:commons-codec:1.15")
            dependency("org.json:json:20210307")
            dependency("org.javassist:javassist:3.28.0-GA")
            dependency("com.google.guava:guava:30.1.1-jre")
            dependency("org.dom4j:dom4j:2.1.3")
            dependency("org.atteo:evo-inflector:1.2.2")
            dependency("net.sourceforge.plantuml:plantuml:8059")

            dependency("org.jsoup:jsoup:1.13.1")
            dependency("com.github.stuxuhai:jpinyin:1.1.8")
            dependency("mysql:mysql-connector-java:8.0.25")
            dependency("com.oracle.database.jdbc:ojdbc8:21.1.0.0")

            dependency("org.asciidoctor:asciidoctorj:2.5.1")
            dependency("org.asciidoctor:asciidoctorj-diagram:2.1.2")
            dependency("org.asciidoctor:asciidoctorj-pdf:1.6.0")


            dependency("net.logstash.logback:logstash-logback-encoder:6.6")
            dependency("javax.mail:mail:1.4.7")
            dependency("com.github.axet:kaptcha:0.0.9")

            dependency("org.dhatim:fastexcel:0.12.11")
            dependency("org.dhatim:fastexcel-reader:0.12.11")

            dependency("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.5.0")

            dependency("org.mybatis:mybatis:3.5.7")
            dependency("org.mybatis:mybatis-spring:2.0.6")
            dependency("org.mybatis.generator:mybatis-generator-core:1.4.0")
            dependency("com.baomidou:mybatis-plus:2.3.3") {
                exclude("com.baomidou:mybatis-plus-generate")
            }
            dependency("com.baomidou:mybatisplus-spring-boot-starter:1.0.5") {
                exclude("org.springframework.boot:spring-boot-configuration-processor")
            }

            dependency("com.github.pagehelper:pagehelper:5.2.0")

            dependency("jakarta.persistence:jakarta.persistence-api:2.2.3")
            //-
            dependency("org.apache.logging.log4j:log4j-api:2.15.0")
            dependency("org.apache.logging.log4j:log4j-core:2.15.0")
            dependency("org.apache.logging.log4j:log4j-to-slf4j:2.15.0")

        }
    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    }

    tasks {
        build {
            setDependsOn(listOf("testClasses"))
        }

        "test"(Test::class) {
            useJUnitPlatform()
        }
        "compileJava"(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:deprecation")
            options.compilerArgs.add("-parameters")
            options.encoding = "UTF-8"
            dependsOn("processResources")
        }
    }

}
