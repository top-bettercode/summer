package cn.bestwu.generator.database.domain

data class Indexed(
        val name: String,
        val unique: Boolean = false,
        val columnName: MutableList<String> = mutableListOf()
)