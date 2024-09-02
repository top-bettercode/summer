package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.dom.java.element.JavaElement
import top.bettercode.summer.tools.generator.dsl.Generator
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.tools.lang.util.JavaTypeResolver
import java.io.File


/**
 * @author Peter Wu
 */
open class GeneratorExtension(
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
    var projectDir: File = FileUtil.userDirFile,
    /**
     * 基础路径下相对路径
     */
    var dir: String = "",

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
    var corePackageName: String = "",
    /**
     * 使用子模块
     */
    var userModule: Boolean = true,

    var applicationName: String = "",

    var projectName: String = "",

    var isCore: Boolean = false,
    var isCloud: Boolean = false,
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
    var logicalDeleteColumnName: String = "deleted",
    /**
     * 逻辑删除使用布尔值
     */
    var logicalDeleteAsBoolean: Boolean = true,
    /**
     * 缩进
     */
    var indent: String = JavaElement.DEFAULT_INDENT,
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


    fun setting(key: String): Any? = settings[key]

    fun setting(key: String, default: String): String {
        return settings[key] ?: return default
    }

    fun enable(key: String, default: Boolean = false): Boolean {
        return settings[key]?.toBoolean() ?: default
    }

    /**
     * JDBC连接配置
     */
    var databases: Map<String, DatabaseConfiguration> = mapOf()
        set(value) {
            field = value
            value.values.forEach {
                it.extension = this
            }
        }

    private val _databases: MutableMap<String, DatabaseConfiguration> = mutableMapOf()

    fun database(moduleName: String): DatabaseConfiguration {
        return databases[moduleName] ?: _databases.getOrPut(moduleName) {
            val database = (databases[DEFAULT_MODULE_NAME]
                ?: databases.values.firstOrNull() ?: DatabaseConfiguration()).copy()
            database.module = moduleName
            database.driver = DatabaseDriver.UNKNOWN
            database.extension = this
            database
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

    val isProjectPuml: Boolean
        get() = File(projectDir, pumlSrc).exists()

    val hasPuml: Boolean
        get() = file(pumlSrc).exists()

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


    fun file(subfile: String): File {
        val file = File(subfile)
        if (file.isAbsolute)
            return file
        return File(path, subfile)
    }


    val pumlSources: Map<String, List<File>> by lazy {
        file(pumlSrc).listFiles()?.filter { it.isDirectory && "database" != it.name }
            ?.associateBy(
                {
                    if ("src" == it.name) {
                        DEFAULT_MODULE_NAME
                    } else it.name
                }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "puml" }.toList() }
            ?.toSortedMap(comparator)
            ?: emptyMap()
    }

    val pdmSources: Map<String, List<File>> by lazy {
        file(pdmSrc).listFiles()?.filter { it.isDirectory && "database" != it.name }
            ?.associateBy(
                { if ("src" == it.name) DEFAULT_MODULE_NAME else it.name }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "pdm" }.toList() }
            ?.toSortedMap(comparator)
            ?: emptyMap()
    }

    val pumlDatabaseSources: Map<String, List<File>> by lazy {
        file(pumlSrc).listFiles()?.filter { "database" == it.name }
            ?.associateBy({
                if ("src" == it.name) {
                    DEFAULT_MODULE_NAME
                } else it.name
            }
            ) { it.walkTopDown().filter { f -> f.isFile && f.extension == "puml" }.toList() }
            ?.toSortedMap(comparator)
            ?: emptyMap()
    }

    fun <T> run(
        dataType: DataType = DataType.PUML,
        pumlSources: Map<String, List<File>> = this.pumlSources,
        function: (String, TableHolder) -> T
    ): List<T> {
        return when (dataType) {
            DataType.DATABASE -> {
                databases.map { (module, jdbc) ->
                    function(module, jdbc)
                }
            }

            DataType.PUML -> {
                pumlSources.map { (module, files) ->
                    function(module, PumlTableHolder(database(module), files))
                }
            }

            DataType.PDM -> {
                pdmSources.map { (module, files) ->
                    function(module, PdmTableHolder(database(module), files))
                }
            }
        }
    }

    val basePackageName: String
        get() =
            (if (projectPackage) "${packageName}.${
                projectName.replace(
                    '-',
                    '.'
                )
            }" else packageName)

    val moduleSize: Int
        get() {
            return pumlSources.size
        }
    val modules: Array<String>
        get() {
            return pumlSources.keys.toTypedArray()
        }

    companion object {

        /**
         * Comparator 比较 key 默认认模块名(defaultModuleName)排最前，其他按字母排序
         */
        val comparator: Comparator<String> = Comparator { o1, o2 ->
            if (o1 == o2) {
                0
            } else if (o1 == DEFAULT_MODULE_NAME) {
                -1
            } else if (o2 == DEFAULT_MODULE_NAME) {
                1
            } else {
                o1.compareTo(o2)
            }
        }


        const val DEFAULT_MODULE_NAME: String = "app"

        fun isDefaultModule(moduleName: String): Boolean {
            return DEFAULT_MODULE_NAME == moduleName
        }

    }

}
