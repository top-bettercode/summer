package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.type.JdbcType
import org.hibernate.type.descriptor.sql.JdbcTypeJavaClassMappings
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
        for (tuple in tuples) {
            val elements = tuple.elements
            for (i in elements.indices) {
                val element = elements[i]
                val alias = element.alias
                columnNames.add(i, alias)
                val javaType = element.javaType
                if (javaType != Any::class.java) {
                    classNames.add(i, javaType.name)
                    val type = JdbcTypeJavaClassMappings.INSTANCE.determineJdbcTypeCodeForJavaClass(javaType)
                    val jdbcType = JdbcType.forCode(type)
                    jdbcTypes.add(i, jdbcType)
                } else {
                    classNames.add(i, javaType.name)
                    jdbcTypes.add(i, JdbcType.VARCHAR)
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
        throw SQLFeatureNotSupportedException()
    }

    override fun isWritable(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun isDefinitelyWritable(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    override fun getColumnClassName(i: Int): String {
        return classNames[i - 1]
    }

    override fun <T> unwrap(aClass: Class<T>): T {
        throw SQLFeatureNotSupportedException()
    }

    override fun isWrapperFor(aClass: Class<*>?): Boolean {
        throw SQLFeatureNotSupportedException()
    }
}
