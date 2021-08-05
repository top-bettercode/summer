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
import profilesActive
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
        project.extensions.configure(DistExtension::class.java) {
            it.unwrapResources = findProperty(project, "unwrap-resources")?.toBoolean() ?: true
            it.includeJre = findProperty(project, "include-jre")?.toBoolean() ?: false
            it.windows = findProperty(project, "windows")?.toBoolean() ?: false
            it.x64 = findProperty(project, "x64")?.toBoolean() ?: true
            it.nativePath = findProperty(project, "native-path") ?: "native"
            it.runUser = findProperty(project, "run-user") ?: ""
            it.jreWindowsI586Gz = findProperty(project, "jre-windows-i-586-gz") ?: ""
            it.jreWindowsX64Gz = findProperty(project, "jre-windows-x-64-gz") ?: ""
            it.jreLinuxI586Gz = findProperty(project, "jre-linux-i-586-gz") ?: ""
            it.jreLinuxX64Gz = findProperty(project, "jre-linux-x-64-gz") ?: ""
            it.windowsServiceOldPath = findProperty(project, "windows-service-old-path") ?: ""
            it.distOldPath = findProperty(project, "dist-old-path") ?: ""
            it.jvmArgs = (findProperty(project, "jvm-args") ?: "").split(" +".toRegex())
            it.excludeUnWrapResources = (findProperty(project, "exclude-unwrap-resources")
                ?: "META-INF/additional-spring-configuration-metadata.json,META-INF/spring.factories").split(
                ","
            )
        }
        val windowsServiceEnable = (findProperty(project, "windows-service.enable"))?.toBoolean()
            ?: false
        if (windowsServiceEnable) {
            project.plugins.apply(WindowsServicePlugin::class.java)
            project.extensions.configure(WindowsServicePluginConfiguration::class.java) {
                val isX64 = findProperty(project, "x64")?.toBoolean() != false
                it.outputDir = (findProperty(project, "windows-service.output-dir"))
                    ?: "windows-service-${if (isX64) "x64" else "x86"}/${project.name}"
                val arch = findProperty(project, "windows-service.architecture")
                it.architecture = if (arch.isNullOrBlank()) {
                    (if (isX64) Architecture.AMD64 else Architecture.X86)
                } else Architecture.valueOf(arch)
                it.displayName = (findProperty(project, "windows-service.display-name"))
                    ?: project.name
                it.description = (findProperty(project, "windows-service.description"))
                    ?: project.description
                it.startClass = (findProperty(project, "windows-service.start-class"))
                    ?: findProperty(project, "main-class-name")
                it.startMethod = findProperty(project, "windows-service.start-method")
                    ?: "main"
                it.startParams = findProperty(project, "windows-service.start-params")
                    ?: "start"
                it.stopClass = (findProperty(project, "windows-service.stop-class"))
                    ?: findProperty(project, "main-class-name")
                it.stopMethod = findProperty(project, "windows-service.stop-method")
                    ?: "main"
                it.stopParams = findProperty(project, "windows-service.stop-params")
                    ?: "stop"
                val startup = findProperty(project, "windows-service.startup")
                it.startup = if (startup.isNullOrBlank()) Startup.AUTO else Startup.valueOf(startup)
                it.interactive = findProperty(project, "windows-service.interactive")?.toBoolean()
                    ?: false
                it.dependsOn = (findProperty(project, "windows-service.depends-on")
                    ?: "").split(";")
                it.environment = findProperty(project, "windows-service.environment") ?: ""
                it.libraryPath = findProperty(project, "windows-service.library-path")
                it.javaHome = findProperty(project, "windows-service.java-home")
                val dist = project.extensions.getByType(DistExtension::class.java)
                if (it.javaHome.isNullOrBlank() && dist.includeJre)
                    it.javaHome = "\"%APP_HOME%jre\""
                it.jvm = findProperty(project, "windows-service.jvm")
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
                it.jvmOptions = (findProperty(project, "windows-service.jvm-options")
                    ?: "").split(" +".toRegex())
                it.jvmOptions9 = (findProperty(project, "windows-service.jvm-options-9")
                    ?: "").split(" +".toRegex())
                it.jvmMs = findProperty(project, "windows-service.jvm-ms")?.toIntOrNull()
                it.jvmMx = findProperty(project, "windows-service.jvm-mx")?.toIntOrNull()
                it.jvmSs = findProperty(project, "windows-service.jvm-ss")?.toIntOrNull()
                it.stopTimeout =
                    findProperty(project, "windows-service.stop-timeout")?.toIntOrNull()
                it.logPath = findProperty(project, "windows-service.log-path") ?: "logs"
                it.logPrefix = findProperty(project, "windows-service.log-prefix") ?: "service"
                it.logLevel = LogLevel.valueOf(
                    findProperty(project, "windows-service.log-level")
                        ?: "INFO"
                )
                it.logJniMessages =
                    findProperty(project, "windows-service.log-jni-messages")?.toIntOrNull()
                it.stdOutput = findProperty(project, "windows-service.std-output")
                it.stdError = findProperty(project, "windows-service.std-error")
                it.pidFile = findProperty(project, "windows-service.pid-file")
                it.serviceUser = findProperty(project, "windows-service.service-user")
                it.servicePassword = findProperty(project, "windows-service.service-password")
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
                    if (dist.includeJre) {
                        project.copy { copySpec ->
                            copySpec.from(project.tarTree(if (dist.x64) dist.jreWindowsX64Gz else dist.jreWindowsI586Gz)) { spec ->
                                spec.eachFile {
                                    it.path = "jre/" + it.path.substringAfter("/")
                                }
                                spec.includeEmptyDirs = false
                            }
                            copySpec.into(outputDirectory.absolutePath)
                        }
                    }
                    val installScript = File(outputDirectory, "${project.name}-install.bat")
                    val installScriptText = installScript.readText()
                        .replace("%APP_HOME%lib\\conf", "%APP_HOME%conf").replace(
                            "if \"%OS%\"==\"Windows_NT\" endlocal",
                            "if \"%OS%\"==\"Windows_NT\" endlocal\nnet start ${task.configuration.displayName}"
                        )
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
                        copySpec.from(project.tarTree(if (dist.windows) (if (dist.x64) dist.jreWindowsX64Gz else dist.jreWindowsI586Gz) else (if (dist.x64) dist.jreLinuxX64Gz else dist.jreLinuxI586Gz))) { spec ->
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
        val jvmArgs = dist.jvmArgs.filter { it.isNotBlank() }.toMutableSet()
        val encoding = "-Dfile.encoding=UTF-8"
        jvmArgs += encoding
        if (project.extensions.findByName("profile") != null)
            jvmArgs += "-Dspring.profiles.active=${project.profilesActive}"
        val nativeLibArgs = if (project.file(dist.nativePath).exists()) {
            val nativeLibArgs =
                "-Djava.library.path=${project.file(dist.nativePath).absolutePath}"
            jvmArgs += nativeLibArgs
            nativeLibArgs
        } else ""

        val application = project.convention.findPlugin(ApplicationPluginConvention::class.java)

        if (application != null) {
            application.applicationDefaultJvmArgs += jvmArgs
            application.applicationDefaultJvmArgs =
                application.applicationDefaultJvmArgs.distinct()

            project.tasks.getByName("startScripts") { task ->
                task as CreateStartScripts
                task.inputs.file(project.rootProject.file("gradle.properties"))
                if (task.mainClassName.isNullOrBlank()) {
                    task.mainClassName = findProperty(project, "main-class-name")
                }
                if (dist.unwrapResources)
                    task.classpath = project.files(task.classpath).from("\$APP_HOME/conf")
                task.doLast {
                    it as CreateStartScripts
                    val newUnixScriptLine = mutableListOf<String>()
                    val newWindowsScriptLine = mutableListOf<String>()
                    val unixScriptLine = it.unixScript.readLines()
                    val windowsScriptLine = it.windowsScript.readLines()
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
  sudo service ${project.name} start
  sudo chkconfig ${project.name} on
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
  sudo systemctl start ${project.name}.service
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

    private fun findProperty(project: Project, key: String) =
        (project.findProperty("dist.${project.name}.$key") as? String
            ?: project.findProperty("dist.$key") as? String)

    companion object {
        private const val CREATE_WINDOWS_SERVICE_TASK_NAME = "createWindowsService"
    }
}


