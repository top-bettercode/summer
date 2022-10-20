package plugin

import org.gradle.api.Project
import top.bettercode.generator.DatabaseDriver
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.ddl.MysqlToDDL
import top.bettercode.generator.dom.unit.FileUnit
import top.bettercode.gradle.generator.GeneratorPlugin


/**
 *
 * @author Peter Wu
 */
object RootProjectTasks {

    fun config(project: Project) {
        project.tasks.apply {
            val jenkinsJobs = project.findProperty("jenkins.jobs")?.toString()?.split(",")
                ?.filter { it.isNotBlank() }
            val jenkinsDevJobs = project.findProperty("jenkins.dev.jobs")?.toString()?.split(",")
                ?.filter { it.isNotBlank() }
            val jenkinsTestJobs = project.findProperty("jenkins.test.jobs")?.toString()?.split(",")
                ?.filter { it.isNotBlank() }
            val jenkinsOtherJobs =
                project.findProperty("jenkins.other.jobs")?.toString()?.split(",")
                    ?.filter { it.isNotBlank() }

            val jobs = mutableMapOf<String, List<String>>()
            if (!jenkinsJobs.isNullOrEmpty()) {
                jobs["default"] = jenkinsJobs
            }
            if (!jenkinsDevJobs.isNullOrEmpty()) {
                jobs["dev"] = jenkinsDevJobs
            }
            if (!jenkinsTestJobs.isNullOrEmpty()) {
                jobs["test"] = jenkinsTestJobs
            }
            if (!jenkinsOtherJobs.isNullOrEmpty()) {
                jobs["other"] = jenkinsOtherJobs
            }

            val jenkinsServer = project.findProperty("jenkins.server")?.toString()
            val jenkinsAuth = project.findProperty("jenkins.auth")?.toString()
            if (jobs.isNotEmpty() && !jenkinsAuth.isNullOrBlank() && !jenkinsServer.isNullOrBlank()) {
                val jenkins = Jenkins(jenkinsServer, jenkinsAuth)
                create("build[All]") {
                    it.group = "jenkins"
                    it.doLast {
                        jobs.forEach { (env, jobNames) ->
                            jobNames.forEach { jobName ->
                                jenkins.build(jobName, env)
                            }
                        }
                    }
                }
                jobs.forEach { (env, jobNames) ->
                    if (env != "default") {
                        create("build[$env]") {
                            it.group = "jenkins"
                            it.doLast {
                                jobNames.forEach { jobName ->
                                    jenkins.build(jobName, env)
                                }
                            }
                        }
                    }
                    jobNames.forEach { jobName ->
                        val jobTaskName = jobName.replace(
                            "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                            ""
                        )
                        val envName = if (env == "default") "" else "[$env]"
                        create("build$envName[$jobTaskName]") {
                            it.group = "jenkins"
                            it.doLast {
                                jenkins.build(jobName, env)
                            }
                        }
                        create("lastBuildInfo$envName[$jobTaskName]") {
                            it.group = "jenkins"
                            it.doLast {
                                jenkins.buildInfo(jobName)
                            }
                        }
                        create("description$envName[$jobTaskName]") {
                            it.group = "jenkins"
                            it.doLast {
                                val description = jenkins.description(jobName)
                                println("job 描述信息：${if (description.isNotBlank()) "\n$description" else "无"}")
                            }
                        }
                    }
                }
            }

            create("gen[DbScript]") { t ->
                t.group = GeneratorPlugin.genGroup
                t.doLast {
                    val destFile = FileUnit(
                        "database/init.sql"
                    )
                    destFile.apply {
                        val gen = project.extensions.getByType(GeneratorExtension::class.java)
                        val commentPrefix = when (gen.defaultDatasource.databaseDriver) {
                            DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                MysqlToDDL.commentPrefix
                            }

                            else -> {
                                "--"
                            }
                        }
                        +"$commentPrefix ${
                            project.rootProject.file("database/database.sql").readText()
                        }"

                        when (gen.defaultDatasource.databaseDriver) {
                            DatabaseDriver.MYSQL, DatabaseDriver.MARIADB -> {
                                +""
                                +"SET NAMES 'utf8';"
                                +""
                            }

                            else -> {}
                        }

                        val mysqlSecuritySchema =
                            project.rootProject.file("database/security/security-mysql-schema.sql")
                        if (gen.defaultDatasource.databaseDriver == DatabaseDriver.MYSQL && mysqlSecuritySchema.exists()) {
                            +mysqlSecuritySchema.readText()
                            +""
                        }

                        val oracleSecuritySchema =
                            project.rootProject.file("database/security/security-oracle-schema.sql")
                        if (gen.defaultDatasource.databaseDriver == DatabaseDriver.ORACLE && oracleSecuritySchema.exists()) {
                            +oracleSecuritySchema.readText()
                            +""
                        }

                        project.rootProject.file("database/ddl").listFiles()?.filter { it.isFile }
                            ?.forEach {
                                +it.readText()
                            }
                        project.rootProject.file("database/init").listFiles()?.filter { it.isFile }
                            ?.forEach {
                                +it.readText()
                            }
                    }
                    destFile.writeTo(project.rootDir)
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

    }
}