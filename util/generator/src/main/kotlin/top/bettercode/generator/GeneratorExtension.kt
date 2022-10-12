package top.bettercode.generator

import top.bettercode.generator.dom.java.JavaTypeResolver
import top.bettercode.generator.dom.java.element.JavaElement
import top.bettercode.generator.dsl.Generator
import top.bettercode.lang.capitalized
import top.bettercode.lang.decapitalized
import java.io.File
import java.util.*


/**
 * @author Peter Wu
 */
open class GeneratorExtension(
    /**
     * 子项目共用数据源
     */
    var unitedDatasource: Boolean = true,
    /**
     * 包名是否自动加项目名区分
     */
    var projectPackage: Boolean = false,
    /**
     * 是否是主项目
     */
    var projectIsBoot: Boolean = false,

    /**
     * 生成文件基础路径,项目路径
     */
    var projectDir: File = File("").absoluteFile,
    /**
     * 基础路径下相对路径
     */
    var dir: String = "",

    /**
     * 数据源类型，默认数据库
     */
    var dataType: DataType = DataType.DATABASE,
    /**
     * pdm文件路径
     */
    var pdmSrc: String = "pdm",
    /**
     * PlantUML 图片类型
     */
    var pumlDiagramFormat: String = "PNG",
    /**
     * SQL 脚本目录
     */
    var sqlOutput: String = "database",
    var sqlQuote: Boolean = true,
    /**
     * 覆盖所有已生成文件
     */
    var replaceAll: Boolean = true,

    /**
     * 删除模式，为true时不生成文件，删除已生成的文件
     */
    var delete: Boolean = false,
    /**
     * 生成SQL时是否生成外键相关语句
     */
    var useForeignKey: Boolean = false,
    /**
     * 生成代码包名
     */
    var packageName: String = "",
    /**
     * 使用子模块
     */
    var userModule: Boolean = true,
    /**
     * 更新Sql时生成删除表语句
     */
    var dropTablesWhenUpdate: Boolean = false,
    /**
     * 更新Sql时生成删除字段语句
     */
    var dropColumnsWhenUpdate: Boolean = false,

    var applicationName: String = "",

    var projectName: String = "",
    /**
     * 注释说明
     */
    var remarks: String = "",
    /**
     * 手动主键名
     */
    var primaryKeyName: String = "id",
    /**
     * 逻辑删除字段名
     */
    var softDeleteColumnName: String = "deleted",
    /**
     * 逻辑删除使用布尔值
     */
    var softDeleteAsBoolean: Boolean = true,
    /**
     * 缩进
     */
    var indent: String = JavaElement.defaultIndent,
    /**
     * 模板
     */
    var generators: Array<Generator> = arrayOf(),

    /**
     * 公共状态码
     */
    var commonCodeTypes: Array<String> = arrayOf(),
    /**
     * 相关数据表
     */
    var tableNames: Array<String> = arrayOf(),
    /**
     * 额外设置
     */
    var settings: MutableMap<String, String> = mutableMapOf()
) {

    /**
     * JDBC连接配置
     */
    var datasources: Map<String, JDBCConnectionConfiguration> = mapOf()
        set(value) {
            field = value
            value.values.forEach {
                it.ext = this
            }
        }


    companion object {

        const val defaultModuleName = "modules"

        /**
         * javaName
         */
        var javaName: (String) -> String = {
            javaName(it, false)
        }

        @JvmStatic
        fun javaName(str: String, capitalize: Boolean = false): String {
            val s = str.split(Regex("[^\\p{Alnum}]")).joinToString("") { s ->
                s.lowercase(Locale.getDefault()).capitalized()
            }
            return if (capitalize) s else s.decapitalized()
        }

    }

    var useJSR310Types: Boolean = true
        set(value) {
            field = value
            JavaTypeResolver.useJSR310Types = value
        }

    var forceIntegers: Boolean = true
        set(value) {
            field = value
            JavaTypeResolver.forceIntegers = value
        }

    var forceBigDecimals: Boolean = false
        set(value) {
            field = value
            JavaTypeResolver.forceBigDecimals = value
        }

    val defaultDatasource: JDBCConnectionConfiguration by lazy { datasources[defaultModuleName]!! }

    /**
     * PlantUML 脚本目录
     */
    var pumlSrc: String = "puml"

    /**
     * 根路径
     */
    var rootPath: File? = null
        get() = if (field == null) {
            findUpPath(projectDir)
        } else field

    private val path: File? by lazy {
        if (File(projectDir, pumlSrc).exists())
            projectDir
        else rootPath
    }

    private fun findUpPath(file: File): File? {
        val parentFile = file.absoluteFile.parentFile
        return if (parentFile != null) {
            var pFile = File(parentFile, pumlSrc.substringBefore("/"))
            if (pFile.exists()) {
                parentFile
            } else {
                pFile = File(parentFile, pdmSrc)
                if (pFile.exists()) {
                    parentFile
                } else
                    findUpPath(parentFile)
            }
        } else
            null
    }

    fun datasource(moduleName: String): JDBCConnectionConfiguration {
        return datasources[moduleName] ?: defaultDatasource
    }

    fun file(subfile: String): File {
        val file = File(subfile)
        if (file.isAbsolute)
            return file
        return File(path, subfile)
    }

    fun isDefaultModule(moduleName: String): Boolean {
        return defaultModuleName == moduleName
    }

    val pumlSources: Map<String, List<File>> by lazy {
        file(pumlSrc).listFiles()?.filter { it.isDirectory && "database" != it.name }
            ?.associateBy(
                {
                    if ("src" == it.name) {
                        defaultModuleName
                    } else it.name
                }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "puml" }.toList() }
            ?: emptyMap()
    }

    val pdmSources: Map<String, List<File>> by lazy {
        file(pdmSrc).listFiles()?.filter { it.isDirectory && "database" != it.name }
            ?.associateBy(
                { if ("src" == it.name) "modules" else it.name }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "pdm" }.toList() }
            ?.toSortedMap { o1, o2 -> o1.compareTo(o2) }
            ?: TreeMap()
    }

    val pumlDatabaseSources: Map<String, List<File>> by lazy {
        file(pumlSrc).listFiles()?.filter { "database" == it.name }
            ?.associateBy({
                if ("src" == it.name) {
                    defaultModuleName
                } else it.name
            }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "puml" }.toList() }
            ?.toSortedMap { o1, o2 -> o1.compareTo(o2) }
            ?: TreeMap()
    }

    fun <T> run(dataType: DataType = this.dataType, function: (String, TableHolder) -> T): List<T> {
        return when (dataType) {
            DataType.DATABASE -> {
                datasources.map { (module, jdbc) ->
                    function(module, jdbc)
                }
            }

            DataType.PUML -> {
                pumlSources.map { (module, files) ->
                    function(module, PumlTableHolder(this, module, files))
                }
            }

            DataType.PDM -> {
                pdmSources.map { (module, files) ->
                    function(module, PdmTableHolder(this, module, files))
                }
            }
        }
    }
}
