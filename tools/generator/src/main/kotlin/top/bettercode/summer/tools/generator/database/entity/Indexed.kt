package top.bettercode.summer.tools.generator.database.entity

data class Indexed(
        val unique: Boolean = false,
        val columnName: MutableList<String> = mutableListOf(),
        val indexName: String? = null
) {
    fun name(tableName: String): String = indexName?:"${if (unique) "IDX_UK" else "IDX"}_${
        tableName.replace("_", "").takeLast(10)
    }_${
        columnName.joinToString("_") { cname ->
            cname.replace(tableName, "").replace("_", "")
        }
    }".take(30)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Indexed) return false

        if (unique != other.unique) return false
        if (columnName != other.columnName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unique.hashCode()
        result = 31 * result + columnName.hashCode()
        return result
    }


}