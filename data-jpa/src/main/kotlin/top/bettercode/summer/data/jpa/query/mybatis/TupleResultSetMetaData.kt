package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.type.JdbcType
import org.hibernate.type.descriptor.sql.JdbcTypeJavaClassMappings
import java.sql.*
import javax.persistence.*

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
                } else if (classNames.size <= i) {
                    classNames.add(i, javaType.name)
                    jdbcTypes.add(i, JdbcType.VARCHAR)
                }
            }
        }
    }

    fun findColumn(columnName: String): Int {
        return columnNames.indexOf(columnName)
    }

    @Throws(SQLException::class)
    override fun getColumnCount(): Int {
        return columnNames.size
    }

    @Throws(SQLException::class)
    override fun isAutoIncrement(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isCaseSensitive(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isSearchable(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isCurrency(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isNullable(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isSigned(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getColumnDisplaySize(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getColumnLabel(i: Int): String {
        return columnNames[i - 1]
    }

    @Throws(SQLException::class)
    override fun getColumnName(i: Int): String {
        return columnNames[i - 1]
    }

    @Throws(SQLException::class)
    override fun getSchemaName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getPrecision(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getScale(i: Int): Int {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getTableName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getCatalogName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getColumnType(i: Int): Int {
        return jdbcTypes[i - 1].TYPE_CODE
    }

    @Throws(SQLException::class)
    override fun getColumnTypeName(i: Int): String {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isReadOnly(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isWritable(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isDefinitelyWritable(i: Int): Boolean {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun getColumnClassName(i: Int): String {
        return classNames[i - 1]
    }

    @Throws(SQLException::class)
    override fun <T> unwrap(aClass: Class<T>): T {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun isWrapperFor(aClass: Class<*>?): Boolean {
        throw SQLFeatureNotSupportedException()
    }
}
