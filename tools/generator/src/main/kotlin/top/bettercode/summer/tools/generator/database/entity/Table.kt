package top.bettercode.summer.tools.generator.database.entity

import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.JDBCConnectionConfiguration
import top.bettercode.summer.tools.lang.decapitalized

/**
 *
 * 表对应数据模型类
 *
 * @author Peter Wu
 */
data class Table(
        val productName: String,
        val catalog: String?,
        val schema: String?,
        /**
         * 表名
         */
        val tableName: String,
        /**
         * 表类型
         */
        val tableType: String,
        /**
         * 注释说明
         */
        var remarks: String,
        /**
         * 主键
         */
        var primaryKeyNames: List<String>,
        val indexes: MutableList<Indexed>,
        /**
         * 字段
         */
        var pumlColumns: List<Any>,
        val subModule: String = "database",
        val subModuleName: String = "database",
        val physicalOptions: String = "",
        val engine: String = ""
) {

    val primaryKeys: MutableList<Column>
    val columns: MutableList<Column> =
            pumlColumns.asSequence().filter { it is Column }.map {
                val col = it as Column
                col
            }.toMutableList()

    init {
        val iterator = indexes.iterator()
        while (iterator.hasNext()) {
            val indexed = iterator.next()
            if (primaryKeyNames.containsAll(indexed.columnName)) {
                iterator.remove()
            }
            if (indexed.columnName.size == 1) {
                val column = columns.find { it.columnName == indexed.columnName[0] }!!
                column.indexed = true
                column.unique = indexed.unique
            }
        }
        primaryKeys =
                columns.asSequence().filter { primaryKeyNames.contains(it.columnName) }.toMutableList()
        primaryKeys.forEach {
            it.isPrimary = true
            it.indexed = true
            it.unique = true
            it.nullable = false
        }
    }

    val primaryKey: Column? by lazy {
        if (primaryKeys.size == 1) {
            primaryKeys[0]
        } else {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Table) return false

        if (tableName != other.tableName) return false
        if (engine != other.engine) return false
        if (remarks != other.remarks) return false
        if (physicalOptions != other.physicalOptions) return false
        if (primaryKeyNames.size != other.primaryKeyNames.size || (primaryKeyNames - other.primaryKeyNames.toSet()).isNotEmpty() || (other.primaryKeyNames - primaryKeyNames.toSet()).isNotEmpty()) return false
        if (indexes.size != other.indexes.size || (indexes - other.indexes.toSet()).isNotEmpty() || (other.indexes - indexes.toSet()).isNotEmpty()) return false
        return !(pumlColumns.size != other.pumlColumns.size || (pumlColumns - other.pumlColumns.toSet()).isNotEmpty() || (other.pumlColumns - pumlColumns.toSet()).isNotEmpty())
    }

    override fun hashCode(): Int {
        var result = tableName.hashCode()
        result = 31 * result + engine.hashCode()
        result = 31 * result + remarks.hashCode()
        result = 31 * result + physicalOptions.hashCode()
        result = 31 * result + primaryKeyNames.hashCode()
        result = 31 * result + indexes.hashCode()
        result = 31 * result + pumlColumns.hashCode()
        return result
    }

    override fun toString(): String {
        return "Table(productName='$productName', catalog=$catalog, schema=$schema, tableName='$tableName', tableType='$tableType', remarks='$remarks', primaryKeyNames=$primaryKeyNames, indexes=$indexes, pumlColumns=$pumlColumns, subModule='$subModule', subModuleName='$subModuleName', physicalOptions='$physicalOptions', primaryKeys=$primaryKeys, columns=$columns, primaryKey=$primaryKey)"
    }

    //--------------------------------------------

    lateinit var module: String
    lateinit var ext: GeneratorExtension
    val datasource: JDBCConnectionConfiguration by lazy { ext.datasource(module) }

    val supportSoftDelete: Boolean
        get() = columns.find { it.softDelete } != null

    val className: String
        get() = datasource.className(tableName)

    val entityName: String get() = className.decapitalized()

    val pathName: String get() = entityName

}