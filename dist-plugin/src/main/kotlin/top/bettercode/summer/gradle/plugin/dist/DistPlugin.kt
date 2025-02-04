package top.bettercode.summer.gradle.plugin.dist

import org.apache.tools.ant.taskdefs.Get
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.distribution.plugins.DistributionPlugin.TASK_INSTALL_NAME
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.GradleInternal
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin.PROCESS_RESOURCES_TASK_NAME
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import top.bettercode.summer.gradle.plugin.dist.DistExtension.Companion.findDistProperty
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
@Suppress("ObjectLiteralToLambda")
class DistPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("java")

        project.extensions.create("dist", DistExtension::class.java)


        project.extensions.configure(DistExtension::class.java) { dist ->
            dist.windows = (project.findDistProperty("windows"))?.toBoolean()
                ?: Os.isFamily(Os.FAMILY_WINDOWS)
            dist.unwrapResources = project.findDistProperty("unwrap-resources")?.toBoolean() ?: true
            dist.autoStart = project.findDistProperty("auto-start")?.toBoolean() ?: true
            dist.includeJdk = project.findDistProperty("include-jdk")?.toBoolean() ?: false
            dist.urandom = (project.findDistProperty("urandom") ?: "false").toBoolean()
            dist.nativePath = project.findDistProperty("native-path") ?: "native"
            dist.runUser = project.findDistProperty("run-user") ?: ""
            dist.jdkArchiveSrc = project.findDistProperty("jdk-archive-src") ?: ""
            dist.prevArchiveSrc = project.findDistProperty("prev-archive-src") ?: ""
            dist.jvmArgs = (project.findDistProperty("jvm-args")
                ?: "").split(" +".toRegex()).filter { it.isNotBlank() }
            dist.excludeUnWrapResources = (project.findDistProperty("exclude-unwrap-resources")
                ?: "META-INF/additional-spring-configuration-metadata.json,META-INF/spring.factories").split(
                ","
            )

        }
        val dist = project.extensions.getByType(DistExtension::class.java)

        val includeJdk = dist.includeJdk

        project.tasks.apply {

            named("jar") { task ->
                task.enabled = true
            }

            named("compileJava") {
                it.dependsOn(PROCESS_RESOURCES_TASK_NAME)
            }

        }

        project.afterEvaluate {
            val gradle = project.gradle as GradleInternal
            val distributionTasks = mutableListOf<Task>()
            lateinit var currentProject: Project
            gradle.allprojects {
                if (it.projectDir.absolutePath == gradle.startParameter.currentDir.absolutePath)
                    currentProject = it
                distributionTasks.addAll(it.tasks.filter { task ->
                    task.group == "distribution"
                })
            }
            val taskNames =
                gradle.startParameter.taskNames.map {
                    var name = it
                    if (currentProject != project.rootProject) name =
                        currentProject.tasks.getByName(name).path
                    if (name.startsWith(":")) name else ":$name"
                }

            val distributionTask =
                distributionTasks.find {
                    taskNames.contains(it.path) || taskNames.contains(":" + it.name)
                }
            project.rootProject.allprojects { p ->
                p.tasks.named("jar") { task ->
                    task as Jar
                    if (dist.unwrapResources) {
                        if (distributionTask != null) {
                            task.outputs.upToDateWhen { false }
                            val resources = mutableMapOf<String, String>()
                            task.exclude { file ->
                                @Suppress("UnstableApiUsage") val destinationDir =
                                    (p.tasks.getByName(PROCESS_RESOURCES_TASK_NAME) as ProcessResources).destinationDir
                                if (file.file.absolutePath.startsWith(destinationDir.absolutePath)) {
                                    val fileParentPath = destinationDir.absolutePath + "/"
                                    if (!file.isDirectory) {
                                        val exclude =
                                            !dist.excludeUnWrapResources.contains(file.path)
                                        if (exclude) resources[file.file.absolutePath] =
                                            if (file.file.parentFile == destinationDir) "" else
                                                file.file.parentFile.absolutePath.substringAfter(
                                                    fileParentPath
                                                )
                                        exclude
                                    } else {
                                        var exclude = true
                                        file.file.walkTopDown().filter { it.isFile }.forEach {
                                            val path = it.path.substringAfter(fileParentPath)
                                            val contains =
                                                dist.excludeUnWrapResources.contains(path)
                                            if (contains) {
                                                exclude = false
                                            } else {
                                                resources[it.absolutePath] =
                                                    if (it.parentFile == destinationDir) "" else
                                                        it.parentFile.absolutePath.substringAfter(
                                                            fileParentPath
                                                        )
                                            }
                                        }

                                        exclude
                                    }
                                } else {
                                    false
                                }
                            }
                            task.doLast(object : Action<Task> {
                                override fun execute(it: Task) {
                                    if (resources.isNotEmpty()) {
                                        p.copy { spec ->
                                            resources.forEach { (filePath, to) ->
                                                spec.from(filePath) {
                                                    if (to.isNotBlank())
                                                        it.into(to)
                                                }
                                            }
                                            spec.into(
                                                File(
                                                    distributionTask.project.layout.buildDirectory.get().asFile,
                                                    "conf"
                                                ).absolutePath
                                            )
                                        }
                                    }
                                }
                            })
                        }
                    }
                    task.manifest {
                        it.attributes(
                            mapOf(
                                "Manifest-Version" to p.version,
                                "Implementation-Title" to "${p.rootProject.name}${p.path}",
                                "Implementation-Version" to p.version
                            )
                        )
                    }
                }
            }

            if (project.plugins.findPlugin(DistributionPlugin::class.java) != null) {
                val distribution = project.extensions.getByType(DistributionContainer::class.java)
                    .getAt(DistributionPlugin.MAIN_DISTRIBUTION_NAME)
                distribution.contents { copySpec ->

                    if (dist.unwrapResources)
                        copySpec.from(
                            File(
                                project.layout.buildDirectory.get().asFile,
                                "conf"
                            ).absolutePath
                        ) {
                            it.into("conf")
                        }

                    if (project.file(dist.nativePath).exists()) {
                        copySpec.from(project.file(dist.nativePath).absolutePath) {
                            it.into("native")
                        }
                    }
                    if (includeJdk && distributionTask != null) {
                        includeJre(copySpec, dist, project)
                    }
                    distribution.distributionBaseName.set(project.name)

                    copySpec.from(
                        File(
                            project.layout.buildDirectory.get().asFile,
                            "service"
                        ).absolutePath
                    )
                }

                if (dist.prevArchiveSrc.isNotBlank()) {
                    project.tasks.create("installDistUpdate") {
                        it.dependsOn(TASK_INSTALL_NAME)
                        val createTask = project.tasks.getByName(TASK_INSTALL_NAME)
                        it.group = createTask.group
                        it.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                val dest =
                                    project.file("" + project.layout.buildDirectory.get().asFile + "/install")
                                val updateDir = File(dest, "update")
                                compareUpdate(
                                    project,
                                    updateDir,
                                    project.file(dist.prevArchiveSrc),
                                    File(dest, distribution.distributionBaseName.get()),
                                    false
                                )
                            }
                        })
                    }

                    project.tasks.create("installDistUpdateZip", Zip::class.java) {
                        it.dependsOn("installDistUpdate")
                        val createTask = project.tasks.getByName("installDistUpdate")
                        it.group = createTask.group
                        val dest =
                            project.file("" + project.layout.buildDirectory.get().asFile + "/install")
                        val updateDir = File(dest, "update")
                        it.from(updateDir)
                        it.archiveFileName.set("${project.name}-${project.version}-dist-update.zip")
                        it.destinationDirectory.set(dest)
                    }
                }
            }
            val jvmArgs = dist.jvmArgs(project)

            val application = project.extensions.findByType(JavaApplication::class.java)

            if (application != null) {
                application.applicationDefaultJvmArgs += jvmArgs
                application.applicationDefaultJvmArgs =
                    application.applicationDefaultJvmArgs.distinct()
                val mainClassName = project.findDistProperty("main-class-name")
                if (!mainClassName.isNullOrBlank()) {
                    application.mainClass.set(mainClassName)
                }
                val includeNative = true
                project.tasks.getByName("startScripts") { task ->
                    task as CreateStartScripts
                    task.unixStartScriptGenerator =
                        StartScript.startScriptGenerator(
                            project,
                            dist,
                            false,
                            includeJdk,
                            includeNative
                        )
                    task.windowsStartScriptGenerator =
                        StartScript.startScriptGenerator(
                            project,
                            dist,
                            true,
                            includeJdk,
                            includeNative
                        )

                    task.inputs.file(project.rootProject.file("gradle.properties"))
                    if (!task.mainClass.isPresent) {
                        task.mainClass.set(mainClassName)
                    }
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            StartScriptsExt.ext(project, dist)
                        }
                    })
                }
            }

            project.tasks.getByName("test") { task ->
                task as Test
                if (application != null)
                    task.jvmArgs = application.applicationDefaultJvmArgs.toList()
                else
                    task.jvmArgs = jvmArgs.toList()
                //    在 AIX 上，设置环境变量 LIBPATH
                //    在 GNU/Linux 上，设置环境变量 LD_LIBRARY_PATH
                //    在 MacOS 上，设置环境变量 DYLD_LIBRARY_PATH
                //    在 Windows 上，设置环境变量 PATH
                if (Os.isFamily(Os.FAMILY_UNIX))
                    task.environment("LD_LIBRARY_PATH", dist.nativePath(project).apply {
                        project.file(this).let {
                            if (!it.exists()) it.mkdirs()
                        }
                    })

            }

            project.tasks.withType(JavaExec::class.java) { task ->
                if (application != null)
                    task.jvmArgs = application.applicationDefaultJvmArgs.toList()
                else
                    task.jvmArgs = jvmArgs.toList()

                if (Os.isFamily(Os.FAMILY_UNIX))
                    task.environment("LD_LIBRARY_PATH", dist.nativePath(project).apply {
                        project.file(this).let {
                            if (!it.exists()) it.mkdirs()
                        }
                    })
            }


        }
    }

    private fun includeJre(
        copySpec: CopySpec,
        dist: DistExtension,
        project: Project
    ) {
        val jdkArchive = dist.jdkArchive
        if (!jdkArchive.exists()) {
            val get = Get()
            get.project = project.ant.project
            val url = URL(dist.jdkArchiveSrc)
            get.setVerbose(true)
            get.setSkipExisting(true)
            project.logger.lifecycle("download:${url}")
            get.doGet(
                url, jdkArchive, 2,
                Get.VerboseProgress(System.out)
            )
        }
        project.logger.lifecycle("packaging jdk:$jdkArchive")
        copySpec.from(
            if (jdkArchive.extension == "zip")
                project.zipTree(jdkArchive)
            else
                project.tarTree(jdkArchive)
        ) { spec ->
            spec.eachFile {
                val dir = it.sourcePath.substringBefore("/")
                it.path = if (it.path.contains("/$dir/"))
                    it.path.replaceFirst("/$dir/", "/jdk/")
                else
                    it.path.replaceFirst("$dir/", "jdk/")
            }
            spec.includeEmptyDirs = false
            spec.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }

    private fun compareUpdate(
        project: Project,
        updateDir: File,
        oldDir: File,
        newDir: File,
        isWindows: Boolean
    ) {
        updateDir.deleteRecursively()
        updateDir.mkdirs()
        val olds = oldDir.walkTopDown().filter { it.isFile }
        val news = newDir.walkTopDown().filter { it.isFile }
        val deleteFileList = File(updateDir, "deleteFileList.txt")
        val updateFile = File(updateDir, "update.${if (isWindows) "bat" else "sh"}")
        updateFile.printWriter().use { up ->
            if (isWindows) {
                up.println("net stop ${project.name}")
                up.println("call ${project.name}-uninstall.bat")
            } else {
                up.println("#!/usr/bin/env sh")
                up.println()
                up.println("sh shutdown.sh")
            }
            deleteFileList.printWriter().use { pw ->
                olds.forEach {
                    val subPath =
                        it.absolutePath.substringAfter(oldDir.absolutePath + File.separator)
                    val newFile = File(newDir, subPath)
                    if (!newFile.exists()) {
                        pw.println(subPath)
                        if (isWindows) {
                            up.println("del /F $subPath")
                        } else {
                            up.println("rm -fv $subPath")
                        }
                    }
                }
            }
            if (isWindows) {
                up.println("call ${project.name}-install.bat")
                up.println("net start ${project.name}")
            } else {
                up.println("chmod +x bin/${project.name}")
                up.println("sh startup.sh")
            }
        }
        if (isWindows) {
            updateFile.writeText(updateFile.readText().replace("\n", "\r\n"))
        }
        updateFile.setExecutable(true, false)
        updateFile.setReadable(true, false)
        updateFile.setWritable(true, false)
        news.forEach {
            val subPath = it.absolutePath.substringAfter(newDir.absolutePath + File.separator)
            val oldFile = File(oldDir, subPath)
            if (!oldFile.exists() || it.sha256() != oldFile.sha256()) {
                val newFile = File(updateDir, subPath)
                it.copyTo(newFile)
                newFile.setExecutable(true, false)
                newFile.setReadable(true, false)
                newFile.setWritable(true, false)
            }
        }
    }

    private fun File.sha256(): String {
        require(exists()) { "文件${absolutePath}不存在" }
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(this.readBytes())
        return BigInteger(1, digest.digest()).toString(16)
    }


}


