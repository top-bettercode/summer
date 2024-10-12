package top.bettercode.summer.test.autodoc

import top.bettercode.summer.tools.autodoc.model.Field

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
    val tableNames: LinkedHashSet<String> = linkedSetOf()

    /**
     * 必填参数
     */
    @JvmStatic
    val requiredParameters: MutableSet<String> = mutableSetOf()

    /**
     * 请求头必填参数
     */
    @JvmStatic
    val requiredHeaders: MutableSet<String> = mutableSetOf()

    /**
     * 忽略请求头参数
     */
    @JvmStatic
    val ignoredHeaders: MutableSet<String> = mutableSetOf()

    /**
     * 请求头
     */
    @JvmStatic
    val headers: MutableSet<String> = mutableSetOf()

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
     * 是否需要授权
     */
    @JvmStatic
    var requireAuthorization: Boolean = false

    /**
     * 字段描述
     */
    @JvmStatic
    val fields: LinkedHashMap<String, LinkedHashSet<Field>> = linkedMapOf()

    val extedTypes: MutableSet<Class<*>> = mutableSetOf()

    @JvmStatic
    fun collectionName(collectionName: String) {
        Autodoc.collectionName = collectionName
    }

    @JvmStatic
    fun name(name: String) {
        Autodoc.name = name
    }

    @JvmStatic
    fun version(version: String) {
        Autodoc.version = version
    }

    @JvmStatic
    fun description(description: String) {
        Autodoc.description = description
    }

    @JvmStatic
    fun schema(schema: String) {
        Autodoc.schema = schema
    }


    /**
     * 设置相关数据表名
     */
    @JvmStatic
    fun tableNames(vararg tableName: String) {
        tableNames.addAll(tableName)
    }

    /**
     * 设置必填参数
     */
    @JvmStatic
    fun requiredParameters(vararg parameter: String) {
        requiredParameters.addAll(parameter)
    }

    /**
     * 设置请求头必填参数
     */
    @JvmStatic
    fun requiredHeaders(vararg header: String) {
        requiredHeaders.addAll(header)
    }

    @JvmStatic
    fun ignoredHeaders(vararg header: String) {
        ignoredHeaders.addAll(header)
    }

    /**
     * 设置是否需要授权
     */
    @JvmStatic
    fun requireAuthorization() {
        requireAuthorization = true
    }

    /**
     * 设置请求头
     */
    @JvmStatic
    fun headers(vararg header: String) {
        headers.addAll(header)
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
        fields.computeIfAbsent("DEFAULT") { LinkedHashSet() }
            .add(Field(name = name, description = description, canCover = false))
    }

    fun reset() {
        collectionName = ""
        name = ""
        version = ""
        description = ""
        schema = null
        tableNames.clear()
        requiredParameters.clear()
        requiredHeaders.clear()
        ignoredHeaders.clear()
        headers.clear()
        enable = true
        disableOnException = null
        requireAuthorization = false
        fields.clear()
        extedTypes.clear()
    }
}
