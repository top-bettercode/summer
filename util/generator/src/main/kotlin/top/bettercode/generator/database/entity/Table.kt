package top.bettercode.generator.database.entity

import top.bettercode.generator.GeneratorExtension
import org.atteo.evo.inflector.English

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
    val physicalOptions: String = "",
    var sequenceStartWith: Int? = null,
    val moduleName: String = "database"
) {


    val primaryKeys: MutableList<Column>
    val columns: MutableList<Column> =
        pumlColumns.asSequence().filter { it is Column }.map { it as Column }.toMutableList()

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

    fun className(extension: GeneratorExtension): String = extension.className(tableName)

    fun entityName(extension: GeneratorExtension): String = className(extension).decapitalize()

    fun pathName(extension: GeneratorExtension): String = English.plural(entityName(extension))

    fun supportSoftDelete(extension: GeneratorExtension): Boolean =
        columns.find { it.isSoftDelete(extension) } != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Table) return false

        if (tableName != other.tableName) return false
        if (remarks != other.remarks) return false
        if (sequenceStartWith != other.sequenceStartWith) return false
        if (physicalOptions != other.physicalOptions) return false
        if (primaryKeyNames.size != other.primaryKeyNames.size || (primaryKeyNames - other.primaryKeyNames).isNotEmpty() || (other.primaryKeyNames - primaryKeyNames).isNotEmpty()) return false
        if (indexes.size != other.indexes.size || (indexes - other.indexes).isNotEmpty() || (other.indexes - indexes).isNotEmpty()) return false
        if (pumlColumns.size != other.pumlColumns.size || (pumlColumns - other.pumlColumns).isNotEmpty() || (other.pumlColumns - pumlColumns).isNotEmpty()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tableName.hashCode()
        result = 31 * result + remarks.hashCode()
        result = 31 * result + physicalOptions.hashCode()
        result = 31 * result + (sequenceStartWith ?: 0)
        result = 31 * result + primaryKeyNames.hashCode()
        result = 31 * result + indexes.hashCode()
        result = 31 * result + pumlColumns.hashCode()
        return result
    }
}