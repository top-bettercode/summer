package top.bettercode.summer.data.jpa.query.mybatis

import java.util.*
import java.util.stream.Stream
import javax.persistence.*

/**
 * @author Peter Wu
 */
class MybatisQuery(
        val queryString: String,
        val query: Query,
        val mybatisParam: MybatisParam
) : Query {

    override fun getResultList(): List<*> {
        return query.resultList
    }

    override fun getResultStream(): Stream<*> {
        return query.resultStream
    }

    override fun getSingleResult(): Any? {
        return query.singleResult
    }

    override fun executeUpdate(): Int {
        return query.executeUpdate()
    }

    override fun setMaxResults(maxResult: Int): Query {
        return query.setMaxResults(maxResult)
    }

    override fun getMaxResults(): Int {
        return query.maxResults
    }

    override fun setFirstResult(startPosition: Int): Query {
        return query.setFirstResult(startPosition)
    }

    override fun getFirstResult(): Int {
        return query.firstResult
    }

    override fun setHint(hintName: String, value: Any): Query {
        return query.setHint(hintName, value)
    }

    override fun getHints(): Map<String, Any> {
        return query.hints
    }

    override fun <T> setParameter(param: Parameter<T>, value: T): Query {
        return query.setParameter(param, value)
    }

    override fun setParameter(
            param: Parameter<Calendar>,
            value: Calendar, temporalType: TemporalType
    ): Query {
        return query.setParameter(param, value, temporalType)
    }

    override fun setParameter(
            param: Parameter<Date>, value: Date,
            temporalType: TemporalType
    ): Query {
        return query.setParameter(param, value, temporalType)
    }

    override fun setParameter(name: String, value: Any): Query {
        return query.setParameter(name, value)
    }

    override fun setParameter(
            name: String, value: Calendar,
            temporalType: TemporalType
    ): Query {
        return query.setParameter(name, value, temporalType)
    }

    override fun setParameter(
            name: String, value: Date,
            temporalType: TemporalType
    ): Query {
        return query.setParameter(name, value, temporalType)
    }

    override fun setParameter(position: Int, value: Any): Query {
        return query.setParameter(position, value)
    }

    override fun setParameter(
            position: Int, value: Calendar,
            temporalType: TemporalType
    ): Query {
        return query.setParameter(position, value, temporalType)
    }

    override fun setParameter(
            position: Int, value: Date,
            temporalType: TemporalType
    ): Query {
        return query.setParameter(position, value, temporalType)
    }

    override fun getParameters(): Set<Parameter<*>> {
        return query.parameters
    }

    override fun getParameter(name: String): Parameter<*> {
        return query.getParameter(name)
    }

    override fun <T> getParameter(name: String, type: Class<T>): Parameter<T> {
        return query.getParameter(name, type)
    }

    override fun getParameter(position: Int): Parameter<*> {
        return query.getParameter(position)
    }

    override fun <T> getParameter(position: Int, type: Class<T>): Parameter<T> {
        return query.getParameter(position, type)
    }

    override fun isBound(param: Parameter<*>?): Boolean {
        return query.isBound(param)
    }

    override fun <T> getParameterValue(param: Parameter<T>): T {
        return query.getParameterValue(param)
    }

    override fun getParameterValue(name: String): Any {
        return query.getParameterValue(name)
    }

    override fun getParameterValue(position: Int): Any {
        return query.getParameterValue(position)
    }

    override fun setFlushMode(flushMode: FlushModeType): Query {
        return query.setFlushMode(flushMode)
    }

    override fun getFlushMode(): FlushModeType {
        return query.flushMode
    }

    override fun setLockMode(lockMode: LockModeType): Query {
        return query.setLockMode(lockMode)
    }

    override fun getLockMode(): LockModeType {
        return query.lockMode
    }

    override fun <T> unwrap(cls: Class<T>): T {
        return query.unwrap(cls)
    }
}
