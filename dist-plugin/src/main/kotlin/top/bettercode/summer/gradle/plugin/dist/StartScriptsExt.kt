package top.bettercode.summer.gradle.plugin.dist

import org.gradle.api.Project
import java.io.File

/**
 *
 * @author Peter Wu
 */
object StartScriptsExt {

    fun ext(project: Project, dist: DistExtension) {
        val appName =
            (if (project.rootProject != project) "${project.rootProject.name}-" else "") + project.name

        writeServiceFile(
            project, "build.txt", """${appName}-${project.version}"""
        )
        if (dist.windows) {
            //WinSW
            val winSWFile =
                File(project.layout.buildDirectory.get().asFile, "service/${project.name}.exe")
            DistPlugin::class.java.getResourceAsStream("/WinSW.NET461.exe")
                ?.copyTo(winSWFile.apply { parentFile.mkdirs() }.outputStream())

            writeServiceFile(
                project, "${project.name}.xml", """
<service>
  <id>$appName</id>
  <name>${project.rootProject.description ?: appName}</name>
  <description>${project.rootProject.description ?: appName}</description>
  <executable>%BASE%\bin\${project.name}.bat</executable>
  
  <startmode>${if (dist.autoStart) "Automatic" else "Manual"}</startmode>
  <waithint>3 sec</waithint>
  <stoptimeout>2 sec</stoptimeout>
  <log mode="none"/>
</service>
            """, executable = false
            )
            writeServiceFile(
                project, "run.bat", """
@if "%DEBUG%"=="" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.\

@rem Execute app
%DIRNAME%bin\${project.name}.bat

pause
            """, executable = true
            )

            writeServiceFile(
                project, "startup.bat", """
@if "%DEBUG%"=="" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.\

@rem start app
%DIRNAME%${project.name}.exe start
            """, executable = true
            )

            writeServiceFile(
                project, "shutdown.bat", """
@if "%DEBUG%"=="" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.\

@rem stop app
%DIRNAME%${project.name}.exe stop
            """, executable = true
            )

            writeServiceFile(
                project, "${project.name}-install.bat", """
@if "%DEBUG%"=="" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.\

@rem install service
%DIRNAME%${project.name}.exe install

@rem start app
%DIRNAME%${project.name}.exe start
            """, executable = true
            )

            writeServiceFile(
                project, "${project.name}-uninstall.bat", """
@if "%DEBUG%"=="" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.\

@rem stop app
%DIRNAME%${project.name}.exe stop

@rem uninstall service
%DIRNAME%${project.name}.exe uninstall
            """, executable = true
            )

        } else {
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
                project, "${project.name}-install.sh", """
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
                    echo "Starting $appName";
                    ${'$'}APP_HOME/startup.sh
                    ;;
              stop)
                    # Stop daemons.
                    echo "Shutting down $appName";
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
              ) | sudo tee /etc/init.d/$appName
              sudo chmod +x /etc/init.d/$appName
              sudo chkconfig $appName on
              ${
                    if (dist.autoStart) """
              sudo service $appName start
              """.trimIndent() else ""
                }
            else
              (
                cat <<EOF
            [Unit]
            Description=$appName
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
              ) | sudo tee /etc/systemd/system/$appName.service
              sudo systemctl daemon-reload
              sudo systemctl enable $appName.service
              ${
                    if (dist.autoStart) """
              sudo systemctl start $appName.service
              """.trimIndent() else ""
                }
            fi
            """
            )

            //${project.name}-uninstall
            writeServiceFile(
                project, "${project.name}-uninstall.sh", """
            #!/usr/bin/env sh
            
            if [ -z "${'$'}(whereis systemctl | cut -d':' -f2)" ]; then
              sudo service $appName stop
              sudo chkconfig $appName off
              sudo rm -f /etc/init.d/$appName
            else
              sudo systemctl stop $appName.service
              sudo systemctl disable $appName.service
              sudo rm -f /etc/systemd/system/$appName.service
            fi
            """
            )
        }
    }


    private fun writeServiceFile(
        project: Project,
        fileName: String,
        text: String,
        executable: Boolean = true
    ) {
        val serviceScript = File(project.layout.buildDirectory.get().asFile, "service/$fileName")
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