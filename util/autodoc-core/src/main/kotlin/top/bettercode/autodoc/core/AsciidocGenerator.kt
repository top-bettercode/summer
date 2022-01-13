package top.bettercode.autodoc.core

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import top.bettercode.autodoc.core.model.Field
import top.bettercode.autodoc.core.operation.DocOperationRequest
import top.bettercode.autodoc.core.operation.DocOperationResponse
import top.bettercode.logging.operation.HttpOperation
import top.bettercode.logging.operation.Operation
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * 生成MD接口文档
 *
 * @author Peter Wu
 */
object AsciidocGenerator : AbstractbGenerator() {

    private val asciidoctor: Asciidoctor = Asciidoctor.Factory.create()
    private val TOKEN_PATTERN = Pattern.compile("@.+?@")

    init {
        asciidoctor.requireLibrary("asciidoctor-diagram")
    }

    fun html(autodoc: AutodocExtension) {
        autodoc.docStatic()
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
                asciidoctor.convertFile(inFile, optionsBuilder.build())
                println("生成：$outFile")
            } catch (e: Exception) {
                e.printStackTrace()
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
            optionsBuilder.attributes(
                Attributes.builder().attributes(
                    mapOf(
                        "pdf-fontsdir" to AsciidocGenerator::class.java.getResource("/data/fonts")?.file,
                        "pdf-style" to AsciidocGenerator::class.java.getResource("/data/themes/default-theme.yml")?.file
                    )
                ).build()
            )
            optionsBuilder.mkDirs(true)
            optionsBuilder.safe(SafeMode.UNSAFE)
            try {
                println(
                    "${if (outFile.exists()) "覆盖" else "生成"}：${
                        if (prefixPath == null) outFile.path else
                            outFile.absolutePath.substringAfter(
                                prefixPath.absolutePath + File.separator
                            )
                    }"
                )
                asciidoctor.convertFile(inFile, optionsBuilder.build())
            } catch (e: Exception) {
                e.printStackTrace()
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
        autodoc.listModuleNames { name, pyname ->
            if (name != currentName) {
                pw.appendln()
                pw.appendln("[[_${pynames.pyname(name)}]]")
                pw.appendln("== link:$pyname.html[$name]")
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
                    out.println("= $projectName")
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
:docinfo1:
:table-caption!:
:sectlinks:"""
                        )
                    }
                    out.println(":toclevels: $toclevels")
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
                (commonAdocs + autodoc.commonAdocs(module)).sortedWith(kotlin.Comparator { o1, o2 ->
                    if (o1.name == "README.adoc") -1 else o1.name.compareTo(
                        o2.name
                    )
                }).forEach {
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
                module.collections.forEach { collection ->
                    val collectionName = collection.name

                    out.println()
                    out.println("[[_${pynames.pyname(collectionName)}]]")
                    out.println("== $collectionName")
                    out.println()

                    collection.operations.forEach { operation ->
                        out.println()
                        val operationPath =
                            operation.operationFile.absolutePath.substringAfter(sourcePath)
                        val operationName = operation.name
                        out.println("[[_${pynames.pyname("$collectionName-$operationName")}]]")
                        out.println("=== $operationName")
                        out.println()
                        out.println("[width=\"100%\",cols=\"1,4,1,1,2,1,2\", stripes=\"even\"]")
                        out.println("|===")
                        if (operation.description.isNotBlank()) {
                            out.println(".1+.^|说明 6+|${operation.description}")
                        }

                        val request = operation.request as DocOperationRequest
                        request.apply {
                            out.println(".1+.^|方法 6+.^|${method}")

                            out.println(
                                ".1+.^|地址 6+.^|link:{{apiHost}}${
                                    str(
                                        HttpOperation.getRestRequestPath(
                                            request
                                        )
                                    )
                                }[{{apiHost}}++$restUri++]"
                            )

                            if (uriVariablesExt.isNotEmpty()) {
                                val uriFields =
                                    uriVariablesExt.checkBlank("$operationPath:request.uriVariablesExt")
                                out.println(".${uriFields.size + 1}+.^|URL")
                                out.println("h|名称 h|类型 3+h|描述 h|示例")
                                uriFields.forEach {
                                    out.print(
                                        "|${
                                            str(
                                                it.name
                                            )
                                        }"
                                    )
                                    out.print(
                                        "|${
                                            str(
                                                it.type
                                            )
                                        }"
                                    )
                                    out.print(
                                        " 3+|${
                                            str(
                                                it.description,
                                                true
                                            )
                                        }"
                                    )
                                    out.print(
                                        "|${
                                            str(
                                                it.value
                                            )
                                        }"
                                    )
                                    out.println()
                                }
                            }
                            if (headersExt.isNotEmpty()) {
                                val headerFields =
                                    headersExt.checkBlank("$operationPath:request.headersExt")
                                out.println(".${headerFields.size + 1}+.^|请求头")
                                out.println("h|名称 h|类型 h|必填 2+h|描述 h|示例")
                                headerFields.forEach {
                                    out.print(
                                        "|${
                                            str(
                                                it.name
                                            )
                                        }"
                                    )
                                    out.print(
                                        "|${
                                            str(
                                                it.type
                                            )
                                        }"
                                    )
                                    out.print(
                                        "|${
                                            str(
                                                it.requiredDescription
                                            )
                                        }"
                                    )
                                    out.print(
                                        " 2+|${
                                            str(
                                                it.description,
                                                true
                                            )
                                        }"
                                    )
                                    out.print(
                                        "|${
                                            str(
                                                it.value
                                            )
                                        }"
                                    )
                                    out.println()
                                }
                            }

                            val parameterFields =
                                parametersExt.checkBlank("$operationPath:request.parametersExt")
                            val partsFields = partsExt.checkBlank("$operationPath:request.partsExt")
                            val contentFields =
                                contentExt.checkBlank("$operationPath:request.contentExt")
                            val parameterBuilder = StringBuilder()
                            val size =
                                writeParameters(
                                    parameterBuilder,
                                    parameterFields,
                                    partsFields,
                                    contentFields
                                )

                            out.println(".${size + 1}+.^|请求")
                            if (size == 0) {
                                out.println("6+|无")
                            } else {
                                out.println("h|名称 h|类型 h|必填 h|描述 h|默认值 h|示例值")
                                out.println(parameterBuilder.toString())
                            }
                        }
                        val response = operation.response as DocOperationResponse
                        response.apply {
                            val contentFields =
                                contentExt.checkBlank("$operationPath:response.contentExt")
                            val responseBuilder = StringBuilder()
                            val size = writeResponse(
                                responseBuilder,
                                contentFields
                            )
                            out.println(".${size + 1}+.^|响应")
                            if (size == 0) {
                                out.println("6+|无")
                            } else {
                                out.println("h|名称 h|类型 3+h|描述 h|示例")
                                out.println(responseBuilder.toString())
                            }
                        }
                        if (!pdf) {
                            out.println(".1+.^|示例 6+a|")
                            out.println("[source,http,options=\"nowrap\"]")
                            out.println("----")
                            out.println(
                                HttpOperation.toString(
                                    operation.request,
                                    operation.protocol,
                                    true
                                ).replace("|", "\\|")
                            )
                            out.println(
                                HttpOperation.toString(response, operation.protocol, true)
                                    .replace("|", "\\|")
                            )
                            out.println("----")
                        }

                        out.println("|===")
                        out.println("'''")
                        out.println()
                    }
                }
            }
            println("生成：$adocFile")
        }
    }

    private fun writeResponse(out: StringBuilder, contentFields: Set<Field>): Int {
        var size = 0
        contentFields.forEach { field ->
            size += writeResp(out, field)
        }
        return size
    }

    private fun writeParameters(
        out: StringBuilder,
        parameterFields: Set<Field>,
        partsFields: Set<Field>,
        contentFields: Set<Field>
    ): Int {
        var size = 0
        parameterFields.forEach {
            size += writeParam(out, it)
        }
        partsFields.forEach {
            size += writeParam(out, it)
        }
        contentFields.forEach {
            size += writeParam(out, it)
        }
        return size
    }

    private fun writeResp(out: StringBuilder, field: Field, depth: Int = 0): Int {
        out.append(
            "|${fillBlank(depth)}${
                str(
                    field.name
                )
            }"
        )
        out.append("|${str(field.type)}")
        out.append(
            " 3+|${
                str(
                    field.description,
                    true
                )
            }"
        )

        out.append("|${str(if (field.children.isNotEmpty()) "" else field.value)}")
        out.appendln()
        var size = 1
        field.children.forEach {
            size += writeResp(out, it, depth + 1)
        }
        return size
    }

    private fun writeParam(out: StringBuilder, field: Field, depth: Int = 0): Int {
        out.append(
            "|${fillBlank(depth)}${
                str(
                    field.name
                )
            }"
        )
        out.append("|${str(field.type)}")
        out.append("|${str(field.requiredDescription)}")
        out.append("|${str(field.description, true)}")
        out.append("|${str(field.defaultVal)}")
        out.append("|${str(field.value)}")
        out.appendln()
        var size = 1
        field.children.forEach {
            size += writeParam(out, it, depth + 1)
        }
        return size
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
            var blank = "[white]#├──# "
            for (i in 1 until depth) {
                blank += blank
            }
            blank
        }
    }
}
