package plugin

import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import isCloud
import org.gradle.api.Project
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
object ProjectDependencies {

    private val kotlinVersionConfig: ResourceBundle = ResourceBundle.getBundle("kotlin-version")
    private val kotlinVersion =
        kotlinVersionConfig.getString("kotlin.version")
    private val kotlinCoroutinesVersion = kotlinVersionConfig.getString("kotlin-coroutines.version")

    fun config(project: Project) {
        project.configurations.apply {
            filter {
                arrayOf(
                    "implementation",
                    "testImplementation"
                ).contains(it.name)
            }.forEach {
                it.exclude(mapOf("group" to "org.codehaus.jackson"))
                it.exclude(
                    mapOf(
                        "group" to "com.vaadin.external.google",
                        "module" to "android-json"
                    )
                )
                it.exclude(
                    mapOf(
                        "group" to "org.junit.vintage",
                        "module" to "junit-vintage-engine"
                    )
                )
            }
            if ("false" != project.findProperty("dependencies.disable-cache"))
                all {
                    it.resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
                }
        }

        project.extensions.configure(StandardDependencyManagementExtension::class.java) { ext ->
            ext.imports {
                it.mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
                if (project.isCloud) {
                    it.mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2.2.7.RELEASE")
                    it.mavenBom("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR12")
                }
            }


            ext.dependencies {
                it.apply {
                    val summerVersion =
                        ProjectPlugin::class.java.`package`.implementationVersion

                    dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                    dependency("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    dependency("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
                    dependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

                    dependency("top.bettercode.summer:config:$summerVersion")
                    dependency("top.bettercode.summer:environment:$summerVersion")
                    dependency("top.bettercode.summer:api-sign:$summerVersion")
                    dependency("top.bettercode.summer:common-lang:$summerVersion")
                    dependency("top.bettercode.summer:starter-logging:$summerVersion")
                    dependency("top.bettercode.summer:autodoc-gen:$summerVersion")
                    dependency("top.bettercode.summer:excel:$summerVersion")
                    dependency("top.bettercode.summer:ueditor:$summerVersion")
                    dependency("top.bettercode.summer:wechat:$summerVersion")
                    dependency("top.bettercode.summer:sms:$summerVersion")

                    dependency("top.bettercode.summer:web:$summerVersion")
                    dependency("top.bettercode.summer:data-jpa:$summerVersion")
                    dependency("top.bettercode.summer:data-mybatis:$summerVersion")
                    dependency("top.bettercode.summer:security:$summerVersion")

                    dependency("top.bettercode.summer:kk:$summerVersion")
                    dependency("top.bettercode.summer:test:$summerVersion")

                    dependency("top.bettercode.wechat:weixin-mp:0.9.7")
                    dependency("top.bettercode.wechat:weixin-app:0.9.7")

                    val oracleJdbcVersion = "21.5.0.0"
                    dependency("com.oracle.database.jdbc:ojdbc8:$oracleJdbcVersion")
                    dependency("com.oracle.database.jdbc:ucp:$oracleJdbcVersion")
                    dependency("com.oracle.database.security:oraclepki:$oracleJdbcVersion")
                    dependency("com.oracle.database.security:osdt_core:$oracleJdbcVersion")
                    dependency("com.oracle.database.security:osdt_cert:$oracleJdbcVersion")

                    dependency("jakarta.persistence:jakarta.persistence-api:2.2.3")

                    dependency("org.bouncycastle:bcpkix-jdk15on:1.70")
                    dependency("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.4")
                    dependency("com.github.axet:kaptcha:0.0.9")
                    dependency("net.sf.ehcache:ehcache:2.10.9.2")
                    dependency("org.dhatim:fastexcel-reader:0.12.12")
                    dependency("org.apache.poi:poi-ooxml:5.1.0")
                    dependency("org.codehaus.woodstox:woodstox-core-asl:4.4.1")

                    dependency("org.apache.logging.log4j:log4j-api:2.17.1")
                    dependency("org.apache.logging.log4j:log4j-core:2.17.1")
                    dependency("org.apache.logging.log4j:log4j-to-slf4j:2.17.1")

                    dependency("com.alipay.sdk:alipay-sdk-java:4.13.58.ALL")
                    dependency("com.aliyun:aliyun-java-sdk-core:4.5.20")
                    dependency("com.aliyun:aliyun-java-sdk-dysmsapi:2.1.0")
                }
            }
        }


        project.dependencies.apply {
            add(
                "annotationProcessor",
                "org.springframework.boot:spring-boot-configuration-processor"
            )
            add("compileOnly", "org.springframework.boot:spring-boot-configuration-processor")
        }
    }
}