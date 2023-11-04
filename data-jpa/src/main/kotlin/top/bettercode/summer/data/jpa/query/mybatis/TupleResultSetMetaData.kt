package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.type.JdbcType
import java.sql.ResultSetMetaData
import java.sql.SQLFeatureNotSupportedException
import javax.persistence.Tuple

/**
 * @author Peter Wu
 */
class TupleResultSetMetaData(tuples: List<Tuple>) : ResultSetMetaData {
    private val columnNames: MutableList<String> = ArrayList()
    private val classNames: MutableList<String> = ArrayList()
    private val jdbcTypes: MutableList<JdbcType> = ArrayList()

    init {
        val size = tuples.size
        if (size > 0) {
            val firstTuple = tuples[0]
            val firstElements = firstTuple.elements
            val indexes = mutableListOf<Int>()
            firstElements.indices.forEach { i ->
                val element = firstElements[i] as NativeTupleElementImpl
                columnNames.add(element.alias)
                classNames.add(element.javaType.name)
                val jdbcType = element.getJdbcType()
                jdbcTypes.add(jdbcType)
                if (jdbcType == JdbcType.NULL) {
                    indexes.add(i)
                }
            }

            if (indexes.isNotEmpty()) {
                for (t in 1 until size) {
                    val tuple = tuples[t]
                    val elements = tuple.elements
                    val iterator = indexes.iterator()
                    iterator.forEach { index ->
                        val element = elements[index] as NativeTupleElementImpl
                        val jdbcType = element.getJdbcType()
                        if (jdbcType != JdbcType.NULL) {
                            classNames[index] = element.javaType.name
                            jdbcTypes[index] = jdbcType
                            iterator.remove()
                        }
                    }
                    if (indexes.isEmpty()) {
                        break
                    }
                }
            }
        }
    }

    fun findColumn(columnName: String): Int {
        return columnNames.indexOf(columnName)
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun isAutoIncrement(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun isCaseSensitive(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun isSearchable(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun isCurrency(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun isNullable(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun isSigned(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun getColumnDisplaySize(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun getColumnLabel(i: Int): String {
        return columnNames[i - 1]
    }

    override fun getColumnName(i: Int): String {
        return columnNames[i - 1]
    }

    override fun getSchemaName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun getPrecision(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun getScale(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    override fun getTableName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun getCatalogName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun getColumnType(i: Int): Int {
        return jdbcTypes[i - 1].TYPE_CODE
    }

    override fun getColumnTypeName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    override fun isReadOnly(i: Int): Boolean {
        return true
    }

    override fun isWritable(i: Int): Boolean {
        return false
    }

    override fun isDefinitelyWritable(i: Int): Boolean {
        return false
    }

    override fun getColumnClassName(i: Int): String {
        return classNames[i - 1]
    }

    override fun <T> unwrap(iface: Class<T>): T {
        return iface.cast(this)
    }

    override fun isWrapperFor(aClass: Class<*>): Boolean {
        return aClass.isInstance(this)
    }
}
