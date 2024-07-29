package top.bettercode.summer.gradle.plugin.project

import notEmptyDir
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin
import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.ddl.MysqlToDDL
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.util.StringUtil.toCamelCase


/**
 *
 * @author Peter Wu
 */
@Suppress("ObjectLiteralToLambda")
object RootProjectTasks {
    fun config(project: Project) {
        project.tasks.apply {

            val prefix = "jenkins"
            val jenkinsServer = project.findProperty("$prefix.server")?.toString()
            val jenkinsAuth = project.findProperty("$prefix.auth")?.toString()
            if (!jenkinsAuth.isNullOrBlank() && !jenkinsServer.isNullOrBlank()) {

                val entries =
                    project.properties.filter {
                        it.key.startsWith("$prefix.jobs.")
                                && it.key.split(".").size == 4
                    }

                val jobs = (entries.mapKeys {
                    val key = it.key.substringAfter("$prefix.jobs.")
                    val split = key.split(".")
                    val projectName = split.first()
                    val jobName = "[${split.last().toCamelCase(true)}]"
                    projectName to jobName
                }).mapValues {
                    it.value.toString()
                }.entries.groupBy { it.key.first }
                    .mapValues { it.value.map { entry -> entry.key.second to entry.value } }

                if (jobs.isNotEmpty()) {
                    val jenkins = Jenkins(jenkinsServer, jenkinsAuth)
                    if (jobs.keys.size > 1) {
                        create("buildAll") {
                            it.group = prefix
                            it.doLast(object : Action<Task> {
                                override fun execute(it: Task) {
                                    jobs.forEach { (env, jobPairs) ->
                                        jobPairs.forEach { (_, jobName) ->
                                            jenkins.build(jobName, env)
                                        }
                                    }
                                }
                            })
                        }
                    }
                    jobs.forEach { (env, jobNames) ->
                        val group = "$prefix $env"
                        if (jobNames.size > 1) {
                            create("build${env.capitalized()}") {
                                it.group = group
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        jobNames.forEach { (_, jobName) ->
                                            jenkins.build(jobName, env)
                                        }
                                    }
                                })
                            }
                        }
                        val envName = env.capitalized()
                        jobNames.forEach { (projectName, jobName) ->
                            create("build$envName$projectName") {
                                it.group = group
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        jenkins.build(jobName, env)
                                    }
                                })
                            }
                            create("last$envName$projectName") {
                                it.group = group
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        jenkins.buildInfo(jobName)
                                    }
                                })
                            }
                            create("info$envName$projectName") {
                                it.group = group
                                it.doLast(object : Action<Task> {
                                    override fun execute(it: Task) {
                                        val description = jenkins.description(jobName)
                                        project.logger.lifecycle("job 描述信息：${if (description.isNotBlank()) "\n$description" else "无"}")
                                    }
                                })
                            }
                        }
                    }
                }
            }

            create("genDbScript") { t ->
                t.group = GeneratorPlugin.GEN_GROUP
                val extension = project.extensions.getByType(GeneratorExtension::class.java)
                val databaseModules = extension.databases.keys
                if (project.rootProject.file(extension.pumlSrc).notEmptyDir()) {
                    t.dependsOn(databaseModules.map {
                        "toDDL${
                            if (GeneratorExtension.isDefaultModule(it)) "" else "[${
                                it.capitalized()
                            }]"
                        }"
                    })
                } else {
                    project.subprojects.forEach { p ->
                        if (p.file(extension.pumlSrc).notEmptyDir()) {
                            t.dependsOn(databaseModules.map {
                                "${p.name}:toDDL${
                                    if (GeneratorExtension.isDefaultModule(it)) "" else "[${
                                        it.capitalized()
                                    }]"
                                }"
                            })
                        }
                    }
                }
                t.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        extension.databases.forEach { (module, database) ->
                            val isDefault = GeneratorExtension.isDefaultModule(module)
                            val suffix = if (isDefault) "" else "-$module"
                            //init.sql
                            val destFile = FileUnit(
                                "database/init$suffix.sql"
                            )
                            destFile.apply {
                                val commentPrefix = when (database.driver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        MysqlToDDL.commentPrefix
                                    }

                                    else -> {
                                        "--"
                                    }
                                }
                                val databaseFile =
                                    project.rootProject.file("database/database$suffix.sql")
                                if (databaseFile.exists()) {
                                    +"$commentPrefix ${
                                        databaseFile.readText().trim()
                                    }"
                                    +""
                                }

                                when (database.driver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        +"$commentPrefix use ${database.schema};"
                                        +""
                                        +"SET NAMES 'utf8';"
                                        +""
                                    }

                                    else -> {}
                                }

                                val schema =
                                    project.rootProject.file("database/ddl/${if (isDefault) "schema" else module}.sql")
                                if (schema.exists()) {
                                    +schema.readText().trim()
                                    +""
                                } else {
                                    project.rootProject.file("database/ddl/${if (isDefault) "schema" else module}")
                                        .listFiles()?.filter { it.isFile }?.forEach {
                                            +it.readText().trim()
                                            +""
                                        }
                                }
                                project.rootProject.file("database/init/$suffix").listFiles()
                                    ?.filter { it.isFile }
                                    ?.forEach {
                                        +it.readText().trim()
                                        +""
                                    }
                            }
                            destFile.writeTo(project.rootDir)
                            //update.sql
                            val updateFile = FileUnit(
                                "database/update$suffix.sql"
                            )
                            updateFile.apply {
                                val commentPrefix = when (database.driver) {
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
                                    +updateDdl.readText().trim()
                                    +""
                                } else {
                                    val listFiles =
                                        project.rootProject.file("database/update/v${project.version}$suffix")
                                            .listFiles()
                                    if (listFiles.isNullOrEmpty()) {
                                        +"$commentPrefix ${database.url.substringBefore("?")}"
                                        when (database.driver) {
                                            DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                                +"$commentPrefix use ${database.schema};"
                                            }

                                            else -> {
                                            }
                                        }
                                    } else {
                                        listFiles.filter { it.isFile }.forEach {
                                            +it.readText().trim()
                                            +""
                                        }
                                    }
                                }
                                +""
                                +"".padEnd(
                                    20, when (database.driver) {
                                        DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                            '#'
                                        }

                                        else -> {
                                            '-'
                                        }
                                    }
                                )
                                +""
                                project.rootProject.file("database/update-data/v${project.version}$suffix")
                                    .listFiles()
                                    ?.filter { it.isFile }
                                    ?.forEach {
                                        +it.readText().trim()
                                        +""
                                    }
                            }
                            updateFile.writeTo(project.rootDir)

                            //test.sql
                            val testFile = FileUnit(
                                "database/test$suffix.sql"
                            )
                            testFile.apply {
                                val commentPrefix = when (database.driver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        MysqlToDDL.commentPrefix
                                    }

                                    else -> {
                                        "--"
                                    }
                                }
                                val databaseFile =
                                    project.rootProject.file("database/database$suffix.sql")
                                if (databaseFile.exists()) {
                                    +"$commentPrefix ${
                                        databaseFile.readText().trim()
                                    }"
                                    +""
                                }

                                when (database.driver) {
                                    DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                        +"$commentPrefix use ${database.schema};"
                                        +""
                                        +"SET NAMES 'utf8';"
                                        +""
                                    }

                                    else -> {}
                                }

                                project.rootProject.file("database/test/v${project.version}$suffix")
                                    .listFiles()?.filter { it.isFile }?.forEach {
                                        +it.readText().trim()
                                        +""
                                    }
                            }
                            testFile.writeTo(project.rootDir)
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