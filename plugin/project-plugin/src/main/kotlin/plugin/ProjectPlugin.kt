package plugin

import hudson.cli.CLI
import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import org.atteo.evo.inflector.English
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.InnerInterface
import top.bettercode.generator.dom.java.element.Interface
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.generator.dsl.Generators
import top.bettercode.generator.puml.PumlConverter
import top.bettercode.gradle.dist.jvmArgs
import top.bettercode.gradle.generator.GeneratorPlugin
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * @author Peter Wu
 */
val Project.mainProject: Boolean
    get() = !arrayOf("core").contains(name) && parent?.name != "util" && name != "util"

class ProjectPlugin : Plugin<Project> {

    val kotlinVersionConfig: ResourceBundle = ResourceBundle.getBundle("kotlin-version")
    private val kotlinVersion =
        kotlinVersionConfig.getString("kotlin.version")
    private val kotlinCoroutinesVersion = kotlinVersionConfig.getString("kotlin-coroutines.version")


    override fun apply(project: Project) {

        project.description = project.findProperty("application.name") as? String

        project.allprojects.forEach { subProject ->
            subProject.plugins.apply("idea")
            subProject.plugins.apply("java")

            subProject.group = subProject.properties["app.packageName"] as String
            subProject.version = subProject.properties["app.version"] as String

//            idea
            subProject.extensions.configure(org.gradle.plugins.ide.idea.model.IdeaModel::class.java) { idea ->
                idea.module {
                    it.inheritOutputDirs = false
                    it.isDownloadJavadoc = false
                    it.isDownloadSources = true
                    val convention = subProject.convention.getPlugin(
                        JavaPluginConvention::class.java
                    )
                    it.outputDir = convention.sourceSets
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.outputDir
                    it.testOutputDir = convention.sourceSets
                        .getByName(SourceSet.TEST_SOURCE_SET_NAME).java.outputDir
                }
            }

//            java
            subProject.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) { java ->
                java.sourceCompatibility = JavaVersion.VERSION_1_8
                java.targetCompatibility = JavaVersion.VERSION_1_8
            }

            val mainProject = subProject.mainProject
            val needDocProject = subProject.parent?.name != "util" && subProject.name != "util"

            subProject.plugins.apply {
                apply("summer.profile")
                apply("summer.packageinfo")
                apply("io.spring.dependency-management")
            }

            if (needDocProject) {
                subProject.plugins.apply {
                    apply("summer.generator")
                    apply("summer.autodoc")
                }
            }
            if (mainProject) {
                subProject.plugins.apply {
                    apply("application")
                    apply("org.springframework.boot")
                    apply("summer.dist")
                }
            }


            subProject.configurations.apply {
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

            subProject.extensions.configure(StandardDependencyManagementExtension::class.java) { ext ->
                ext.imports {
                    it.mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
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

                        dependency("top.bettercode.summer:test:$summerVersion")

                        dependency("top.bettercode.wechat:weixin-mp:0.9.7")
                        dependency("top.bettercode.wechat:weixin-app:0.9.7")
                        dependency("com.oracle.database.jdbc:ojdbc8:21.4.0.0.1")
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


            subProject.dependencies.apply {
                add(
                    "annotationProcessor",
                    "org.springframework.boot:spring-boot-configuration-processor"
                )
                add("compileOnly", "org.springframework.boot:spring-boot-configuration-processor")
            }

            subProject.tasks.apply {
                val jvmArgs = subProject.jvmArgs

                val application =
                    subProject.convention.findPlugin(ApplicationPluginConvention::class.java)
                if (application != null) {
                    application.applicationDefaultJvmArgs += jvmArgs
                    application.applicationDefaultJvmArgs =
                        application.applicationDefaultJvmArgs.distinct()
                }

                named("test", Test::class.java) {
                    it.useJUnitPlatform()
                    if (application != null)
                        it.jvmArgs = application.applicationDefaultJvmArgs.toList()
                    else
                        it.jvmArgs = jvmArgs.toList()
                }

                named("build") {
                    it.setDependsOn(listOf("testClasses"))
                }

                named("compileJava", JavaCompile::class.java) {
                    it.options.compilerArgs.add("-Xlint:unchecked")
                    it.options.compilerArgs.add("-parameters")
                    it.options.encoding = "UTF-8"
                }

                if (mainProject) {
                    create("resolveMainClass") {
                        it.doLast {
                            subProject.tasks.findByName("startScripts").apply {
                                this as CreateStartScripts
                                if (mainClassName.isNullOrBlank()) {
                                    val bootJar = subProject.tasks.getByName("bootJar")
                                    bootJar as BootJar
                                    mainClassName = bootJar.mainClassName
                                }
                            }
                        }
                    }
                    named("startScripts", CreateStartScripts::class.java) { scripts ->
                        scripts.dependsOn("resolveMainClass")
                    }
                    named("bootRun", BootRun::class.java) {
                        System.getProperties().forEach { t, u ->
                            it.systemProperty(t as String, u)
                        }
                    }
                    named("bootJar", BootJar::class.java) {
                        it.launchScript()
                    }
                    named("distZip", Zip::class.java) {
                        it.archiveFileName.set("${subProject.name}.zip")
                    }
                }

                if (needDocProject && !mainProject) {
                    named("asciidoc") { it.enabled = false }
                    named("htmldoc") { it.enabled = false }
                    named("postman") { it.enabled = false }
                }
            }
        }

        project.tasks.apply {
            val jenkinsJobs = project.findProperty("jenkins.jobs")?.toString()?.split(",")
                ?.filter { it.isNotBlank() }
            val jenkinsServer = project.findProperty("jenkins.server")?.toString()
            val jenkinsAuth = project.findProperty("jenkins.auth")?.toString()
            if (!jenkinsJobs.isNullOrEmpty() && !jenkinsAuth.isNullOrBlank() && !jenkinsServer.isNullOrBlank()) {
                create("jenkins[All]") {
                    it.group = "tool"
                    it.doLast {
                        jenkinsJobs.forEach { jobName ->
                            CLI._main(
                                arrayOf(
                                    "-s",
                                    jenkinsServer,
                                    "-auth",
                                    jenkinsAuth,
                                    "build",
                                    jobName,
                                    "-s",
                                    "-v"
                                )
                            )
                        }
                    }
                }
                jenkinsJobs.forEach { jobName ->
                    val jobTaskName = jobName.replace(
                        "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                        ""
                    )
                    create("jenkins[$jobTaskName]") {
                        it.group = "tool"
                        it.doLast {
                            CLI._main(
                                arrayOf(
                                    "-s",
                                    jenkinsServer,
                                    "-auth",
                                    jenkinsAuth,
                                    "build",
                                    jobName,
                                    "-s",
                                    "-v"
                                )
                            )
                        }
                    }
                }
            }

            create("genDbScript") { t ->
                t.group = GeneratorPlugin.taskGroup
                t.doLast {
                    val destFile: File = project.rootProject.file("database/init.sql")
                    val initBuilder = StringBuilder()
                    initBuilder.appendln("SET NAMES 'utf8';")
//                    initBuilder.appendln(project.rootProject.file("database/database.sql").readText())
                    project.rootProject.file("database/ddl").listFiles()?.filter { it.isFile }
                        ?.forEach {
                            initBuilder.appendln(it.readText())
                        }
                    project.rootProject.file("database/init").listFiles()?.filter { it.isFile }
                        ?.forEach {
                            initBuilder.appendln(it.readText())
                        }
                    destFile.writeText(initBuilder.toString())
                }
            }

            create("prettyConfig") { t ->
                t.doLast {
                    ConfigTool.prettyConfig(
                        project.file("conf"),
                        project.subprojects.map { it.file("src/main/resources/application.yml") }
                            .filter { it.exists() })
                }
            }
        }

        project.subprojects { subProject ->
            if (subProject.name == (project.findProperty("tools.project") ?: "core")) {
                subProject.tasks.apply {
                    create("genSerializationViews") { t ->
                        t.group = GeneratorPlugin.taskGroup
                        t.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            val tableNames =
                                (Generators.tableNames(gen) + gen.tableNames).sortedBy { it }
                                    .distinct()
                            val type =
                                JavaType("${if (gen.projectPackage) "${gen.packageName}.${gen.projectName}" else gen.packageName}.web.CoreSerializationViews")
                            val serializationViews = Interface(type).apply {
                                javadoc {
                                    +"/**"
                                    +" * 模型属性 json SerializationViews"
                                    +" */"
                                }
                                this.visibility = JavaVisibility.PUBLIC
                                tableNames.forEach {
                                    val pathName = English.plural(gen.className(it))
                                    innerInterface(InnerInterface(JavaType("Get${pathName}List")))
                                    innerInterface(InnerInterface(JavaType("Get${pathName}Info")))
                                }
                            }
                            val file = subProject.file(
                                "src/main/java/${
                                    type.fullyQualifiedName.replace(
                                        '.',
                                        '/'
                                    )
                                }.java"
                            )
                            if (!file.parentFile.exists()) {
                                file.parentFile.mkdirs()
                            }
                            file.writeText(serializationViews.formattedContent)
                        }
                    }
                    create("printMapper") {
                        it.group = GeneratorPlugin.taskGroup
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(MapperPrint())
                            Generators.call(gen)
                        }
                    }
                    create("printMybatisWhere") {
                        it.group = GeneratorPlugin.taskGroup
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(MybatisWherePrint())
                            Generators.call(gen)
                        }
                    }

                    create("printSetter") {
                        it.group = GeneratorPlugin.taskGroup
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(SetterPrint(true))
                            Generators.call(gen)
                        }
                    }

                    create("printExcelField") {
                        it.group = GeneratorPlugin.taskGroup
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(ExcelFieldPrint())
                            Generators.call(gen)
                        }
                    }
                    create("genDbDoc") {
                        it.group = GeneratorPlugin.taskGroup
                        it.doLast {
                            val dbDoc = DbDoc(subProject)
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(dbDoc)
                            gen.tableNames = arrayOf()
                            Generators.call(gen)
                        }
                    }
                    create("genDicCode") {
                        it.group = GeneratorPlugin.taskGroup
                        it.doLast {
                            val propertiesFile =
                                subProject.file("src/main/resources/default-dic-code.properties")
                            propertiesFile.parentFile.mkdirs()
                            propertiesFile.writeText("")
                            val codeTypes: MutableSet<String> = mutableSetOf()
                            //生成 properties
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            PumlConverter.reformat(gen)
                            gen.generators =
                                arrayOf(DicCodeProperties(propertiesFile, codeTypes))
                            gen.tableNames = arrayOf()
                            Generators.call(gen)
                            //生成
                            val dicCodeGen = DicCodeGen(subProject)
                            dicCodeGen.setUp()
                            dicCodeGen.genCode()
                            dicCodeGen.tearDown()
                        }
                    }
                    create("genErrorCode") { t ->
                        t.group = GeneratorPlugin.taskGroup
                        t.doLast {
                            val file = subProject.file("src/main/resources/error-code.properties")
                            if (file.exists()) {
                                val gen =
                                    subProject.extensions.getByType(GeneratorExtension::class.java)
                                val destFile = subProject.file(
                                    "src/main/java/${
                                        gen.packageName.replace(
                                            '.',
                                            '/'
                                        )
                                    }/support/ErrorCode.java"
                                )
                                val clazz =
                                    TopLevelClass(JavaType("${gen.packageName}.support.ErrorCode"))

                                clazz.visibility = JavaVisibility.PUBLIC
                                clazz.apply {
                                    javadoc {
                                        +"/**"
                                        +" * 业务错误码"
                                        +" */"
                                    }
                                    val properties = Properties()
                                    properties.load(file.inputStream())
                                    properties.forEach { k, v ->
                                        field(
                                            "CODE_$k",
                                            JavaType.stringInstance,
                                            "\"$k\"",
                                            true,
                                            JavaVisibility.PUBLIC
                                        ) {
                                            isStatic = true
                                            javadoc {
                                                +"/**"
                                                +" * $v"
                                                +" */"
                                            }
                                        }
                                    }
                                    subProject.rootProject.file("doc/业务相关错误码.adoc").printWriter()
                                        .use {
                                            it.println(
                                                """
== 业务相关错误码

当处理建议为空时，取 message 字段内容提示用户即可。

|===
| 业务相关错误码 | 处理建议 | 说明 |
                            """.trimIndent()
                                            )
                                            properties.forEach { k, v ->
                                                it.println("| $k | $v |  |")
                                            }
                                            it.println("|===")
                                        }
                                }
                                destFile.writeText(clazz.formattedContent)
                            }
                        }
                    }
                }
            }
        }
    }
}