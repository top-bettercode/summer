package top.bettercode.summer.tools.lang.property

/**
 *
 * @author Peter Wu
 */
object Settings : HashMap<String, PropertiesSource>() {
    private fun readResolve(): Any = Settings


    @JvmStatic
    val cellRegex = PropertiesSource.of("cell-regex")

    @JvmStatic
    val jdbcTypeName = PropertiesSource.of("defaultJdbcTypeName", "jdbcTypeName")

    @JvmStatic
    val division = PropertiesSource.of("division")

    @JvmStatic
    val dicCode = PropertiesSource("default-dic-code", "dic-code", "app-dic-code")

    @JvmStatic
    val exceptionHandle = PropertiesSource.of("default-exception-handle", "exception-handle")

    init {
        put("cell-regex", cellRegex)
        put("jdbcTypeName", jdbcTypeName)
        put("areaCode", division)
        put("dic-code", dicCode)
        put("exception-handle", exceptionHandle)
    }


    @JvmStatic
    fun isDicCode(baseName: String): Boolean {
        return "dic-code" == baseName
    }
}