import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.InnerInterface
import cn.bestwu.generator.dom.java.element.Interface
import cn.bestwu.generator.dom.java.element.JavaVisibility
import cn.bestwu.generator.dom.java.element.TopLevelClass
import cn.bestwu.generator.dsl.Generators
import cn.bestwu.generator.puml.PumlConverter
import hudson.cli.CLI
import org.atteo.evo.inflector.English
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import profilesActive

/**
 *
 * @author Peter Wu
 */
class ProjectPlugin : Plugin<Project> {

    private val pluginBundle: ResourceBundle = ResourceBundle.getBundle("summer-version")

    override fun apply(project: Project) {

        project.description = project.findProperty("application.name") as? String

        project.allprojects.forEach { subProject ->
            subProject.plugins.apply("org.jetbrains.kotlin.jvm")
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

            subProject.repositories.apply {
                mavenLocal()
                maven { it.setUrl("https://maven.aliyun.com/repository/public") }
                mavenCentral()
                maven {
                    it.setUrl("http://maven.wintruelife.com/nexus/content/repositories/snapshots")
                    it.isAllowInsecureProtocol = true
                }

                maven { it.setUrl("https://oss.jfrog.org/oss-snapshot-local") }
            }

            subProject.tasks.apply {
                named("build") {
                    it.setDependsOn(listOf("testClasses"))
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

            create("dbMerge") {
                it.group = "gen"
                it.doLast {
                    val destFile: File = project.rootProject.file("database/init.sql")
                    val initBuilder = StringBuilder()
                    initBuilder.appendLine("SET NAMES 'utf8';")
//                    initBuilder.appendLine(project.rootProject.file("database/database.sql").readText())
                    project.rootProject.file("database/ddl").listFiles()?.filter { it.isFile }
                        ?.forEach {
                            initBuilder.appendLine(it.readText())
                        }
                    project.rootProject.file("database/init").listFiles()?.filter { it.isFile }
                        ?.forEach {
                            initBuilder.appendLine(it.readText())
                        }
                    destFile.writeText(initBuilder.toString())
                }
            }
        }


        project.subprojects {
            val subProject = it

            val mainProject =
                !arrayOf("core").contains(subProject.name) && subProject.parent?.name != "util" && subProject.name != "util"
            val needDocProject = subProject.parent?.name != "util" && subProject.name != "util"

            subProject.plugins.apply {
                apply("org.jetbrains.kotlin.jvm")
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
                    apply("org.springframework.boot")
                    apply("application")
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
                all {
                    it.resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
                }
            }

            subProject.extensions.configure(io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension::class.java) { ext ->
                ext.imports {
                    it.mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
                }

                ext.dependencies {
                    val summerVersion = pluginBundle.getString("summer.version")
                    it.dependency("cn.bestwu.wechat:weixin-mp:0.9.7")
                    it.dependency("cn.bestwu.wechat:weixin-app:0.9.7")
                    it.dependency("com.alipay.sdk:alipay-sdk-java:3.4.49.ALL")
                    it.dependency("com.aliyun:aliyun-java-sdk-core:4.2.2")
                    it.dependency("com.aliyun:aliyun-java-sdk-dysmsapi:1.1.0")
                    it.dependency("com.oracle.database.jdbc:ojdbc8:21.1.0.0")
                    it.dependency("jakarta.persistence:jakarta.persistence-api:2.2.3")

                    it.dependency("org.bouncycastle:bcpkix-jdk15on:1.62")
                    it.dependency("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.2")
                    it.dependency("com.github.axet:kaptcha:0.0.9")
                    it.dependency("net.sf.ehcache:ehcache:2.10.6")

                    it.dependency("cn.bestwu.summer:api-sign:$summerVersion")
                    it.dependency("cn.bestwu.summer:common-lang:$summerVersion")
                    it.dependency("cn.bestwu.summer:starter-logging:$summerVersion")
                    it.dependency("cn.bestwu.summer:autodoc-gen:$summerVersion")
                    it.dependency("cn.bestwu.summer:excel:$summerVersion")

                    it.dependency("cn.bestwu.summer:web:$summerVersion")
                    it.dependency("cn.bestwu.summer:data-jpa:$summerVersion")
                    it.dependency("cn.bestwu.summer:data-mybatis:$summerVersion")
                    it.dependency("cn.bestwu.summer:security-server:$summerVersion")
                    it.dependency("cn.bestwu.summer:security-resource:$summerVersion")

                    it.dependency("org.dhatim:fastexcel-reader:0.12.9")
                    it.dependency("org.apache.poi:poi-ooxml:4.0.0")
                    it.dependency("org.codehaus.woodstox:woodstox-core-asl:4.4.1")
                    it.dependency("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.2.5.RELEASE")
                    it.dependency("org.springframework.security.oauth:spring-security-oauth2:2.4.0.RELEASE")

                }
            }

            subProject.dependencies.apply {
                add(
                    "annotationProcessor",
                    "org.springframework.boot:spring-boot-configuration-processor"
                )
                add("compileOnly", "org.springframework.boot:spring-boot-configuration-processor")

                if ("release" != subProject.profilesActive)
                    add("implementation", "org.springframework.boot:spring-boot-starter-websocket")
            }

            subProject.tasks.apply {
                named("test", Test::class.java) {
                    it.useJUnitPlatform()
                }

                named("compileJava", JavaCompile::class.java) {
                    it.options.compilerArgs.add("-Xlint:unchecked")
                    it.options.encoding = "UTF-8"
                }
                if (mainProject) {
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

            if (subProject.name == project.findProperty("tools.project") ?: "core") {
                subProject.tasks.apply {
                    create("genSerializationViews") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            val tableNames =
                                (Generators.tableNames(gen) + gen.tableNames).distinct()
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
                            subProject.file(
                                "src/main/java/${
                                    type.fullyQualifiedName.replace(
                                        '.',
                                        '/'
                                    )
                                }.java"
                            ).writeText(serializationViews.formattedContent)
                        }
                    }
                    create("printMapper") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(MapperPrint())
                            Generators.call(gen)
                        }
                    }
                    create("printMybatisWhere") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(MybatisWherePrint())
                            Generators.call(gen)
                        }
                    }

                    create("printSetter") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(SetterPrint(false))
                            Generators.call(gen)
                        }
                    }

                    create("printSetter2") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(SetterPrint(true))
                            Generators.call(gen)
                        }
                    }


                    create("printExcelField") {
                        it.group = "gen"
                        it.doLast {
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(ExcelFieldPrint())
                            Generators.call(gen)
                        }
                    }
                    create("dbDoc") {
                        it.group = "gen"
                        it.doLast {
                            val dbDoc = DbDoc(subProject)
                            val gen =
                                subProject.extensions.getByType(GeneratorExtension::class.java)
                            gen.generators = arrayOf(dbDoc)
                            gen.tableNames = arrayOf()
                            Generators.call(gen)
                        }
                    }
                    create("dicCode") {
                        it.group = "gen"
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
                    create("genErrorCode") {
                        it.group = "gen"
                        it.doLast {
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