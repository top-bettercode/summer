package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.mapping.MappedStatement
import org.hibernate.transform.ResultTransformer
import java.sql.ResultSet
import javax.persistence.Tuple

/**
 * @author Peter Wu
 */
class MybatisResultTransformer(
    private val mappedStatement: MappedStatement,
    private val isStreamQuery: Boolean
) : ResultTransformer {

    private var resultSetMetaData: TupleResultSetMetaData? = null
    var autoCloseResultSet = true

    override fun transformTuple(tuple: Array<Any?>, aliases: Array<String>): Any {
        val tupleImpl = NativeTupleImpl(tuple, aliases)
        if (isStreamQuery) {
            val tuples = listOf(tupleImpl)
            val resultSetMetaData: TupleResultSetMetaData
            if (this.resultSetMetaData == null) {
                synchronized(this) {
                    if (this.resultSetMetaData == null) {
                        resultSetMetaData = TupleResultSetMetaData(tuples)
                        if (resultSetMetaData.complete) {
                            this.resultSetMetaData = resultSetMetaData
                        }
                    } else {
                        resultSetMetaData = this.resultSetMetaData!!
                    }
                }
            } else {
                resultSetMetaData = this.resultSetMetaData!!
            }
            val resultSet = TupleResultSet(tuples, resultSetMetaData)
            return MybatisResultSetHandler(mappedStatement, autoCloseResultSet).handleResultSets(
                resultSet,
                Int.MAX_VALUE
            ).first()!!
        } else {
            return tupleImpl
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun transformList(list: List<*>): List<Any?> {
        val tuples = list as List<Tuple>
        val resultSetMetaData: TupleResultSetMetaData
        if (this.resultSetMetaData == null) {
            synchronized(this) {
                if (this.resultSetMetaData == null) {
                    resultSetMetaData = TupleResultSetMetaData(tuples)
                    if (resultSetMetaData.complete) {
                        this.resultSetMetaData = resultSetMetaData
                    }
                } else {
                    resultSetMetaData = this.resultSetMetaData!!
                }
            }
        } else {
            resultSetMetaData = this.resultSetMetaData!!
        }
        val resultSet = TupleResultSet(tuples, resultSetMetaData)
        return MybatisResultSetHandler(mappedStatement, autoCloseResultSet).handleResultSets(
            resultSet,
            Int.MAX_VALUE
        )
    }

    fun transformResultSet(resultSet: ResultSet?): Any? {
        return this.transformListResultSet(resultSet, 1)[0]
    }

    fun transformListResultSet(resultSet: ResultSet): List<Any?> {
        return transformListResultSet(resultSet, resultSet.fetchSize)
    }

    fun transformListResultSet(resultSet: ResultSet?, maxRows: Int): List<Any?> {
        var maxRows1 = maxRows
        if (maxRows1 == 0) {
            maxRows1 = Int.MAX_VALUE
        }
        return MybatisResultSetHandler(mappedStatement, autoCloseResultSet).handleResultSets(
            resultSet,
            maxRows1
        )
    }

}
