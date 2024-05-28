package top.bettercode.summer.gradle.plugin.dist

import groovy.text.SimpleTemplateEngine
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
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
import java.io.*

class TemplateBasedStartScriptGenerator(
    private val project: Project,
    private val dist: DistExtension,
    private val windows: Boolean,
    private val includeJdk: Boolean,
    private val includeNative: Boolean
) : TemplateBasedScriptGenerator {
    private val lineSeparator: String =
        if (windows) TextUtil.getWindowsLineSeparator() else TextUtil.getUnixLineSeparator()
    private val bindingFactory: StartScriptTemplateBindingFactory =
        if (windows) StartScriptTemplateBindingFactory.windows() else StartScriptTemplateBindingFactory.unix()
    private var template: TextResource

    init {
        template = template()
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

            val nativeLibArgs = dist.nativeLibArgs(project)
            if (defaultJvmOpts.contains(nativeLibArgs)) {
                defaultJvmOpts.remove(nativeLibArgs)
                defaultJvmOpts += dist.startScriptNativeLibArgs(project, windows)
            } else if (includeNative) {
                defaultJvmOpts += dist.startScriptNativeLibArgs(project, windows)
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
        return IoUtils.get(getTemplate().asReader()) { reader: Reader? ->
            val engine = SimpleTemplateEngine()
            val template = engine.createTemplate(reader)
            val output = template.make(binding).toString()
            return@get TextUtil.convertLineSeparators(output, lineSeparator) ?: ""
        }
    }

    private fun template(): StringBackedTextResource {
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
        if (includeJdk) {
            text = if (windows) {
                val location = "@rem Add default JVM options here"
                StringBuilder(text).insert(
                    text.indexOf(location),
                    "@rem Set JAVA_HOME.\r\nset JAVA_HOME=%APP_HOME%\\\\jdk\r\n\r\n"
                ).toString()
            } else {
                val location = "# Add default JVM options here"
                StringBuilder(text).insert(
                    text.indexOf(location),
                    "# Set JAVA_HOME.\nJAVA_HOME=\"\\\$APP_HOME/jdk\"\n\n"
                ).toString()
            }
        }

        if (windows) {
            text = StringBuilder(text).insert(
                text.indexOf("@rem Add default JVM options here") - 1,
                "\r\nmkdir \"%APP_HOME%\\\\build\\\\native\" /p\r\n"
            ).toString()
        } else {
            text = StringBuilder(text).insert(
                text.indexOf("# Add default JVM options here") - 1,
                "\nmkdir -p \\\$APP_HOME/build/native\n"
            ).toString()

            text = StringBuilder(text).insert(
                text.indexOf("# Use the maximum available") - 1,
                "DEFAULT_JVM_OPTS=`eval echo \\\$DEFAULT_JVM_OPTS`\n"
            ).toString()

            if (includeNative) {
                text = StringBuilder(text).insert(
                    text.indexOf("# Use the maximum available") - 1,
                    "\nexport LD_LIBRARY_PATH=\\\$LD_LIBRARY_PATH:${dist.linuxLDLibraryPath(project)}\n"
                ).toString()
            }
        }

        return StringBackedTextResource(
            GradleUserHomeTemporaryFileProvider { project.gradle.gradleUserHomeDir },
            text
        )
    }

}