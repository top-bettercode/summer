package top.bettercode.summer.tools.generator.database.entity

data class Indexed(
        val name: String? = null,
        val unique: Boolean = false,
        val columnName: MutableList<String> = mutableListOf()
) {
    fun name(tableName: String): String = if (name.isNullOrBlank()) "${if (unique) "IDX_UK" else "IDX"}_${
        tableName.replace("_", "")
    }_${
        columnName.joinToString("_") { cname ->
            cname.replace(tableName, "").replace("_", "")
        }
    }" else name
}