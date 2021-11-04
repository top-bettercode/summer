package top.bettercode.gradle.dist

import com.github.alexeylisyutenko.windowsserviceplugin.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.distribution.plugins.DistributionPlugin.TASK_INSTALL_NAME
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPlugin.PROCESS_RESOURCES_TASK_NAME
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class DistPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)

        project.extensions.create("dist", DistExtension::class.java)

        val windowsServiceEnable = (project.findDistProperty("windows-service.enable"))?.toBoolean()
            ?: false

        project.extensions.configure(DistExtension::class.java) {
            it.unwrapResources = project.findDistProperty("unwrap-resources")?.toBoolean() ?: true
            it.autoStart = project.findDistProperty("auto-start")?.toBoolean() ?: true
            it.includeJre = project.findDistProperty("include-jre")?.toBoolean() ?: false
            it.windows =
                if (windowsServiceEnable) windowsServiceEnable else project.findDistProperty("windows")
                    ?.toBoolean() ?: false
            it.urandom = (project.findDistProperty("urandom") ?: "false").toBoolean()
            it.x64 = project.findDistProperty("x64")?.toBoolean() ?: true
            it.nativePath = project.findDistProperty("native-path") ?: "native"
            it.runUser = project.findDistProperty("run-user") ?: ""
            it.jreWindowsI586Gz = project.findDistProperty("jre-windows-i-586-gz") ?: ""
            it.jreWindowsX64Gz = project.findDistProperty("jre-windows-x-64-gz") ?: ""
            it.jreLinuxI586Gz = project.findDistProperty("jre-linux-i-586-gz") ?: ""
            it.jreLinuxX64Gz = project.findDistProperty("jre-linux-x-64-gz") ?: ""
            it.windowsServiceOldPath = project.findDistProperty("windows-service-old-path") ?: ""
            it.distOldPath = project.findDistProperty("dist-old-path") ?: ""
            it.jvmArgs = (project.findDistProperty("jvm-args") ?: "").split(" +".toRegex())
            it.excludeUnWrapResources = (project.findDistProperty("exclude-unwrap-resources")
                ?: "META-INF/additional-spring-configuration-metadata.json,META-INF/spring.factories").split(
                ","
            )
        }

        if (windowsServiceEnable) {
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
                val dist = project.extensions.getByType(DistExtension::class.java)
                if (it.javaHome.isNullOrBlank() && dist.includeJre)
                    it.javaHome = "\"%APP_HOME%jre\""
                it.jvm = project.findDistProperty("windows-service.jvm")
                if (it.jvm.isNullOrBlank()) {
                    it.jvm = if (dist.includeJre) {
                        if (dist.x64) {
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
        project.tasks.getByName("jar") { task ->
            task.enabled = true
            task as Jar
            task.manifest {
                it.attributes(
                    mapOf(
                        "Manifest-Version" to project.version,
                        "Implementation-Title" to "${if (project != project.rootProject) "${project.rootProject.name}:" else ""}${project.name}",
                        "Implementation-Version" to project.version
                    )
                )
            }
        }



        project.tasks.getByName("compileJava") {
            it.dependsOn("processResources")
        }

        val dist = project.extensions.getByType(DistExtension::class.java)
        var jreGz = if (dist.x64) dist.jreWindowsX64Gz else dist.jreWindowsI586Gz
        if (windowsServiceEnable) {
            project.tasks.getByName(CREATE_WINDOWS_SERVICE_TASK_NAME) { task ->
                task as WindowsServicePluginTask
                task.inputs.file(project.rootProject.file("gradle.properties"))
                task.automaticClasspath =
                    project.files(task.automaticClasspath).from("%APP_HOME%\\conf")
                task.doLast {
                    val outputDirectory = task.outputDirectory
                    project.copy {
                        val destinationDir =
                            (project.tasks.getByName("processResources") as ProcessResources).destinationDir
                        it.from(destinationDir)
                        it.exclude { f ->
                            dist.excludeUnWrapResources.any {
                                f.file.absolutePath == File(
                                    destinationDir,
                                    it
                                ).absolutePath
                            }

                        }
                        it.into(File(outputDirectory, "conf").absolutePath)
                    }
                    if (dist.includeJre && jreGz.isNotBlank()) {
                        project.copy { copySpec ->
                            copySpec.from(project.tarTree(jreGz)) { spec ->
                                spec.eachFile {
                                    it.path = "jre/" + it.path.substringAfter("/")
                                }
                                spec.includeEmptyDirs = false
                            }
                            copySpec.into(outputDirectory.absolutePath)
                        }
                    }
                    val installScript = File(outputDirectory, "${project.name}-install.bat")
                    var installScriptText = installScript.readText()
                        .replace("%APP_HOME%lib\\conf", "%APP_HOME%conf")
                    if (dist.autoStart) {
                        installScriptText = installScriptText.replace(
                            "if \"%OS%\"==\"Windows_NT\" endlocal",
                            "if \"%OS%\"==\"Windows_NT\" endlocal\nnet start ${task.configuration.displayName}"
                        )
                    }
                    installScript.writeText(installScriptText)
                }
            }
            project.tasks.create("windowsServiceZip", Zip::class.java) {
                it.dependsOn(CREATE_WINDOWS_SERVICE_TASK_NAME)
                val createTask =
                    project.tasks.getByName(CREATE_WINDOWS_SERVICE_TASK_NAME) as WindowsServicePluginTask
                it.group = createTask.group
                it.from(createTask.outputDirectory)
                if (dist.includeJre)
                    it.archiveFileName.set("${project.name}-windows-${if (dist.x64) "x64" else "x86"}-${project.version}.zip")
                else
                    it.archiveFileName.set("${project.name}-windows-${project.version}.zip")
                it.destinationDirectory.set(createTask.outputDirectory.parentFile)
            }

            if (dist.windowsServiceOldPath.isNotBlank()) {
                project.tasks.create("windowsServiceUpdate") {
                    it.dependsOn(CREATE_WINDOWS_SERVICE_TASK_NAME)
                    val createTask =
                        project.tasks.getByName(CREATE_WINDOWS_SERVICE_TASK_NAME) as WindowsServicePluginTask
                    it.group = createTask.group
                    it.doLast {
                        val updateDir = File(createTask.outputDirectory.parentFile, "update")
                        require(dist.windowsServiceOldPath.isNotBlank()) { "旧版本路径不能为空" }
                        compareUpdate(
                            project,
                            updateDir,
                            project.file(dist.windowsServiceOldPath),
                            createTask.outputDirectory,
                            true
                        )
                    }
                }

                project.tasks.create("windowsServiceUpdateZip", Zip::class.java) {
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
        project.afterEvaluate {
            if (windowsServiceEnable || dist.unwrapResources) {
                project.tasks.getByName("jar") { task ->
                    task as Jar
                    task.exclude { file ->
                        val destinationDir =
                            (project.tasks.getByName(PROCESS_RESOURCES_TASK_NAME) as ProcessResources).destinationDir
                        val listFiles =
                            destinationDir.walkTopDown()
                                .filter { f ->
                                    dist.excludeUnWrapResources.none {
                                        f.absolutePath == File(destinationDir, it).absolutePath
                                    } && f.walkTopDown()
                                        .none { fi ->
                                            dist.excludeUnWrapResources.any {
                                                fi.absolutePath == File(
                                                    destinationDir,
                                                    it
                                                ).absolutePath
                                            }
                                        }
                                }
                        listFiles.contains(file.file)
                    }
                }
            }
            if (project.plugins.findPlugin(DistributionPlugin::class.java) != null) {
                val distribution = project.extensions.getByType(DistributionContainer::class.java)
                    .getAt(DistributionPlugin.MAIN_DISTRIBUTION_NAME)
                distribution.contents { copySpec ->
                    if (dist.unwrapResources) {
                        val destinationDir =
                            (project.tasks.getByName(PROCESS_RESOURCES_TASK_NAME) as ProcessResources).destinationDir
                        copySpec.from(destinationDir) { c ->
                            c.exclude { f ->
                                dist.excludeUnWrapResources.any {
                                    f.file.absolutePath == File(destinationDir, it).absolutePath
                                }
                            }
                            c.into("conf")
                        }
                    }
                    if (project.file(dist.nativePath).exists()) {
                        copySpec.from(project.file(dist.nativePath).absolutePath) {
                            it.into("native")
                        }
                    }
                    if (dist.includeJre) {
                        jreGz =
                            if (dist.windows) jreGz else (if (dist.x64) dist.jreLinuxX64Gz else dist.jreLinuxI586Gz)
                        if (jreGz.isNotBlank())
                            copySpec.from(project.tarTree(jreGz)) { spec ->
                                spec.eachFile {
                                    it.path = it.path.replace("j(dk|re).*?/".toRegex(), "jre/")
                                }
                                spec.includeEmptyDirs = false
                            }
                        distribution.distributionBaseName.set("${project.name}-${if (dist.x64) "x64" else "x86"}")
                    } else {
                        distribution.distributionBaseName.set(project.name)
                    }
                    copySpec.from(File(project.buildDir, "service").absolutePath)
                }
                if (dist.distOldPath.isNotBlank()) {
                    project.tasks.create("installDistUpdate") {
                        it.dependsOn(TASK_INSTALL_NAME)
                        val createTask = project.tasks.getByName(TASK_INSTALL_NAME)
                        it.group = createTask.group
                        it.doLast {
                            val dest = project.file("" + project.buildDir + "/install")
                            val updateDir = File(dest, "update")
                            require(dist.distOldPath.isNotBlank()) { "旧版本路径不能为空" }
                            compareUpdate(
                                project,
                                updateDir,
                                project.file(dist.distOldPath),
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
                        it.archiveFileName.set("${project.name}-${project.version}-dist_update.zip")
                        it.destinationDirectory.set(dest)
                    }
                }
            }
        }
        val jvmArgs = project.jvmArgs

        val application = project.convention.findPlugin(ApplicationPluginConvention::class.java)

        if (application != null) {
            application.applicationDefaultJvmArgs += jvmArgs
            application.applicationDefaultJvmArgs = application.applicationDefaultJvmArgs.distinct()

            project.tasks.getByName("startScripts") { task ->
                task as CreateStartScripts
                task.inputs.file(project.rootProject.file("gradle.properties"))
                if (task.mainClassName.isNullOrBlank()) {
                    task.mainClassName = project.findDistProperty("main-class-name")
                }
                if (dist.unwrapResources)
                    task.classpath = project.files(task.classpath).from("\$APP_HOME/conf")
                task.doLast {
                    it as CreateStartScripts
                    val newUnixScriptLine = mutableListOf<String>()
                    val newWindowsScriptLine = mutableListOf<String>()
                    val unixScriptLine = it.unixScript.readLines()
                    val windowsScriptLine = it.windowsScript.readLines()
                    val nativeLibArgs = project.nativeLibArgs
                    unixScriptLine.forEach { l ->
                        if (dist.unwrapResources && l.endsWith("\$APP_HOME/lib/conf")) {
                            newUnixScriptLine.add(
                                l.substring(
                                    0,
                                    l.lastIndexOf(":\$APP_HOME/lib/conf")
                                ) + ":\$APP_HOME/conf"
                            )
                        } else if (project.file(dist.nativePath).exists() && l.contains(
                                nativeLibArgs
                            )
                        ) {
                            newUnixScriptLine.add(
                                l.replace(
                                    nativeLibArgs,
                                    "-Djava.library.path=\$APP_HOME/native"
                                )
                            )
                        } else if (dist.includeJre) {
                            newUnixScriptLine.add(
                                l.replace(
                                    "APP_HOME=\"`pwd -P`\"",
                                    "APP_HOME=\"`pwd -P`\"\nJAVA_HOME=\"\$APP_HOME/jre\""
                                )
                            )
                        } else {
                            newUnixScriptLine.add(l)
                        }
                    }
                    windowsScriptLine.forEach { l ->
                        if (dist.unwrapResources && l.endsWith("%APP_HOME%\\lib\\conf")) {
                            newWindowsScriptLine.add(
                                l.substring(
                                    0,
                                    l.lastIndexOf("%APP_HOME%\\lib\\conf")
                                ) + "%APP_HOME%\\conf"
                            )
                        }
                        if (project.file(dist.nativePath).exists() && l.contains(nativeLibArgs)
                        ) {
                            newWindowsScriptLine.add(
                                l.replace(
                                    nativeLibArgs,
                                    "-Djava.library.path=%APP_HOME%\\native"
                                )
                            )
                        }
                        if (dist.includeJre) {
                            newWindowsScriptLine.add(
                                l.replace(
                                    "set APP_HOME=%DIRNAME%..",
                                    "set APP_HOME=%DIRNAME%..\r\nset JAVA_HOME=%APP_HOME%\\jre"
                                )
                            )
                        } else {
                            newWindowsScriptLine.add(l)
                        }
                    }

                    it.unixScript.printWriter().use { pw ->
                        newUnixScriptLine.forEach { l ->
                            pw.println(l)
                        }
                    }
                    it.windowsScript.printWriter().use { pw ->
                        newWindowsScriptLine.forEach { l ->
                            pw.println(l)
                        }
                    }
                    //run.sh
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
${if (dist.runUser.isNotBlank()) "User=${dist.runUser}" else ""}
ExecStart=${'$'}APP_HOME/bin/${project.name}
ExecReload=/bin/kill -HUP \${'$'}MAINPID
KillMode=/bin/kill -s QUIT \${'$'}MAINPID
Restart=always

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
            if (!oldFile.exists() || it.sha1() != oldFile.sha1()) {
                val newFile = File(updateDir, subPath)
                it.copyTo(newFile)
                newFile.setExecutable(true, false)
                newFile.setReadable(true, false)
                newFile.setWritable(true, false)
            }
        }
    }

    private fun File.sha1(): String {
        require(exists()) { "文件${absolutePath}不存在" }
        val digest = MessageDigest.getInstance("SHA-1")
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

    companion object {
        private const val CREATE_WINDOWS_SERVICE_TASK_NAME = "createWindowsService"
    }
}


