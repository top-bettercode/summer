package top.bettercode.summer.tools.lang.property

/**
 *
 * @author Peter Wu
 */
object Settings : HashMap<String, PropertiesSource>() {

    private const val serialVersionUID: Long = 1L

    @JvmStatic
    val cellRegex = PropertiesSource.of("cell-regex")

    @JvmStatic
    val jdbcTypeName = PropertiesSource.of("defaultJdbcTypeName", "jdbcTypeName")

    @JvmStatic
    val areaCode = PropertiesSource.of("areaCode")

    @JvmStatic
    val dicCode = PropertiesSource("default-dic-code", "dic-code", "app-dic-code")

    @JvmStatic
    val exceptionHandle = PropertiesSource.of("default-exception-handle", "exception-handle")

    init {
        put("cell-regex", cellRegex)
        put("jdbcTypeName", jdbcTypeName)
        put("areaCode", areaCode)
        put("dic-code", dicCode)
        put("exception-handle", exceptionHandle)
    }


    @JvmStatic
    fun isDicCode(baseName: String): Boolean {
        return "dic-code" == baseName
    }
}