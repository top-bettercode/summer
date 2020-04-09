package cn.bestwu.autodoc.core

import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.lang.util.StringUtil
import cn.bestwu.logging.operation.HttpOperation
import cn.bestwu.logging.operation.Operation
import cn.bestwu.logging.operation.PrettyPrintingContentModifier
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import java.io.File
import java.io.PrintWriter


/**
 * 生成MD接口文档
 *
 * @author Peter Wu
 */
object AsciidocGenerator : AbstractbGenerator() {

    private val asciidoctor: Asciidoctor = Asciidoctor.Factory.create()

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
                htmlFile.writeText(htmlFile.readText()
                        .replace("https://fonts.googleapis.com/css?family=Open+Sans:300,300italic,400,400italic,600,600italic%7CNoto+Serif:400,400italic,700,700italic%7CDroid+Sans+Mono:400,700", "doc-static/Open+Sans.css")
                        .replace("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css", "doc-static/font-awesome.min.css")
                        .replace("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js", "doc-static/highlight.min.js")
                        .replace("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css", "doc-static/github.min.css")
                )
            }
        }
    }

    fun html(inFile: File, outFile: File) {
        if (inFile.exists()) {
            val options = Options()
            options.setToFile(outFile.absolutePath)
            options.setMkDirs(true)
            options.setSafe(SafeMode.UNSAFE)
            try {
                asciidoctor.convertFile(inFile, options)
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

    fun pdf(inFile: File, outFile: File) {
        if (inFile.exists()) {
            val options = Options()
            options.setToFile(outFile.absolutePath)
            options.setBackend("pdf")
            options.setAttributes(mapOf("pdf-fontsdir" to AsciidocGenerator::class.java.getResource("/data/fonts").file,
                    "pdf-style" to AsciidocGenerator::class.java.getResource("/data/themes/default-theme.yml").file))
            options.setMkDirs(true)
            options.setSafe(SafeMode.UNSAFE)
            try {
                asciidoctor.convertFile(inFile, options)
                println("生成：$outFile")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun postmanLink(autodoc: AutodocExtension, name: String): String {
        return Operation.LINE_SEPARATOR + "== link:${autodoc.postmanFile(name).name}[Postman Collection]" + Operation.LINE_SEPARATOR
    }

    private fun moduleToc(autodoc: AutodocExtension, currentName: String, pynames: MutableMap<String, Int>): String {
        val pw = StringBuilder()
        autodoc.listModuleNames { name, pyname ->
            if (name != currentName) {
                pw.appendln()
                pw.appendln("[[${pynames.pyname(name)}]]")
                pw.appendln("== link:$pyname.html[$name]")
            }
        }
        return pw.toString()
    }

    fun asciidoc(autodoc: AutodocExtension, pdf: Boolean = false) {
        val rootDoc = autodoc.rootSource
        val sourcePath = (rootDoc?.absoluteFile?.parentFile?.absolutePath
                ?: autodoc.source.absolutePath) + File.separator
        val commonAdocs = autodoc.commonAdocs()
        autodoc.listModules { module, pyname ->
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
                        out.println(""":doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toc-title: 目录
:sectanchors:
:docinfo1:
:table-caption!:
:sectlinks:""")
                    }
                    out.println(":toclevels: $toclevels")
                    out.println(":apiHost: $apiHost")
                }
                if (!pdf) {
                    out.print(moduleToc(autodoc, module.name, pynames))
                    out.print(postmanLink(autodoc, pyname))
                }
                if (autodoc.readme.exists()) {
                    out.println()
                    var pre = ""
                    autodoc.readme.readLines().forEach {
                        if (it.startsWith("==") && !pre.startsWith("[["))
                            out.println("[[${pynames.pyname(it.substringAfter(" "))}]]")
                        out.println(it)
                        pre = it
                    }
                }
                val properties = autodoc.properties
                ((commonAdocs + autodoc.commonAdocs(module)).sortedWith(Comparator { o1, o2 -> if (o1.name == "README.adoc") -1 else o1.name.compareTo(o2.name) })).forEach {
                    out.println()
                    var pre = ""
                    it.readLines().forEach {
                        var line = it
                        properties.forEach { entry ->
                            line = line.replace("@${entry.key}@", entry.value.toString())
                        }
                        if (line.startsWith("==") && !pre.startsWith("[["))
                            out.println("[[${pynames.pyname(line.substringAfter(" "))}]]")
                        out.println(line)
                        pre = line
                    }

                }
                out.println()
                out.println(":sectnums:")
                module.collections.forEach { collection ->
                    val collectionName = collection.name

                    out.println()
                    out.println("[[${pynames.pyname(collectionName)}]]")
                    out.println("== $collectionName")
                    out.println()

                    collection.operations.forEach { operation ->
                        out.println()
                        autodoc.extFieldExt(operation)
                        val operationPath = operation.operationFile.absolutePath.substringAfter(sourcePath)
                        val operationName = operation.name
                        out.println("[[${pynames.pyname("$collectionName-$operationName")}]]")
                        out.println("=== $operationName")
                        out.println()
                        out.println("[width=\"100%\",cols=\"1,4,1,1,2,1,2\", stripes=\"even\"]")
                        out.println("|===")
                        if (operation.description.isNotBlank()) {
                            out.println(".1+.^|说明 6+|${operation.description}")
                        }

                        val request = operation.request as DocOperationRequest
                        request.apply {
                            out.println(".1+.^|方法 6+|${method}")

                            out.println(".1+.^|地址 6+|link:{apiHost}${str(HttpOperation.getRestRequestPath(request))}[{apiHost}++$restUri++]")

                            if (uriVariables.isNotEmpty()) {
                                val uriFields = uriVariables.toFields(uriVariablesExt, operationPath)
                                out.println(".${uriFields.size + 1}+.^|URL")
                                out.println("h|名称 h|类型 3+h|描述 h|示例")
                                uriFields.forEach {
                                    out.print("|${str(it.name)}")
                                    out.print("|${str(it.type)}")
                                    out.print(" 3+|${str(it.description, true)}")
                                    out.print("|${str(it.value)}")
                                    out.println()
                                }
                            }
                            if (headers.isNotEmpty()) {
                                val headerFields = headers.singleValueMap.toFields(headersExt, operationPath)
                                out.println(".${headerFields.size + 1}+.^|请求头")
                                out.println("h|名称 h|类型 h|必填 2+h|描述 h|示例")
                                headerFields.forEach {
                                    out.print("|${str(it.name)}")
                                    out.print("|${str(it.type)}")
                                    out.print("|${str(it.requiredDescription)}")
                                    out.print(" 2+|${str(it.description, true)}")
                                    out.print("|${str(it.value)}")
                                    out.println()
                                    headers[it.name] = it.value
                                }
                            }

                            val contentParams = contentAsString.toMap()
                            val parameterFields = parameters.singleValueMap.toFields(parametersExt, operationPath, expand = true)
                            val contentFields = contentParams?.toFields(contentExt, operationPath, expand = true)
                            out.println(".${parameterFields.size + parts.size + (contentFields?.size
                                    ?: 0) + 1}+.^|请求")
                            if (parameters.isEmpty() && parts.isEmpty() && contentParams.isNullOrEmpty()) {
                                out.println("6+|无")
                            } else {
                                out.println("h|名称 h|类型 h|必填 h|描述 h|默认值 h|示例值")
                                parameterFields.forEach {
                                    writeParam(out, it)
                                }
                                parts.forEach {
                                    writeParam(out, partsExt.findField(it.name, it.contentAsString.type, operationPath))
                                }
                                contentFields?.forEach {
                                    writeParam(out, it)
                                }
                            }
                        }
                        val response = operation.response as DocOperationResponse
                        response.apply {
                            val map = contentAsString.toMap()
                            val contentFields = map?.toFields(contentExt, operationPath, expand = true)
                            out.println(".${(contentFields?.size ?: 0) + 1}+.^|响应")
                            if (map.isNullOrEmpty()) {
                                out.println("6+|无")
                            } else {
                                out.println("h|名称 h|类型 3+h|描述 h|示例")
                                contentFields?.forEach { field ->
                                    out.print("|${fillBlank(field.depth)}${str(field.name)}")
                                    out.print("|${str(field.type)}")
                                    out.print(" 3+|${str(field.description, true)}")

                                    out.print("|${str(if (field.expanded && field.value.toMap() != null) "" else field.value)}")
                                    out.println()
                                }
                            }
                        }
                        if (!pdf) {
                            out.println(".1+.^|示例 6+a|")
                            out.println("[source,http,options=\"nowrap\"]")
                            out.println("----")
                            out.println(HttpOperation.toString(operation.request, operation.protocol, true).replace("|", "\\|"))
                            response.content = PrettyPrintingContentModifier.modifyContent(response.content, response.headers.contentType)
                            out.println(HttpOperation.toString(response, operation.protocol, true).replace("|", "\\|"))
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

    private fun writeParam(out: PrintWriter, it: Field) {
        out.print("|${fillBlank(it.depth)}${str(it.name)}")
        out.print("|${str(it.type)}")
        out.print("|${str(it.requiredDescription)}")
        out.print("|${str(it.description, true)}")
        out.print("|${str(it.defaultVal)}")
        out.print("|${str(if (it.expanded && it.value.toMap() != null) "" else it.value)}")
        out.println()
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
