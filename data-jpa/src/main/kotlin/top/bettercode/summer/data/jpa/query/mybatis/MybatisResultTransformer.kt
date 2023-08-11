package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.mapping.MappedStatement
import org.hibernate.HibernateException
import org.hibernate.transform.ResultTransformer
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.persistence.Tuple
import javax.persistence.TupleElement

/**
 * @author Peter Wu
 */
class MybatisResultTransformer(private val mappedStatement: MappedStatement?) : ResultTransformer {
    override fun transformTuple(tuple: Array<Any>, aliases: Array<String>): Any {
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

    private class NativeTupleElementImpl<X>(private val javaType: Class<out X>, private val alias: String) : TupleElement<X> {
        override fun getJavaType(): Class<out X> {
            return javaType
        }

        override fun getAlias(): String {
            return alias
        }
    }

    private class NativeTupleImpl(tuple: Array<Any>?, aliases: Array<String>?) : Tuple {
        private val tuple: Array<Any>
        private val aliasToValue: MutableMap<String, Any> = LinkedHashMap()
        private val aliasReferences: MutableMap<String, String> = LinkedHashMap()

        init {
            if (tuple == null) {
                throw HibernateException("Tuple must not be null")
            }
            if (aliases == null) {
                throw HibernateException("Aliases must not be null")
            }
            if (tuple.size != aliases.size) {
                throw HibernateException("Got different size of tuples and aliases")
            }
            this.tuple = tuple
            for (i in tuple.indices) {
                aliasToValue[aliases[i]] = tuple[i]
                aliasReferences[aliases[i].lowercase(Locale.getDefault())] = aliases[i]
            }
        }

        override fun <X> get(alias: String, type: Class<X>): X? {
            val untyped = get(alias)
            return if (untyped != null) type.cast(untyped) else null
        }

        override fun get(alias: String): Any? {
            val aliasReference = aliasReferences[alias.lowercase(Locale.getDefault())]
            if (aliasReference != null && aliasToValue.containsKey(aliasReference)) {
                return aliasToValue[aliasReference]
            }
            throw IllegalArgumentException("Unknown alias [$alias]")
        }

        override fun <X> get(i: Int, type: Class<X>): X? {
            val untyped = get(i)
            return type.cast(untyped)
        }

        override fun get(i: Int): Any {
            require(i >= 0) { "requested tuple index must be greater than zero" }
            require(i < aliasToValue.size) { "requested tuple index exceeds actual tuple size" }
            return tuple[i]
        }

        override fun toArray(): Array<Any> {
            // todo : make a copy?
            return tuple
        }

        override fun getElements(): List<TupleElement<*>> {
            val elements: MutableList<TupleElement<*>> = ArrayList(aliasToValue.size)
            for ((key, value) in aliasToValue) {
                elements.add(NativeTupleElementImpl(getValueClass(value), key))
            }
            return elements
        }

        private fun getValueClass(value: Any?): Class<*> {
            var valueClass: Class<*> = Any::class.java
            if (value != null) {
                valueClass = value.javaClass
            }
            return valueClass
        }

        override fun <X> get(tupleElement: TupleElement<X>): X? {
            return get(tupleElement.alias, tupleElement.javaType)
        }
    }

}
