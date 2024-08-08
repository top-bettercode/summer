package top.bettercode.summer.gradle.plugin.autodoc

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import top.bettercode.summer.tools.autodoc.AutodocExtension
import top.bettercode.summer.tools.autodoc.AutodocExtension.Companion.pyname
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.AutodocUtil.checkBlank
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.operation.Operation
import java.io.File
import java.io.PrintWriter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * 生成MD接口文档
 *
 * @author Peter Wu
 */
object AsciidocGenerator {

    private val log = org.slf4j.LoggerFactory.getLogger(AsciidocGenerator::class.java)
    private val TOKEN_PATTERN = Pattern.compile("@.+?@")

    fun html(autodoc: AutodocExtension) {
        docStatic(autodoc)
        autodoc.listModuleNames { name, pyname ->
            val adocFile = autodoc.adocFile(name)
            if (adocFile.exists()) {
                val htmlFile = autodoc.htmlFile(pyname)
                html(adocFile, htmlFile)
                htmlFile.writeText(
                    htmlFile.readText()
                        .replace(
                            "https://fonts.googleapis.com/css?family=Open+Sans:300,300italic,400,400italic,600,600italic%7CNoto+Serif:400,400italic,700,700italic%7CDroid+Sans+Mono:400,700",
                            "static/Open+Sans.css"
                        )
                        .replace(
                            "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css",
                            "static/font-awesome.min.css"
                        )
                        .replace(
                            "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.18.3/highlight.min.js",
                            "static/highlight.min.js"
                        )
                        .replace(
                            "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.18.3/styles/github.min.css",
                            "static/github.min.css"
                        )
                )
            }
        }
    }

    fun html(inFile: File, outFile: File) {
        if (inFile.exists()) {
            val optionsBuilder = Options.builder()
            optionsBuilder.toFile(outFile)
            optionsBuilder.mkDirs(true)
            optionsBuilder.safe(SafeMode.UNSAFE)
            try {
                val asciidoctor: Asciidoctor = Asciidoctor.Factory.create()
                asciidoctor.requireLibrary("asciidoctor-diagram")
                asciidoctor.convertFile(inFile, optionsBuilder.build())
                log.warn("生成：$outFile")
            } catch (e: Exception) {
                log.error(e.message, e)
            }
        }
    }

    fun pdf(autodoc: AutodocExtension) {
        asciidoc(autodoc, true)
        autodoc.listModuleNames { name, _ ->
            val adocFile = autodoc.adocFile(name)
            if (adocFile.exists()) {
                val pdfFile = autodoc.pdfFile(name)
                pdf(adocFile, pdfFile)
            }
        }
    }

    fun pdf(inFile: File, outFile: File, prefixPath: File? = null) {
        if (inFile.exists()) {
            val optionsBuilder = Options.builder()
            optionsBuilder.toFile(outFile)
            optionsBuilder.backend("pdf")
//asciidoctor-pdf -a scripts=cjk -a pdf-theme=./themes/cjk-theme.yml -a pdf-fontsdir=./themes document.adoc
            optionsBuilder.attributes(
                Attributes.builder().attributes(
                    mapOf(
                        "scripts" to "cjk",
                        "pdf-fontsdir" to "uri:classloader:/themes;GEM_FONTS_DIR;",
                        "pdf-theme" to "uri:classloader:/themes/cjk-theme.yml"
                    )
                ).build()
            )
            optionsBuilder.mkDirs(true)
            optionsBuilder.safe(SafeMode.UNSAFE)
            try {
                val asciidoctor: Asciidoctor = Asciidoctor.Factory.create()
                asciidoctor.requireLibrary("asciidoctor-diagram")
                asciidoctor.convertFile(inFile, optionsBuilder.build())
                log.warn(
                    "${if (outFile.exists()) "覆盖" else "生成"}：${
                        if (prefixPath == null) outFile.path else
                            outFile.absolutePath.substringAfter(
                                prefixPath.absolutePath + File.separator
                            )
                    }"
                )
            } catch (e: Exception) {
                log.error(e.message, e)
            }
        }
    }

    private fun postmanLink(autodoc: AutodocExtension, name: String): String {
        return Operation.LINE_SEPARATOR + "== link:${autodoc.postmanFile(name).name}[Postman Collection]" + Operation.LINE_SEPARATOR
    }

    private fun moduleToc(
        autodoc: AutodocExtension,
        currentName: String,
        pynames: MutableMap<String, Int>
    ): String {
        val pw = StringBuilder()
        pw.appendLine("== 其他版本")
        autodoc.listModuleNames { name, pyname ->
            if (name != currentName) {
                pw.appendLine()
                pw.appendLine("[[_${pynames.pyname(name)}]]")
                pw.appendLine("* link:$pyname.html[$name]")
            }
        }
        return pw.toString()
    }

    fun setDefaultDesc(autodoc: AutodocExtension, properties: Properties) {
        autodoc.listModules { module, _ ->
            module.collections.forEach { collection ->
                collection.operations.forEach { operation ->
                    val request = operation.request as DocOperationRequest
                    val response = operation.response as DocOperationResponse

                    request.uriVariablesExt.setDefaultFieldDesc(properties)
                    request.headersExt.setDefaultFieldDesc(properties)
                    request.parametersExt.setDefaultFieldDesc(properties)
                    request.partsExt.setDefaultFieldDesc(properties)
                    request.contentExt.setDefaultFieldDesc(properties)

                    response.headersExt.setDefaultFieldDesc(properties)
                    response.contentExt.setDefaultFieldDesc(properties)

                    operation.save()
                }
            }
        }
    }

    private fun Set<Field>.setDefaultFieldDesc(properties: Properties) {
        this.forEach {
            if (it.description.isBlank() || it.name == it.description) {
                it.description = properties.getOrDefault(it.name, it.name).toString()
            }
            it.children.setDefaultFieldDesc(properties)
        }
    }

    fun asciidoc(autodoc: AutodocExtension, pdf: Boolean = false) {
        val rootDoc = autodoc.rootSource
        val sourcePath = (rootDoc?.absoluteFile?.parentFile?.absolutePath
            ?: autodoc.source.absolutePath) + File.separator
        val commonAdocs = autodoc.commonAdocs()
        autodoc.listModules { module, pyname ->
            module.clean()
            val adocFile = autodoc.adocFile(module.name)
            adocFile.delete()
            adocFile.parentFile.mkdirs()
            adocFile.printWriter().use { out ->
                val pynames = mutableMapOf<String, Int>()
                autodoc.apply {
                    out.println("= $projectName-${module.name}")
                    if (author.isNotBlank())
                        out.println(author)
                    out.println(module.name)
                    val adocProperties = autodoc.propertiesFile(module)
                    if (adocProperties.exists()) {
                        adocProperties.readLines().filter { it.isNotBlank() }.forEach {
                            out.println(it)
                        }
                    } else {
                        out.println(
                            """:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toc-title: 目录
:sectanchors:
:pdf-page-margin: [1in, 0.5in]
:docinfo1:
:table-caption!:
:sectlinks:"""
                        )
                    }
                    out.println(":toclevels: $toclevels")
                    out.println(":apiAddress: $apiAddress")
                }
                if (!pdf) {
                    out.print(
                        moduleToc(
                            autodoc,
                            module.name,
                            pynames
                        )
                    )
                    out.print(
                        postmanLink(
                            autodoc,
                            pyname
                        )
                    )
                }
                if (autodoc.readme.exists()) {
                    out.println()
                    var pre = ""
                    autodoc.readme.readLines().forEach {
                        if (it.startsWith("==") && !pre.startsWith("[["))
                            out.println("[[_${pynames.pyname(it.substringAfter(" "))}]]")
                        out.println(it)
                        pre = it
                    }
                }
                val properties = autodoc.properties
                (commonAdocs + autodoc.commonAdocs(module)).sortedWith { o1, o2 ->
                    if (o1.name == "README.adoc") -1 else o1.name.compareTo(o2.name)
                }.forEach   {
                    out.println()
                    var pre = ""
                    it.readLines().forEach { l ->
                        var line = l
                        val matcher: Matcher = TOKEN_PATTERN.matcher(line)
                        while (matcher.find()) {
                            var group = matcher.group()
                            group = group.substring(1, group.length - 1)
                            val any = properties[group]
                            if (any != null)
                                line = line.replace("@$group@", any.toString())
                        }
                        if (line.startsWith("==") && !pre.startsWith("[["))
                            out.println("[[_${pynames.pyname(line.substringAfter(" "))}]]")
                        out.println(line)
                        pre = line
                    }

                }
                out.println()
                out.println(":sectnums:")
                module.collections.filter { it.operations.isNotEmpty() }.forEach { collection ->
                    val collectionName = collection.name

                    out.println()
                    out.println("[[_${pynames.pyname(collectionName)}]]")
                    out.println("== $collectionName")
                    out.println()

                    collection.operations.forEach { operation ->
                        out.println()
                        val operationPath =
                            operation.operationFile.absolutePath.substringAfter(sourcePath)
                        val operationName = operation.name.replace(AutodocUtil.REPLACE_CHAR, "/")
                        out.println("[[_${pynames.pyname("$collectionName-$operationName")}]]")
                        out.println("=== $operationName")
                        out.println(":sectnums!:")
                        out.println()
                        if (operation.description.isNotBlank()) {
                            out.println("==== 接口说明")
                            out.println(operation.description)
                            out.println()
                        }
                        val request = operation.request as DocOperationRequest
                        request.apply {
                            out.println("==== 请求")
                            out.println(
                                "$method link:{apiAddress}${
                                    HttpOperation.getRequestPath(request)
                                }[{apiAddress}$restUri]"
                            )
                            out.println()

                            if (uriVariablesExt.isNotEmpty()) {
                                val fields =
                                    uriVariablesExt.checkBlank("$operationPath:request.uriVariablesExt")
                                out.println("==== URL参数")
                                out.println("[width=\"100%\", cols=\"2,1,3,3\", stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|描述 h|示例")
                                fields.forEach {
                                    out.print("|${str(it.name)}")
                                    out.print("|${str(it.type)}")
                                    out.print("|${str(it.description, true)}")
                                    out.print("|${str(it.value)}")
                                    out.println()
                                }
                                out.println("|===")
                            }
                            if (headersExt.isNotEmpty()) {
                                val fields =
                                    headersExt.checkBlank("$operationPath:request.headersExt")
                                out.println("==== 请求头")
                                out.println("[width=\"100%\", cols=\"2,2,1,3,3\", stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|必填 h|描述 h|示例")
                                fields.forEach {
                                    out.print("|${str(it.name)}")
                                    out.print("|${str(it.type)}")
                                    out.print("|${str(it.requiredDescription)}")
                                    out.print("|${str(it.description, true)}")
                                    out.print("|${str(it.value)}")
                                    out.println()
                                }
                                out.println("|===")
                            }

                            if (queriesExt.isNotEmpty()) {
                                val fields =
                                    queriesExt.checkBlank("$operationPath:request.queriesExt")
                                out.println("==== 查询参数")
                                out.println("[width=\"100%\", cols=\"3,2,1,3,2,2\", stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|必填 h|描述 h|默认值 h|示例值")
                                fields.forEach {
                                    writeParam(out, it)
                                }
                                out.println("|===")
                            }

                            if (parametersExt.isNotEmpty()) {
                                val fields =
                                    parametersExt.checkBlank("$operationPath:request.parametersExt")
                                out.println("==== 请求参数")
                                out.println("[width=\"100%\", cols=\"3,2,1,3,2,2\", stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|必填 h|描述 h|默认值 h|示例值")
                                fields.forEach {
                                    writeParam(out, it)
                                }
                                out.println("|===")
                            }
                            if (partsExt.isNotEmpty()) {
                                val fields =
                                    partsExt.checkBlank("$operationPath:request.partsExt")
                                out.println("==== 请求参数")
                                out.println("[width=\"100%\", cols=\"3,2,1,3,2,2\", stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|必填 h|描述 h|默认值 h|示例值")
                                fields.forEach {
                                    writeParam(out, it)
                                }
                                out.println("|===")
                            }

                            if (contentExt.isNotEmpty()) {
                                val fields =
                                    contentExt.checkBlank("$operationPath:request.bodyExt")
                                out.println("==== 请求体")
                                out.println("[width=\"100%\", cols=\"3,2,1,3,2,2\", stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|必填 h|描述 h|默认值 h|示例值")
                                fields.forEach {
                                    writeParam(out, it)
                                }
                                out.println("|===")
                            }
                        }
                        out.println("==== 请求示例")
                        out.println("[source,http,options=\"nowrap\"]")
                        out.println("----")
                        out.println(
                            HttpOperation.toString(operation.request, operation.protocol, true)
                        )
                        out.println("----")

                        val response = operation.response as DocOperationResponse
                        response.apply {
                            if (contentExt.isNotEmpty()) {
                                val fields =
                                    contentExt.checkBlank("$operationPath:response.contentExt")
                                out.println("==== 返回参数")
                                out.println("[width=\"100%\", cols=\"3,2,3,2\",, stripes=\"even\"]")
                                out.println("|===")
                                out.println("h|名称 h|类型 h|描述 h|示例")
                                fields.forEach {
                                    writeResp(out, it)
                                }
                                out.println("|===")
                            }
                        }
                        out.println("==== 返回示例")
                        out.println("[source,http,options=\"nowrap\"]")
                        out.println("----")
                        out.println(HttpOperation.toString(response, operation.protocol, true))
                        out.println("----")

                        out.println("'''")
                        out.println()
                        out.println(":sectnums:")
                    }
                }
            }
            log.warn("生成：$adocFile")
        }
    }

    private fun writeResp(out: PrintWriter, field: Field, depth: Int = 0): Int {
        out.append("|${fillBlank(depth)}${str(field.name)}")
        out.append("|${str(field.type)}")
        out.append("|${str(field.description, true)}")
        out.append("|${str(if (field.children.isNotEmpty()) "" else field.value)}")
        out.appendLine()
        var size = 1
        field.children.forEach {
            size += writeResp(out, it, depth + 1)
        }
        return size
    }

    private fun writeParam(out: PrintWriter, field: Field, depth: Int = 0) {
        out.append("|${fillBlank(depth)}${str(field.name)}")
        out.append("|${str(field.type)}")
        out.append("|${str(field.requiredDescription)}")
        out.append("|${str(field.description, true)}")
        out.append("|${str(field.defaultVal)}")
        out.append("|${str(field.value)}")
        out.appendLine()
        field.children.forEach {
            writeParam(out, it, depth + 1)
        }
    }

    private fun str(str: String?, desc: Boolean = false): String {
        return if (desc)
            str?.replace("|", "\\|") ?: ""
        else
            "++${str?.replace("|", "\\|") ?: ""}++"
    }

    private fun fillBlank(depth: Int): String {
        return if (depth == 0) {
            ""
        } else {
            var blank = "[white]#├─# "
            for (i in 1 until depth) {
                blank += blank
            }
            blank
        }
    }

    fun docStatic(ext: AutodocExtension) {
        copy(ext, "docinfo.html")
        copy(ext, "static/font-awesome.min.css")
        copy(ext, "static/highlight.min.js")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hkIqOjjg.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRmecf1I.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hoIqOjjg.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImaTC7TMQ.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOUuhp.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWufuVMCoY.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWufeVMCoY.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfROecf1I.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0Xdc1UAw.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hvIqOjjg.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OXuhpOqc.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFMWaCi_.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRuecf1I.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhrIqM.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWufOVMCoY.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFUZ0bbck.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImbjC7TMQ.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRiecf1I.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImajC7.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhoIqOjjg.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZzC7TMQ.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OUehpOqc.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFWJ0bbck.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hlIqOjjg.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hmIqOjjg.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhlIqOjjg.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFVp0bbck.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZDC7TMQ.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFsWaCi_.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hnIqOjjg.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFgWaCi_.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFVZ0b.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFQWaCi_.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWuf-VMCoY.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOXuhpOqc.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0ddc1UAw.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfReecQ.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFWZ0bbck.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OXehpOqc.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOXehpOqc.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOVuhpOqc.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRqecf1I.woff2")
        copy(ext, "static/gstatic/6NUO8FuJNQ2MbkrZ5-J8lKFrp7pRef2r.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OVuhpOqc.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0Vdc1UAw.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0Udc1UAw.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hrIqM.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0adc1UAw.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhkIqOjjg.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFkWaCi_.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWuc-VM.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFcWaA.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0Wdc1UAw.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFW50bbck.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOUehpOqc.woff2")
        copy(ext, "static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRSecf1I.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhvIqOjjg.woff2")
        copy(ext, "static/gstatic/mem8YaGs126MiZpBA-UFWp0bbck.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWud-VMCoY.woff2")
        copy(ext, "static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWucOVMCoY.woff2")
        copy(ext, "static/gstatic/mem6YaGs126MiZpBA-UFUK0Zdc0.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OXOhpOqc.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhmIqOjjg.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OX-hpOqc.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZjC7TMQ.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UN_r8OUuhp.woff2")
        copy(ext, "static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhnIqOjjg.woff2")
        copy(ext, "static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFoWaCi_.woff2")
        copy(ext, "static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZTC7TMQ.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOX-hpOqc.woff2")
        copy(ext, "static/gstatic/mem5YaGs126MiZpBA-UNirkOXOhpOqc.woff2")
        copy(ext, "static/fonts/fontawesome-webfont.woff2")
        copy(ext, "static/fonts/fontawesome-webfont.svg")
        copy(ext, "static/fonts/fontawesome-webfont.woff")
        copy(ext, "static/fonts/fontawesome-webfont.ttf")
        copy(ext, "static/fonts/fontawesome-webfont.eot")
        copy(ext, "static/github.min.css")
        copy(ext, "static/Open+Sans.css")
    }

    private fun copy(ext: AutodocExtension, path: String) {
        AutodocExtension::class.java.getResourceAsStream("/$path")
            ?.copyTo(File(ext.outputFile, path).apply { parentFile.mkdirs() }.outputStream())
    }

}
