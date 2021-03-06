package top.bettercode.gradle.dist

import groovy.text.SimpleTemplateEngine
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.internal.file.temp.GradleUserHomeTemporaryFileProvider
import org.gradle.api.internal.plugins.DefaultJavaAppStartScriptGenerationDetails
import org.gradle.api.internal.plugins.StartScriptTemplateBindingFactory
import org.gradle.api.internal.plugins.UnixStartScriptGenerator
import org.gradle.api.internal.resources.StringBackedTextResource
import org.gradle.api.resources.TextResource
import org.gradle.internal.io.IoUtils
import org.gradle.jvm.application.scripts.JavaAppStartScriptGenerationDetails
import org.gradle.jvm.application.scripts.TemplateBasedScriptGenerator
import org.gradle.util.internal.TextUtil
import top.bettercode.gradle.dist.DistExtension.Companion.nativeLibArgs
import java.io.*

class TemplateBasedStartScriptGenerator(
    private val project: Project,
    private val dist: DistExtension,
    private val windows: Boolean
) : TemplateBasedScriptGenerator {
    private val lineSeparator: String =
        if (windows) TextUtil.getWindowsLineSeparator() else TextUtil.getUnixLineSeparator()
    private val bindingFactory: StartScriptTemplateBindingFactory =
        if (windows) StartScriptTemplateBindingFactory.windows() else StartScriptTemplateBindingFactory.unix()
    private var template: TextResource

    init {
        template = template(dist, windows)
    }

    override fun generateScript(details: JavaAppStartScriptGenerationDetails, destination: Writer) {
        try {
            val defaultJvmOpts = details.defaultJvmOpts
            val urandomOpt = "-Djava.security.egd=file:/dev/urandom"
            if (dist.urandom)
                if (Os.isFamily(Os.FAMILY_UNIX)) {
                    if (windows)
                        defaultJvmOpts.remove(urandomOpt)
                } else
                    if (!windows)
                        defaultJvmOpts += urandomOpt

            if (defaultJvmOpts.contains(project.nativeLibArgs)) {
                defaultJvmOpts.remove(project.nativeLibArgs)
                defaultJvmOpts += if (windows)
                    "-Djava.library.path=%APP_HOME%\\native"
                else
                    "-Djava.library.path=\$APP_HOME/native"
            }

            val classpath = details.classpath
            if (dist.unwrapResources) {
                classpath.add("conf")
            }

            val newDetails =
                DefaultJavaAppStartScriptGenerationDetails(
                    details.applicationName,
                    details.optsEnvironmentVar,
                    details.exitEnvironmentVar,
                    details.mainClassName,
                    defaultJvmOpts,
                    classpath,
                    details.modulePath,
                    details.scriptRelPath,
                    details.appNameSystemProperty
                )
            val binding = bindingFactory.transform(newDetails)
            val jvmOpts = binding["defaultJvmOpts"]
            binding["defaultJvmOpts"] =
                if (windows) jvmOpts?.replace("%%", "%") else jvmOpts?.replace("\\$", "$")
            val scriptContent = generateStartScriptContentFromTemplate(binding)
            destination.write(scriptContent)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun setTemplate(template: TextResource) {
        this.template = template
    }

    override fun getTemplate(): TextResource {
        return template
    }

    private fun generateStartScriptContentFromTemplate(binding: Map<String, String>): String {
        return IoUtils.get(getTemplate().asReader(), Transformer { reader ->
            try {
                val engine = SimpleTemplateEngine()
                val template = engine.createTemplate(reader)
                val output = template.make(binding).toString()
                return@Transformer TextUtil.convertLineSeparators(output, lineSeparator)!!
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        })
    }

    private fun template(
        dist: DistExtension,
        windows: Boolean
    ): StringBackedTextResource {
        val filename = if (windows) "windowsStartScript.txt" else "unixStartScript.txt"
        val stream: InputStream =
            UnixStartScriptGenerator::class.java.getResourceAsStream(
                filename
            )
                ?: throw IllegalStateException("Could not find class path resource " + filename + " relative to " + UnixStartScriptGenerator::class.java.name)
        val bufferedReader = BufferedReader(
            InputStreamReader(stream, Charsets.UTF_8)
        )
        var text = bufferedReader.readText()
        if (dist.includeJre) {
            text = if (windows) {
                val location = "for %%i in (\"%APP_HOME%\") do set APP_HOME=%%~fi"
                StringBuilder(text).insert(
                    text.indexOf(location) + location.length,
                    "\r\n\r\n@rem Set JAVA_HOME.\r\nset JAVA_HOME=%APP_HOME%\\\\jre"
                ).toString()
            } else {
                val location = "APP_BASE_NAME=\\\${0##*/}"
                StringBuilder(text).insert(
                    text.indexOf(location) + location.length,
                    "\n\n# Set JAVA_HOME.\nJAVA_HOME=\"\\\$APP_HOME/jre\""
                ).toString()
            }
        }
        if (!windows) {
            text = StringBuilder(text).insert(
                text.indexOf("# Use the maximum available") - 1,
                "DEFAULT_JVM_OPTS=`eval echo \\\$DEFAULT_JVM_OPTS`\n"
            ).toString()
        }

        return StringBackedTextResource(
            GradleUserHomeTemporaryFileProvider { project.gradle.gradleUserHomeDir },
            text
        )
    }

}