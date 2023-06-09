package top.bettercode.summer.tools.generator.database.entity

data class Indexed(
        val unique: Boolean = false,
        val columnName: MutableList<String> = mutableListOf()
) {
    fun name(tableName: String): String = "${if (unique) "IDX_UK" else "IDX"}_${
        tableName.replace("_", "")
    }_${
        columnName.joinToString("_") { cname ->
            cname.replace(tableName, "").replace("_", "")
        }
    }"
}