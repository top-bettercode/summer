package top.bettercode.summer.tools.autodoc

import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import top.bettercode.summer.tools.autodoc.AutodocUtil.pyname
import top.bettercode.summer.tools.autodoc.model.DocModule
import java.io.File


/**
 * @author Peter Wu
 */
open class AutodocExtension(
        var apiHost: String = "{apiHost}",
        var author: String = "autodoc",
        var version: String = "v1.0",
        var toclevels: Int = 2,
        /**
         * 最大响应时间(单位毫秒)
         */
        var maxResponseTime: Int = 2000,
        var source: File = File("src/doc"),
        var output: File? = null,
        var authUri: String = "/oauth/token",
        var signParam: String = "sign",
        var authVariables: Array<String> = arrayOf(
                "data.token_type",
                "data.access_token",
                "data.refresh_token"
        ),
        var properties: Map<Any, Any?> = emptyMap()
) {

    var projectName: String = ""
        get() = field.ifBlank { "接口文档" }


    val outputFile: File by lazy { if (output == null) this.source else output!! }

    val readme: File
            by lazy {
                val file = File(source, "README.adoc")
                if (!file.exists() && rootSource != null) {
                    val readme = File(rootSource, "README.adoc")
                    if (readme.exists())
                        readme
                    else
                        file
                } else
                    file
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
    fun commonAdocs(module: DocModule): Collection<File> {
        return module.allModuleFiles {
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
            dic.listFiles { file -> file.isFile && file.extension == "adoc" && file.name != "properties.adoc" && (includeReadme || file.name != "README.adoc") }
                    ?.toList()
                    ?: emptyList()


    fun adocFile(moduleName: String) = File(outputFile, "$projectName-$moduleName.adoc")
    fun htmlFile(modulePyName: String) = File(outputFile, "$modulePyName.html")
    fun pdfFile(moduleName: String) = File(outputFile, "$projectName-$moduleName.pdf")
    fun postmanFile(modulePyName: String) = File(
            outputFile,
            "${
                PinyinHelper.convertToPinyinString(
                        projectName,
                        "",
                        PinyinFormat.WITHOUT_TONE
                )
            }-$modulePyName.postman_collection.json"
    )

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


}
