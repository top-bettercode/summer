plugins {
    `java-library`
    idea
}

allprojects {
    group = "top.bettercode.summer"
    version = summerVersion

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
            outputDir = the<SourceSetContainer>()["main"].java.classesDirectory.get().asFile
            testOutputDir = the<SourceSetContainer>()["test"].java.classesDirectory.get().asFile
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

            dependency("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            dependency("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
            dependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}")

            dependency("org.jetbrains.dokka:kotlin-as-java-plugin:$kotlinVersion")
            dependency("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
            dependency("com.gradle.publish:plugin-publish-plugin:0.20.0")
            dependency("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")

            dependency("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
            dependency("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")

            dependency("top.bettercode.summer:windows-service-plugin:1.2.0-SNAPSHOT")

            dependency("top.bettercode.wechat:weixin-mp:0.9.7")
            dependency("top.bettercode.wechat:weixin-app:0.9.7")

            dependency("commons-codec:commons-codec:1.15")
            dependency("org.json:json:20220320")
            dependency("org.javassist:javassist:3.28.0-GA")
            dependency("org.dom4j:dom4j:2.1.3")
            dependency("org.jsoup:jsoup:1.14.3")
            dependency("org.atteo:evo-inflector:1.3")
            dependency("com.github.axet:kaptcha:0.0.9")
            dependency("com.github.stuxuhai:jpinyin:1.1.8")
            dependency("net.sourceforge.plantuml:plantuml:1.2022.2")
            dependency("com.github.jsqlparser:jsqlparser:4.3")
            dependency("com.qcloud:vod_api:2.1.5")

            dependency("mysql:mysql-connector-java:8.0.28")
            dependency("com.oracle.database.jdbc:ojdbc8:$oracleJdbcVersion")

            dependency("org.asciidoctor:asciidoctorj:2.5.3")
            dependency("org.asciidoctor:asciidoctorj-diagram:2.2.1")
            dependency("org.asciidoctor:asciidoctorj-pdf:1.6.2")

            dependency("net.logstash.logback:logstash-logback-encoder:7.0.1")
            dependency("javax.mail:mail:1.4.7")

            dependency("org.dhatim:fastexcel:0.12.12")
            dependency("org.dhatim:fastexcel-reader:0.12.14")

            dependency("org.mybatis:mybatis:3.5.9")
            dependency("org.mybatis:mybatis-spring:2.0.7")

            dependency("org.mybatis.generator:mybatis-generator-core:1.4.1")

            dependency("org.bouncycastle:bcprov-jdk18on:1.71")

            dependency("xerces:xercesImpl:2.12.2")
            dependency("com.auth0:java-jwt:4.0.0")
        }
    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("com.google.code.findbugs:annotations:3.0.1")
    }

    tasks {
        build {
            setDependsOn(listOf("testClasses"))
        }

        "test"(Test::class) {
            useJUnitPlatform()
            reports.html.required.set(false)
            reports.junitXml.required.set(false)
        }
        "compileJava"(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:deprecation")
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-parameters")
            options.encoding = "UTF-8"
            dependsOn("processResources")
        }
        withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
            incremental = true
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }
    }

}
