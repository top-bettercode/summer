package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import javax.persistence.EntityManager
import javax.transaction.Transactional

/**
 * @author Peter Wu
 */
@Endpoint(id = "update")
open class UpdateEndpoint(private val entityManager: EntityManager) {

    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")

    @WriteOperation
    @Transactional
    open fun write(sql: String): Any {
        val startMillis = System.currentTimeMillis()
        try {
            MDC.put(SqlAppender.MDC_SQL_ID, "Endpoint.update")
            val query = entityManager.createNativeQuery(sql)
            val affected = query.executeUpdate()
            sqlLog.affected(affected.toString())
            return mapOf("affected" to affected)
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