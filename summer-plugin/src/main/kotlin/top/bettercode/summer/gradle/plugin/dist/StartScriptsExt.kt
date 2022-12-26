package top.bettercode.summer.gradle.plugin.dist

import org.gradle.api.Project
import java.io.File

/**
 *
 * @author Peter Wu
 */
object StartScriptsExt {

    fun ext(project: Project, dist: DistExtension) {
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