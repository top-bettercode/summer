package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.lang.Nullable
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.result
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import javax.persistence.EntityManager
import javax.persistence.Tuple

/**
 * @author Peter Wu
 */
@Endpoint(id = "query")
open class QueryEndpoint(private val entityManager: EntityManager) {

    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")

    @WriteOperation
    fun read(sql: String, @Nullable page: Int?, @Nullable size: Int?): Any {
        val sizeParam = size ?: 20
        val offset = page?.let { (it - 1).times(sizeParam) } ?: 0
        val startMillis = System.currentTimeMillis()
        try {
            MDC.put(SqlAppender.MDC_SQL_ID, "Endpoint.query")
            sqlLog.offset(offset.toLong())
            sqlLog.limit(sizeParam)
            val query = entityManager.createNativeQuery(sql, Tuple::class.java)
            @Suppress("DEPRECATION")
            query.unwrap(org.hibernate.query.Query::class.java)
                .setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE)
            query.firstResult = page?.let { (it - 1).times(size ?: 20) } ?: 0
            query.setMaxResults(size ?: 20)

            val result = query.resultList
            val resultSize = result.size
            sqlLog.retrieved(resultSize)
            if (result == null) {
                sqlLog.result("null")
            }
            return mapOf("size" to resultSize, "content" to result)
        } catch (e: Exception) {
            MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
            throw e
        } finally {
            val duration = System.currentTimeMillis() - startMillis
            sqlLog.cost(duration)
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
            MDC.remove(SqlAppender.MDC_SQL_ID)
        }
    }
}