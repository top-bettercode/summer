package top.bettercode.summer.gradle.plugin.project

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin
import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.ddl.MysqlToDDL
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.lang.capitalized


/**
 *
 * @author Peter Wu
 */
object RootProjectTasks {

    fun config(project: Project) {
        project.tasks.apply {

            val prefix = "jenkins"
            val entries = project.properties.filter { it.key.startsWith("$prefix.") && it.key.endsWith(".jobs") }

            val jobs = ((if (project.properties.containsKey("$prefix.jobs")) mapOf("default" to entries["$prefix.jobs"]
            ) else emptyMap()) + entries.filter { it.key.split('.').size == 3 }.mapKeys {
                it.key.substringAfter("$prefix.").substringBefore(".")
            }).mapValues {
                it.value.toString().split(",")
                        .filter { s -> s.isNotBlank() }.distinct()
            }

            val jenkinsServer = project.findProperty("$prefix.server")?.toString()
            val jenkinsAuth = project.findProperty("$prefix.auth")?.toString()
            if (jobs.isNotEmpty() && !jenkinsAuth.isNullOrBlank() && !jenkinsServer.isNullOrBlank()) {
                val jenkins = Jenkins(jenkinsServer, jenkinsAuth)
                create("buildAll") {
                    it.group = prefix
                    it.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            jobs.forEach { (env, jobNames) ->
                                jobNames.forEach { jobName ->
                                    jenkins.build(jobName, env)
                                }
                            }
                        }
                    })
                }
                jobs.forEach { (env, jobNames) ->
                    if (env != "default") {
                        create("build${env.capitalized()}") {
                            it.group = prefix
                            it.doLast(object : Action<Task> {
                                override fun execute(it: Task) {
                                    jobNames.forEach { jobName ->
                                        jenkins.build(jobName, env)
                                    }
                                }
                            })
                        }
                    }
                    if (env in arrayOf("default", "dev", "test", "other")) {
                        jobNames.forEach { jobName ->
                            val jobTaskName = jobName.replace(
                                    "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                                    ""
                            ).capitalized()
                            val envName = if (env == "default") "" else env.capitalized()
                            create("build$envName$jobTaskName") {
                                it.group = prefix
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        jenkins.build(jobName, env)
                                    }
                                })
                            }
                            create("lastBuildInfo$envName$jobTaskName") {
                                it.group = prefix
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        jenkins.buildInfo(jobName)
                                    }
                                })
                            }
                            create("description$envName$jobTaskName") {
                                it.group = prefix
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        val description = jenkins.description(jobName)
                                        println("job 描述信息：${if (description.isNotBlank()) "\n$description" else "无"}")
                                    }
                                })
                            }
                        }
                    }
                }
            }

            create("genDbScript") { t ->
                t.group = GeneratorPlugin.genGroup
                t.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        gen.run { module, _ ->
                            val datasource = gen.datasource(module)

                            val isDefault = gen.isDefaultModule(module)
                            val suffix = if (isDefault) "" else "-$module"
                            //init.sql
                            val destFile = FileUnit(
                                    "database/init$suffix.sql"
                            )
                            destFile.apply {
                                val commentPrefix = when (datasource.databaseDriver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        MysqlToDDL.commentPrefix
                                    }

                                    else -> {
                                        "--"
                                    }
                                }
                                val databaseFile = project.rootProject.file("database/database$suffix.sql")
                                if (databaseFile.exists()) {
                                    +"$commentPrefix ${
                                        databaseFile.readText()
                                    }"
                                }

                                when (datasource.databaseDriver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        +"$commentPrefix use ${datasource.schema};"
                                        +""
                                        +"SET NAMES 'utf8';"
                                        +""
                                    }

                                    else -> {}
                                }

                                if (isDefault) {
                                    val mysqlSecuritySchema =
                                            project.rootProject.file("database/security/security-mysql-schema.sql")
                                    if (datasource.databaseDriver == DatabaseDriver.MYSQL && mysqlSecuritySchema.exists()) {
                                        +mysqlSecuritySchema.readText()
                                        +""
                                    }

                                    val oracleSecuritySchema =
                                            project.rootProject.file("database/security/security-oracle-schema.sql")
                                    if (datasource.databaseDriver == DatabaseDriver.ORACLE && oracleSecuritySchema.exists()) {
                                        +oracleSecuritySchema.readText()
                                        +""
                                    }
                                }

                                val schema = project.rootProject.file("database/ddl/${if (isDefault) "schema" else module}.sql")
                                if (schema.exists()) {
                                    +schema.readText()
                                    +""
                                }
                                project.rootProject.file("database/init/$suffix").listFiles()
                                        ?.filter { it.isFile }
                                        ?.forEach {
                                            +it.readText()
                                        }
                            }
                            destFile.writeTo(project.rootDir)
                            //update.sql
                            val updateFile = FileUnit(
                                    "database/update$suffix.sql"
                            )
                            updateFile.apply {
                                val commentPrefix = when (datasource.databaseDriver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        MysqlToDDL.commentPrefix
                                    }

                                    else -> {
                                        "--"
                                    }
                                }

                                val updateDdl =
                                        project.rootProject.file("database/update/v${project.version}$suffix.sql")
                                if (updateDdl.exists()) {
                                    +updateDdl.readText()
                                } else {
                                    +"$commentPrefix ${datasource.url.substringBefore("?")}"
                                    when (datasource.databaseDriver) {
                                        DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                            +"$commentPrefix use ${datasource.schema};"
                                        }

                                        else -> {
                                        }
                                    }
                                }
                                +""
                                project.rootProject.file("database/update-data/v${project.version}$suffix").listFiles()
                                        ?.filter { it.isFile }
                                        ?.forEach {
                                            +it.readText()
                                        }
                            }
                            updateFile.writeTo(project.rootDir)
                        }
                    }
                })
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

    }
}