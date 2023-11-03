package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.mapping.MappedStatement
import org.hibernate.transform.ResultTransformer
import java.sql.ResultSet
import java.sql.SQLException
import javax.persistence.Tuple

/**
 * @author Peter Wu
 */
class MybatisResultTransformer(private val mappedStatement: MappedStatement?) : ResultTransformer {

    override fun transformTuple(tuple: Array<Any?>, aliases: Array<String>): Any {
        return NativeTupleImpl(tuple, aliases)
    }

    override fun transformList(list: List<*>): List<Any?> {
        @Suppress("UNCHECKED_CAST")
        return transformListResultSet(TupleResultSet(list as List<Tuple>), Int.MAX_VALUE)
    }

    fun transformResultSet(resultSet: ResultSet?): Any? {
        return this.transformListResultSet(resultSet, 1)[0]
    }

    fun transformListResultSet(resultSet: ResultSet): List<Any?> {
        return transformListResultSet(resultSet, resultSet.fetchSize)
    }

    fun transformListResultSet(resultSet: ResultSet?, maxRows: Int): List<Any?> {
        var maxRows1 = maxRows
        return try {
            if (maxRows1 == 0) {
                maxRows1 = Int.MAX_VALUE
            }
            MybatisResultSetHandler(mappedStatement).handleResultSets(resultSet, maxRows1)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

}
