package cn.bestwu.autodoc.core

import cn.bestwu.autodoc.core.model.DocModule
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.DocOperation
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.lang.util.RandomUtil
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import java.io.File


/**
 * @author Peter Wu
 */
open class AutodocExtension(
        var author: String = "autodoc",
        var version: String = "v1.0",
        var apiHost: String = "",
        var toclevels: Int = 2,
        /**
         * 最大响应时间(单位毫秒)
         */
        var maxResponseTime: Int = 2000,
        var source: File = File("src/doc"),
        var output: File? = null,
        var authUri: String = "/oauth/token",
        var signParam: String = "sign",
        var wrapResponse: Boolean = true,
        var authVariables: Array<String> = arrayOf("token_type", "access_token", "refresh_token"),
        var properties: Map<String, Any?> = emptyMap()) {

    var projectName: String = ""
        get() = if (field.isBlank()) "接口文档" else field


    val outputFile: File
        get() = if (output == null) this.source else output!!

    val readme: File
        get() {
            val file = File(source, "README.adoc")
            if (!file.exists() && rootSource != null) {
                val readme = File(rootSource, "README.adoc")
                if (readme.exists())
                    return readme
            }
            return file
        }

    fun propertiesFile(module: DocModule): File {
        val file = module.moduleFile { File(it, "properties.adoc") }
        return if (file.exists()) {
            file
        } else {
            val pfile = File(source, "properties.adoc")
            return if (pfile.exists()) {
                pfile
            } else {
                File(rootSource, "properties.adoc")
            }
        }
    }

    var rootSource: File? = null
        get() = if (field == null) {
            findUpDoc(source.absoluteFile.parentFile)
        } else field

    private fun findUpDoc(file: File): File? {
        val parentFile = file.absoluteFile.parentFile
        return if (parentFile != null) {
            val pFile = File(parentFile, "doc")
            if (pFile.exists()) {
                pFile
            } else findUpDoc(parentFile)
        } else
            null
    }

    /**
     * 公共adoc文件
     */
    fun commonAdocs(module: DocModule): List<File> {
        return module.moduleFile {
            listAdoc(it, true)
        }
    }

    fun commonAdocs(): List<File> {
        val files = listAdoc(source, false).toMutableList()
        if (rootSource != null) {
            files += listAdoc(rootSource!!, false)
        }
        return files
    }

    private fun listAdoc(dic: File, includeReadme: Boolean): List<File> =
            dic.listFiles { file -> file.isFile && file.extension == "adoc" && file.name != "properties.adoc" && (includeReadme || file.name != "README.adoc") }?.toList()
                    ?: emptyList()


    fun adocFile(moduleName: String) = File(outputFile, "$projectName-$moduleName.adoc")
    fun htmlFile(modulePyName: String) = File(outputFile, "$modulePyName.html")
    fun pdfFile(moduleName: String) = File(outputFile, "$projectName-$moduleName.pdf")
    fun postmanFile(modulePyName: String) = File(outputFile, "${PinyinHelper.convertToPinyinString(projectName, "", PinyinFormat.WITHOUT_TONE)}-$modulePyName.postman_collection.json")

    private fun listFileMap(): Map<String, Pair<File?, File?>> {
        val sourceFile = source
        val rootFiles = if (rootSource?.exists() == true) {
            rootSource!!.listFiles { file -> file.isDirectory }
        } else
            arrayOf()
        val fileMap = mutableMapOf<String, Pair<File?, File?>>()
        rootFiles?.forEach {
            fileMap[it.name] = Pair<File?, File?>(it, null)
        }

        val projectFiles = if (!sourceFile.exists()) {
            arrayOf()
        } else
            sourceFile.listFiles { file -> file.isDirectory }

        projectFiles?.forEach {
            val pair = fileMap[it.name]
            if (pair == null) {
                fileMap[it.name] = null to it
            } else {
                fileMap[it.name] = pair.first to it
            }
        }
        return fileMap
    }

    fun listModuleNames(action: (String, String) -> Unit) {
        val pynames = mutableMapOf<String, Int>()
        listFileMap().values.toList().sortedBy {
            (it.first?.name ?: it.second?.name!!).replace(".", "")
        }.forEach { u ->
            val name = u.first?.name ?: u.second?.name!!
            action(name, pynames.pyname(name))
        }
    }


    fun listModules(action: (DocModule, String) -> Unit) {
        val pynames = mutableMapOf<String, Int>()
        listFileMap().values.forEach { u ->
            val name = u.first?.name ?: u.second?.name!!
            val pyname = pynames.pyname(name)
            action(DocModule(u.first, u.second), pyname)
        }
    }

    fun extFieldExt(operation: DocOperation) {
        operation.apply {
            request.apply {
                this as DocOperationRequest
                commonFields("request.uriVariables").knewFixFields(uriVariablesExt)
                commonFields("request.headers").knewFixFields(headersExt)
                commonFields("request.parameters").knewFixFields(parametersExt)
                commonFields("request.parts").knewFixFields(partsExt)
                commonFields("request.content").knewFixFields(contentExt)
            }
            response.apply {
                this as DocOperationResponse
                commonFields("response.headers").knewFixFields(headersExt)
                commonFields("response.content").knewFixFields(contentExt)
            }
        }
    }


    private fun commonFields(name: String): Set<Field> {
        val commonFields = commonFields(name, source)
        commonFields.addAll(commonFields(name, rootSource))
        return commonFields
    }

    private fun commonFields(name: String, source: File?): LinkedHashSet<Field> {
        return if (source != null) {
            var file = File(source, "${name}.yml")
            if (!file.exists()) {
                file = File(source, "field.yml")
            }
            if (file.exists()) {
                file.parseList(Field::class.java)
            } else {
                linkedSetOf()
            }
        } else {
            linkedSetOf()
        }
    }


    fun docStatic() {
        copy("docinfo.html")
        copy("doc-static/font-awesome.min.css")
        copy("doc-static/highlight.min.js")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hkIqOjjg.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRmecf1I.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hoIqOjjg.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImaTC7TMQ.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOUuhp.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWufuVMCoY.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWufeVMCoY.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfROecf1I.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0Xdc1UAw.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hvIqOjjg.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OXuhpOqc.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFMWaCi_.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRuecf1I.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhrIqM.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWufOVMCoY.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFUZ0bbck.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImbjC7TMQ.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRiecf1I.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImajC7.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhoIqOjjg.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZzC7TMQ.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OUehpOqc.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFWJ0bbck.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hlIqOjjg.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hmIqOjjg.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhlIqOjjg.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFVp0bbck.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZDC7TMQ.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFsWaCi_.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hnIqOjjg.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFgWaCi_.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFVZ0b.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFQWaCi_.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWuf-VMCoY.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOXuhpOqc.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0ddc1UAw.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfReecQ.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFWZ0bbck.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OXehpOqc.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOXehpOqc.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOVuhpOqc.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRqecf1I.woff2")
        copy("doc-static/gstatic/6NUO8FuJNQ2MbkrZ5-J8lKFrp7pRef2r.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OVuhpOqc.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0Vdc1UAw.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0Udc1UAw.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKWyV9hrIqM.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0adc1UAw.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhkIqOjjg.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFkWaCi_.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWuc-VM.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFcWaA.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0Wdc1UAw.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFW50bbck.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOUehpOqc.woff2")
        copy("doc-static/gstatic/ga6Law1J5X9T9RW6j9bNdOwzfRSecf1I.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhvIqOjjg.woff2")
        copy("doc-static/gstatic/mem8YaGs126MiZpBA-UFWp0bbck.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWud-VMCoY.woff2")
        copy("doc-static/gstatic/ga6Vaw1J5X9T9RW6j9bNfFIu0RWucOVMCoY.woff2")
        copy("doc-static/gstatic/mem6YaGs126MiZpBA-UFUK0Zdc0.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OXOhpOqc.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhmIqOjjg.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OX-hpOqc.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZjC7TMQ.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UN_r8OUuhp.woff2")
        copy("doc-static/gstatic/memnYaGs126MiZpBA-UFUKXGUdhnIqOjjg.woff2")
        copy("doc-static/gstatic/ga6Iaw1J5X9T9RW6j9bNfFoWaCi_.woff2")
        copy("doc-static/gstatic/ga6Kaw1J5X9T9RW6j9bNfFImZTC7TMQ.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOX-hpOqc.woff2")
        copy("doc-static/gstatic/mem5YaGs126MiZpBA-UNirkOXOhpOqc.woff2")
        copy("doc-static/fonts/fontawesome-webfont.woff2")
        copy("doc-static/fonts/fontawesome-webfont.svg")
        copy("doc-static/fonts/fontawesome-webfont.woff")
        copy("doc-static/fonts/fontawesome-webfont.ttf")
        copy("doc-static/fonts/fontawesome-webfont.eot")
        copy("doc-static/github.min.css")
        copy("doc-static/Open+Sans.css")
    }

    private fun copy(path: String) {
        AutodocExtension::class.java.getResourceAsStream("/$path").copyTo(File(outputFile, path).apply { parentFile.mkdirs() }.outputStream())
    }
}
