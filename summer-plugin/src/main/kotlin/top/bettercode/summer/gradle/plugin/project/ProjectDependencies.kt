package top.bettercode.summer.gradle.plugin.project

import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.generator.ProjectUtil.isCloud
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
object ProjectDependencies {

    private val summerVersion = ProjectPlugin::class.java.`package`.implementationVersion
    private val summerVersionConfig = ResourceBundle.getBundle("summer-version")
    private val oracleJdbcVersion = summerVersionConfig.getString("oracle-jdbc.version")
    private val alibabaCloudVersion = summerVersionConfig.getString("alibaba-cloud.version")
    private val springCloudVersion = summerVersionConfig.getString("spring-cloud.version")

    fun config(project: Project) {
        project.configurations.apply {
            if ("false" != project.findProperty("dependencies.disable-cache"))
                all {
                    it.resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
                }
        }

        project.extensions.configure(StandardDependencyManagementExtension::class.java) { ext ->
            ext.imports {
                it.mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
                if (project.isCloud) {
                    it.mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:${alibabaCloudVersion}")
                    it.mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
                }
            }


            ext.dependencies {
                it.apply {
                    dependency("top.bettercode.summer:tools:$summerVersion")
                    dependency("top.bettercode.summer:excel:$summerVersion")
                    dependency("top.bettercode.summer:ueditor:$summerVersion")
                    dependency("top.bettercode.summer:weixin:$summerVersion")
                    dependency("top.bettercode.summer:sms:$summerVersion")
                    dependency("top.bettercode.summer:mobile-query:$summerVersion")
                    dependency("top.bettercode.summer:rapidauth:$summerVersion")
                    dependency("top.bettercode.summer:weather:$summerVersion")
                    dependency("top.bettercode.summer:jpush:$summerVersion")
                    dependency("top.bettercode.summer:qvod:$summerVersion")
                    dependency("top.bettercode.summer:sap:$summerVersion")

                    dependency("top.bettercode.summer:env:$summerVersion")
                    dependency("top.bettercode.summer:web:$summerVersion")
                    dependency("top.bettercode.summer:data-jpa:$summerVersion")
                    dependency("top.bettercode.summer:security:$summerVersion")
                    dependency("top.bettercode.summer:test:$summerVersion")

                    dependency("com.oracle.database.jdbc:ojdbc8:$oracleJdbcVersion")
                    dependency("com.oracle.database.jdbc:ucp:$oracleJdbcVersion")
                    dependency("com.oracle.database.security:oraclepki:$oracleJdbcVersion")
                    dependency("com.oracle.database.security:osdt_core:$oracleJdbcVersion")
                    dependency("com.oracle.database.security:osdt_cert:$oracleJdbcVersion")

                    dependency("org.bouncycastle:bcprov-jdk18on:1.72")

                    dependency("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.4")
                    dependency("com.github.axet:kaptcha:0.0.9")
                    dependency("org.dhatim:fastexcel-reader:0.13.0")
                    dependency("org.apache.poi:poi-ooxml:5.2.3")
                    dependency("org.codehaus.woodstox:woodstox-core-asl:4.4.1")

                    dependency("org.apache.logging.log4j:log4j-api:2.19.0")
                    dependency("org.apache.logging.log4j:log4j-core:2.19.0")
                    dependency("org.apache.logging.log4j:log4j-to-slf4j:2.19.0")
                }
            }
        }


        project.dependencies.apply {
            add(
                "annotationProcessor",
                "org.springframework.boot:spring-boot-configuration-processor"
            )
            add("compileOnly", "org.springframework.boot:spring-boot-configuration-processor")
            add("compileOnly", "com.google.code.findbugs:annotations:3.0.1")
        }
    }
}