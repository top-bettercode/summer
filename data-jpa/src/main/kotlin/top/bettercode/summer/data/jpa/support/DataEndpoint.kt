package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.lang.Nullable
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.result
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.web.support.ApplicationContextHolder
import javax.persistence.EntityManager
import javax.persistence.Tuple


/**
 * @author Peter Wu
 */
@Endpoint(id = "data")
open class DataEndpoint(
    private val entityManagers: List<EntityManager>,
    private val transactionManagers: List<PlatformTransactionManager>
) {

    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")

    private fun query(
        entityManager: EntityManager,
        sql: String,
        @Nullable page: Int?,
        @Nullable size: Int?
    ): Any {
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

    private fun update(
        transactionManager: PlatformTransactionManager,
        entityManager: EntityManager,
        sql: String
    ): Any {
        val def = DefaultTransactionDefinition()
        def.name = "update"
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status: TransactionStatus = transactionManager.getTransaction(def)
        val startMillis = System.currentTimeMillis()
        try {
            MDC.put(SqlAppender.MDC_SQL_ID, "Endpoint.update")
            val query = entityManager.createNativeQuery(sql)
            val affected = query.executeUpdate()
            transactionManager.commit(status)
            sqlLog.affected(affected.toString())
            return mapOf("affected" to affected)
        } catch (e: Exception) {
            // 如果发生异常，回滚事务
            transactionManager.rollback(status)
            MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
            throw e
        } finally {
            val duration = System.currentTimeMillis() - startMillis
            sqlLog.cost(duration)
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
            MDC.remove(SqlAppender.MDC_SQL_ID)
        }
    }

    private fun getEntityManager(db: String): EntityManager {
        return if (entityManagers.size == 1)
            entityManagers[0]
        else if (db == "d") {
            entityManagers.find { ApplicationContextHolder.getBeanName(it.entityManagerFactory) == "entityManagerFactory" }!!
        } else {
            entityManagers.find { ApplicationContextHolder.getBeanName(it.entityManagerFactory) == "${db}EntityManagerFactory" }!!
        }
    }

    private fun getTransactionManager(db: String): PlatformTransactionManager {
        return if (transactionManagers.size == 1)
            transactionManagers[0]
        else if (db == "d") {
            transactionManagers.find { ApplicationContextHolder.getBeanName(it) == "transactionManager" }!!
        } else {
            transactionManagers.find { ApplicationContextHolder.getBeanName(it) == "${db}TransactionManager" }!!
        }
    }

    @WriteOperation
    open fun write(
        @Selector ds: String, @Selector op: String, sql: String,
        @Nullable page: Int?,
        @Nullable size: Int?
    ): Any {
        return when (op) {
            "update" -> {
                update(getTransactionManager(ds), getEntityManager(ds), sql)
            }

            "query" -> {
                query(getEntityManager(ds), sql, page, size)
            }

            else -> {
                ""
            }
        }
    }

}