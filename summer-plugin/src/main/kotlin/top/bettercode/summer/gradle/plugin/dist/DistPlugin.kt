package top.bettercode.summer.gradle.plugin.dist

import com.github.alexeylisyutenko.windowsserviceplugin.*
import org.apache.tools.ant.taskdefs.Get
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.distribution.plugins.DistributionPlugin.TASK_INSTALL_NAME
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileTree
import org.gradle.api.internal.GradleInternal
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin.PROCESS_RESOURCES_TASK_NAME
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import top.bettercode.summer.gradle.plugin.dist.DistExtension.Companion.findDistProperty
import top.bettercode.summer.gradle.plugin.dist.DistExtension.Companion.jvmArgs
import top.bettercode.summer.tools.lang.util.OS
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
class DistPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("java")

        project.extensions.create("dist", DistExtension::class.java)


        project.extensions.configure(DistExtension::class.java) {
            it.windows = (project.findDistProperty("windows"))?.toBoolean()
                ?: OS.WINDOWS.isCurrentOs
            it.unwrapResources = project.findDistProperty("unwrap-resources")?.toBoolean() ?: true
            it.autoStart = project.findDistProperty("auto-start")?.toBoolean() ?: true
            it.includeJdk = project.findDistProperty("include-jdk")?.toBoolean() ?: false
            it.urandom = (project.findDistProperty("urandom") ?: "false").toBoolean()
            it.nativePath = project.findDistProperty("native-path") ?: "native"
            it.runUser = project.findDistProperty("run-user") ?: ""
            it.jdkArchiveSrc = project.findDistProperty("jdk-archive-src") ?: ""
            it.prevArchiveSrc = project.findDistProperty("prev-archive-src") ?: ""
            it.jvmArgs = (project.findDistProperty("jvm-args") ?: "").split(" +".toRegex())
            it.excludeUnWrapResources = (project.findDistProperty("exclude-unwrap-resources")
                ?: "META-INF/additional-spring-configuration-metadata.json,META-INF/spring.factories").split(
                ","
            )
        }
        val dist = project.extensions.getByType(DistExtension::class.java)

        val includeJdk = dist.includeJdk

        if (dist.windows) {
            project.plugins.apply(WindowsServicePlugin::class.java)
            project.extensions.configure(WindowsServicePluginConfiguration::class.java) {
                val isX64 = project.findDistProperty("x64")?.toBoolean() != false
                it.outputDir = (project.findDistProperty("windows-service.output-dir"))
                    ?: "windows-service-${if (isX64) "x64" else "x86"}/${project.name}"
                val arch = project.findDistProperty("windows-service.architecture")
                it.architecture = if (arch.isNullOrBlank()) {
                    (if (isX64) Architecture.AMD64 else Architecture.X86)
                } else Architecture.valueOf(arch)
                it.displayName = (project.findDistProperty("windows-service.display-name"))
                    ?: project.name
                it.description = (project.findDistProperty("windows-service.description"))
                    ?: project.description
                it.startClass = (project.findDistProperty("windows-service.start-class"))
                    ?: project.findDistProperty("main-class-name")
                it.startMethod = project.findDistProperty("windows-service.start-method")
                    ?: "main"
                it.startParams = project.findDistProperty("windows-service.start-params")
                    ?: "start"
                it.stopClass = (project.findDistProperty("windows-service.stop-class"))
                    ?: project.findDistProperty("main-class-name")
                it.stopMethod = project.findDistProperty("windows-service.stop-method")
                    ?: "main"
                it.stopParams = project.findDistProperty("windows-service.stop-params")
                    ?: "stop"
                val startup = project.findDistProperty("windows-service.startup")
                it.startup = if (startup.isNullOrBlank()) Startup.AUTO else Startup.valueOf(startup)
                it.interactive =
                    project.findDistProperty("windows-service.interactive")?.toBoolean()
                        ?: false
                it.dependsOn = (project.findDistProperty("windows-service.depends-on")
                    ?: "").split(";")
                it.environment = project.findDistProperty("windows-service.environment") ?: ""
                it.libraryPath = project.findDistProperty("windows-service.library-path")
                it.javaHome = project.findDistProperty("windows-service.java-home")
                if (it.javaHome.isNullOrBlank() && includeJdk)
                    it.javaHome = "\"%APP_HOME%jre\""
                it.jvm = project.findDistProperty("windows-service.jvm")
                if (it.jvm.isNullOrBlank()) {
                    it.jvm = if (includeJdk) {
                        if (dist.isX64) {
                            "\"%APP_HOME%jre\\jre\\bin\\server\\jvm.dll\""
                        } else
                            "\"%APP_HOME%jre\\bin\\client\\jvm.dll\""
                    } else {
                        "auto"
                    }
                }
                it.jvmOptions = (project.findDistProperty("windows-service.jvm-options")
                    ?: "").split(" +".toRegex())
                it.jvmOptions9 = (project.findDistProperty("windows-service.jvm-options-9")
                    ?: "").split(" +".toRegex())
                it.jvmMs = project.findDistProperty("windows-service.jvm-ms")?.toIntOrNull()
                it.jvmMx = project.findDistProperty("windows-service.jvm-mx")?.toIntOrNull()
                it.jvmSs = project.findDistProperty("windows-service.jvm-ss")?.toIntOrNull()
                it.stopTimeout =
                    project.findDistProperty("windows-service.stop-timeout")?.toIntOrNull()
                it.logPath = project.findDistProperty("windows-service.log-path") ?: "logs"
                it.logPrefix = project.findDistProperty("windows-service.log-prefix") ?: "service"
                it.logLevel = LogLevel.valueOf(
                    project.findDistProperty("windows-service.log-level")
                        ?: "INFO"
                )
                it.logJniMessages =
                    project.findDistProperty("windows-service.log-jni-messages")?.toIntOrNull()
                it.stdOutput = project.findDistProperty("windows-service.std-output")
                it.stdError = project.findDistProperty("windows-service.std-error")
                it.pidFile = project.findDistProperty("windows-service.pid-file")
                it.serviceUser = project.findDistProperty("windows-service.service-user")
                it.servicePassword = project.findDistProperty("windows-service.service-password")
            }
        }


        project.tasks.apply {

            named("jar") { task ->
                task.enabled = true
            }

            named("compileJava") {
                it.dependsOn(PROCESS_RESOURCES_TASK_NAME)
            }

            if (dist.windows) {
                val createWindowsServiceTaskName =
                    WindowsServicePlugin.getCREATE_WINDOWS_SERVICE_TASK_NAME()
                named(createWindowsServiceTaskName) { task ->
                    task as WindowsServicePluginTask

                    task.inputs.file(project.rootProject.file("gradle.properties"))
                    task.automaticClasspath =
                        project.files(task.automaticClasspath).from("%APP_HOME%\\conf")
                    task.doLast {
                        val outputDirectory = task.outputDirectory

                        if (dist.unwrapResources)
                            project.copy { spec ->
                                spec.from(File(project.buildDir, "conf").absolutePath)
                                spec.into(File(outputDirectory, "conf").absolutePath)
                            }


                        if (includeJdk) {
                            project.copy { copySpec ->
                                includeJdk(copySpec, dist, project)
                            }
                        }
                        val installScript = File(outputDirectory, "${project.name}-install.bat")
                        var installScriptText = installScript.readText()
                            .replace("%APP_HOME%lib\\conf;", "%APP_HOME%conf;")
                        if (dist.autoStart) {
                            installScriptText = installScriptText.replace(
                                "if \"%OS%\"==\"Windows_NT\" endlocal",
                                "if \"%OS%\"==\"Windows_NT\" endlocal\nnet start ${task.configuration.displayName}"
                            )
                        }
                        installScript.writeText(installScriptText)
                    }
                }
                create("windowsServiceZip", Zip::class.java) {
                    it.dependsOn(createWindowsServiceTaskName)
                    val createTask =
                        project.tasks.getByName(createWindowsServiceTaskName) as WindowsServicePluginTask
                    it.group = createTask.group
                    it.from(createTask.outputDirectory)
                    if (includeJdk)
                        it.archiveFileName.set("${project.name}-windows-${if (dist.isX64) "x64" else "x86"}-${project.version}.zip")
                    else
                        it.archiveFileName.set("${project.name}-windows-${project.version}.zip")
                    it.destinationDirectory.set(createTask.outputDirectory.parentFile)
                }

                if (dist.prevArchiveSrc.isNotBlank()) {
                    create("windowsServiceUpdate") {
                        it.dependsOn(createWindowsServiceTaskName)
                        val createTask =
                            project.tasks.getByName(createWindowsServiceTaskName) as WindowsServicePluginTask
                        it.group = createTask.group
                        it.doLast {
                            val updateDir =
                                File(createTask.outputDirectory.parentFile, "update")
                            compareUpdate(
                                project,
                                updateDir,
                                project.file(dist.prevArchiveSrc),
                                createTask.outputDirectory,
                                true
                            )
                        }
                    }

                    create("windowsServiceUpdateZip", Zip::class.java) {
                        it.dependsOn("windowsServiceUpdate")
                        val createTask =
                            project.tasks.getByName("createWindowsService") as WindowsServicePluginTask
                        it.group = createTask.group
                        val updateDir = File(createTask.outputDirectory.parentFile, "update")
                        it.from(updateDir)
                        it.archiveFileName.set("${project.name}-windows-update-${project.version}.zip")
                        it.destinationDirectory.set(createTask.outputDirectory.parentFile)
                    }
                }
            }

        }

        project.afterEvaluate {
            val gradle = project.gradle as GradleInternal
            val needUnwrapTasks = mutableListOf<Task>()
            lateinit var currentProject: Project
            gradle.allprojects {
                if (it.projectDir.absolutePath == gradle.startParameter.currentDir.absolutePath)
                    currentProject = it
                needUnwrapTasks.addAll(it.tasks.filter { task ->
                    task.group == "distribution" ||
                            task.group == WindowsServicePlugin.getPLUGIN_GROUP()
                })
            }
            project.rootProject.allprojects { p ->
                p.tasks.named("jar") { task ->
                    task as Jar
                    if (dist.unwrapResources) {
                        val taskNames =
                            gradle.startParameter.taskNames.map {
                                var name = it
                                if (currentProject != project.rootProject) name =
                                    currentProject.tasks.getByName(name).path
                                if (name.startsWith(":")) name else ":$name"
                            }

                        val needUnwrapTask =
                            needUnwrapTasks.find {
                                taskNames.contains(it.path)
                            }
                        if (needUnwrapTask != null) {
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
                            task.doLast {
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
                                                needUnwrapTask.project.buildDir,
                                                "conf"
                                            ).absolutePath
                                        )
                                    }
                                }
                            }
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
                        copySpec.from(File(project.buildDir, "conf").absolutePath) {
                            it.into("conf")
                        }

                    if (project.file(dist.nativePath).exists()) {
                        copySpec.from(project.file(dist.nativePath).absolutePath) {
                            it.into("native")
                        }
                    }
                    if (includeJdk) {
                        includeJdk(copySpec, dist, project)
                        distribution.distributionBaseName.set("${project.name}-${if (dist.isX64) "x64" else "x86"}")
                    } else {
                        distribution.distributionBaseName.set(project.name)
                    }
                    copySpec.from(File(project.buildDir, "service").absolutePath)
                }

                if (dist.prevArchiveSrc.isNotBlank()) {
                    project.tasks.create("installDistUpdate") {
                        it.dependsOn(TASK_INSTALL_NAME)
                        val createTask = project.tasks.getByName(TASK_INSTALL_NAME)
                        it.group = createTask.group
                        it.doLast {
                            val dest = project.file("" + project.buildDir + "/install")
                            val updateDir = File(dest, "update")
                            compareUpdate(
                                project,
                                updateDir,
                                project.file(dist.prevArchiveSrc),
                                File(dest, distribution.distributionBaseName.get()),
                                false
                            )
                        }
                    }

                    project.tasks.create("installDistUpdateZip", Zip::class.java) {
                        it.dependsOn("installDistUpdate")
                        val createTask = project.tasks.getByName("installDistUpdate")
                        it.group = createTask.group
                        val dest = project.file("" + project.buildDir + "/install")
                        val updateDir = File(dest, "update")
                        it.from(updateDir)
                        it.archiveFileName.set("${project.name}-${project.version}-dist-update.zip")
                        it.destinationDirectory.set(dest)
                    }
                }
            }
        }
        val jvmArgs = project.jvmArgs

        val application = project.extensions.findByType(JavaApplication::class.java)

        if (application != null) {
            application.applicationDefaultJvmArgs += jvmArgs
            application.applicationDefaultJvmArgs = application.applicationDefaultJvmArgs.distinct()

            project.tasks.getByName("startScripts") { task ->
                task as CreateStartScripts
                task.unixStartScriptGenerator =
                    StartScript.startScriptGenerator(project, dist, false, includeJdk)
                task.windowsStartScriptGenerator =
                    StartScript.startScriptGenerator(project, dist, true, includeJdk)

                task.inputs.file(project.rootProject.file("gradle.properties"))
                if (!task.mainClass.isPresent) {
                    task.mainClass.set(project.findDistProperty("main-class-name"))
                }
                task.doLast { //run.sh
                    writeServiceFile(
                        project, "run.sh", """
            #!/usr/bin/env sh
            
            # Attempt to set APP_HOME
            # Resolve links: ${'$'}0 may be a link
            PRG="${'$'}0"
            # Need this for relative symlinks.
            while [ -h "${'$'}PRG" ] ; do
                ls=`ls -ld "${'$'}PRG"`
                link=`expr "${'$'}ls" : '.*-> \(.*\)${'$'}'`
                if expr "${'$'}link" : '/.*' > /dev/null; then
                    PRG="${'$'}link"
                else
                    PRG=`dirname "${'$'}PRG"`"/${'$'}link"
                fi
            done
            SAVED="`pwd`"
            cd "`dirname \"${'$'}PRG\"`/" >/dev/null
            APP_HOME="`pwd -P`"
            
            cd ${'$'}APP_HOME
            mkdir -p "${'$'}APP_HOME/logs"
            ${'$'}APP_HOME/bin/${project.name}
            """
                    )

                    //startup.sh
                    writeServiceFile(
                        project, "startup.sh", """
            #!/usr/bin/env sh
            
            # Attempt to set APP_HOME
            # Resolve links: ${'$'}0 may be a link
            PRG="${'$'}0"
            # Need this for relative symlinks.
            while [ -h "${'$'}PRG" ] ; do
                ls=`ls -ld "${'$'}PRG"`
                link=`expr "${'$'}ls" : '.*-> \(.*\)${'$'}'`
                if expr "${'$'}link" : '/.*' > /dev/null; then
                    PRG="${'$'}link"
                else
                    PRG=`dirname "${'$'}PRG"`"/${'$'}link"
                fi
            done
            SAVED="`pwd`"
            cd "`dirname \"${'$'}PRG\"`/" >/dev/null
            APP_HOME="`pwd -P`"
            
            cd ${'$'}APP_HOME
            mkdir -p "${'$'}APP_HOME/logs"
            nohup "${'$'}APP_HOME/bin/${project.name}" 1>/dev/null 2>"${'$'}APP_HOME/logs/error.log" &
            ps ax|grep ${'$'}APP_HOME/ |grep -v grep|awk '{ print ${'$'}1 }'
            """
                    )

                    //shutdown.sh
                    writeServiceFile(
                        project, "shutdown.sh", """
            #!/usr/bin/env sh
            
            # Attempt to set APP_HOME
            # Resolve links: ${'$'}0 may be a link
            PRG="${'$'}0"
            # Need this for relative symlinks.
            while [ -h "${'$'}PRG" ] ; do
                ls=`ls -ld "${'$'}PRG"`
                link=`expr "${'$'}ls" : '.*-> \(.*\)${'$'}'`
                if expr "${'$'}link" : '/.*' > /dev/null; then
                    PRG="${'$'}link"
                else
                    PRG=`dirname "${'$'}PRG"`"/${'$'}link"
                fi
            done
            SAVED="`pwd`"
            cd "`dirname \"${'$'}PRG\"`/" >/dev/null
            APP_HOME="`pwd -P`"
            
            pid="`ps ax|grep ${'$'}APP_HOME/ |grep -v grep|awk '{ print ${'$'}1 }'`"
            if [ -n "${'$'}pid" ]
            then
                echo "${'$'}pid" |while read id
                do
                kill -9 ${'$'}id
                echo "${'$'}id"
                done
            fi
            """
                    )
                    //${project.name}-install
                    writeServiceFile(
                        project, "${project.name}-install", """
            #!/usr/bin/env sh
            
            # Attempt to set APP_HOME
            # Resolve links: ${'$'}0 may be a link
            PRG="${'$'}0"
            # Need this for relative symlinks.
            while [ -h "${'$'}PRG" ] ; do
                ls=`ls -ld "${'$'}PRG"`
                link=`expr "${'$'}ls" : '.*-> \(.*\)${'$'}'`
                if expr "${'$'}link" : '/.*' > /dev/null; then
                    PRG="${'$'}link"
                else
                    PRG=`dirname "${'$'}PRG"`"/${'$'}link"
                fi
            done
            SAVED="`pwd`"
            cd "`dirname \"${'$'}PRG\"`/" >/dev/null
            APP_HOME="`pwd -P`"
            
            if [ -z "${'$'}(whereis systemctl | cut -d':' -f2)" ]; then
              (
                cat <<EOF
            #!/usr/bin/env sh
            #chkconfig: 2345 80 90
            #description:auto_run
            
            case "\${'$'}1" in
              start)
                    # Start daemon.
                    echo "Starting ${project.name}";
                    ${'$'}APP_HOME/startup.sh
                    ;;
              stop)
                    # Stop daemons.
                    echo "Shutting down ${project.name}";
                    ${'$'}APP_HOME/shutdown.sh
                    ;;
              restart)
                    \${'$'}0 stop
                    sleep 2
                    \${'$'}0 start
                    ;;
              *)
                    echo \${'$'}"Usage: \${'$'}0 {start|stop|restart}"
                    exit 1
                    ;;
            esac
            
            exit 0
            EOF
              ) | sudo tee /etc/init.d/${project.name}
              sudo chmod +x /etc/init.d/${project.name}
              sudo chkconfig ${project.name} on
              ${
                            if (dist.autoStart) """
              sudo service ${project.name} start
              """.trimIndent() else ""
                        }
            else
              (
                cat <<EOF
            [Unit]
            Description=${project.name}
            After=network.target
            
            [Service]
            ${if (dist.runUser.isNotBlank()) "User=${dist.runUser}" else "User=`whoami`"}
            Type=forking
            ExecStart=${'$'}APP_HOME/startup.sh
            ExecReload=/bin/kill -HUP \${'$'}MAINPID
            KillMode=/bin/kill -s QUIT \${'$'}MAINPID
            
            [Install]
            WantedBy=multi-user.target
            EOF
              ) | sudo tee /etc/systemd/system/${project.name}.service
              sudo systemctl daemon-reload
              sudo systemctl enable ${project.name}.service
              ${
                            if (dist.autoStart) """
              sudo systemctl start ${project.name}.service
              """.trimIndent() else ""
                        }
            fi
            """
                    )

                    //${project.name}-uninstall
                    writeServiceFile(
                        project, "${project.name}-uninstall", """
            #!/usr/bin/env sh
            
            if [ -z "${'$'}(whereis systemctl | cut -d':' -f2)" ]; then
              sudo service ${project.name} stop
              sudo chkconfig ${project.name} off
              sudo rm -f /etc/init.d/${project.name}
            else
              sudo systemctl stop ${project.name}.service
              sudo systemctl disable ${project.name}.service
              sudo rm -f /etc/systemd/system/${project.name}.service
            fi
            """
                    )
                }
            }
        }

        project.tasks.getByName("test") { task ->
            task as Test
            if (application != null)
                task.jvmArgs = application.applicationDefaultJvmArgs.toList()
            else
                task.jvmArgs = jvmArgs.toList()
        }
    }

    private fun includeJdk(
        copySpec: CopySpec,
        dist: DistExtension,
        project: Project
    ) {
        copySpec.from(jdkArchive(dist, project)) { spec ->
            spec.eachFile {
                it.path = it.path.replace("j(dk|re|ava).*?/".toRegex(), "jre/")
            }
            spec.includeEmptyDirs = false
            spec.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }

    private fun jdkArchive(dist: DistExtension, project: Project): FileTree {
        val jdkArchive = dist.jdkArchive
        if (!jdkArchive.exists()) {
            val get = Get()
            get.project = project.ant.project
            val url = URL(dist.jdkArchiveSrc)
            get.setVerbose(true)
            get.setSkipExisting(true)
            println("download:${url}")
            get.doGet(
                url, jdkArchive, 2,
                Get.VerboseProgress(System.out)
            )
        }
        return if (jdkArchive.extension == "zip") project.zipTree(jdkArchive) else project.tarTree(
            jdkArchive
        )
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

    private fun writeServiceFile(
        project: Project,
        fileName: String,
        text: String,
        executable: Boolean = true
    ) {
        val serviceScript = File(project.buildDir, "service/$fileName")
        if (!serviceScript.parentFile.exists()) {
            serviceScript.parentFile.mkdirs()
        }
        serviceScript.printWriter().use {
            it.println(text.trimIndent())
        }
        if (executable)
            serviceScript.setExecutable(true, false)
    }
}


