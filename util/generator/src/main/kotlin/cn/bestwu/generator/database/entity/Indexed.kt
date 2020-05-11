package cn.bestwu.generator.database.entity

data class Indexed(
        val name: String,
        val unique: Boolean = false,
        val columnName: MutableList<String> = mutableListOf()
)