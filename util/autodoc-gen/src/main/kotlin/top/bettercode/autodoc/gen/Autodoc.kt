package top.bettercode.autodoc.gen

/**
 * 接口文档数据模型
 */
object Autodoc {
    /**
     * 接口集合名称
     */
    @JvmStatic
    var collectionName: String = ""

    /**
     * 接口名称
     */
    @JvmStatic
    var name: String = ""

    @JvmStatic
    var version: String = ""

    /**
     * 接口描述
     */
    @JvmStatic
    var description: String = ""

    /**
     * 数据库 schema
     */
    @JvmStatic
    var schema: String? = null

    /**
     * 相关数据表名
     */
    @JvmStatic
    var tableNames: Set<String> = setOf()

    /**
     * 必填参数
     */
    @JvmStatic
    var requiredParameters: Set<String> = setOf()

    /**
     * 请求头必填参数
     */
    @JvmStatic
    var requiredHeaders: Set<String> = setOf()

    /**
     * 请求头
     */
    @JvmStatic
    var headers: Set<String> = setOf()

    /**
     * 是否启用文档数据生成
     */
    @JvmStatic
    var enable: Boolean = true

    /**
     * 异常时是否不生成文档
     */
    @JvmStatic
    var disableOnException: Boolean? = null


    /**
     * 字段描述
     */
    @JvmStatic
    var fields: MutableMap<String, String> = mutableMapOf()


    @JvmStatic
    fun collectionName(collectionName: String) {
        this.collectionName = collectionName
    }

    @JvmStatic
    fun name(name: String) {
        this.name = name
    }

    @JvmStatic
    fun version(version: String) {
        this.version = version
    }

    @JvmStatic
    fun description(description: String) {
        this.description = description
    }

    @JvmStatic
    fun schema(schema: String) {
        this.schema = schema
    }


    /**
     * 设置相关数据表名
     */
    @JvmStatic
    fun tableNames(vararg tableName: String) {
        tableNames = tableName.toSet()
    }

    /**
     * 设置必填参数
     */
    @JvmStatic
    fun requiredParameters(vararg parameter: String) {
        requiredParameters = parameter.toSet()
    }

    /**
     * 设置请求头必填参数
     */
    @JvmStatic
    fun requiredHeaders(vararg header: String) {
        requiredHeaders = header.toSet()
    }

    /**
     * 设置请求头
     */
    @JvmStatic
    fun headers(vararg header: String) {
        headers = header.toSet()
    }

    /**
     * 启用文档数据生成
     */
    @JvmStatic
    fun enable() {
        enable = true
    }

    /**
     * 关闭文档数据生成
     */
    @JvmStatic
    fun disable() {
        enable = false
    }

    @JvmStatic
    fun disableOnException() {
        disableOnException = true
    }

    @JvmStatic
    fun enableOnException() {
        disableOnException = false
    }

    /**
     * 设置字段描述
     */
    @JvmStatic
    fun field(name: String, description: String) {
        fields[name] = description
    }

}
