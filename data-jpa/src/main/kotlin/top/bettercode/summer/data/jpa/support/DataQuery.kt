package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
class DataQuery(
    private val entityManagers: List<EntityManager>,
    private val transactionManagers: List<PlatformTransactionManager>
) {
    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")
    private val defaultDS = "d"

    @JvmOverloads
    fun query(
        ds: String = defaultDS,
        sql: String,
        @Nullable page: Int? = null,
        @Nullable size: Int? = null
    ): Any {
        val sizeParam = size ?: 20
        val offset = page?.let { (it - 1).times(sizeParam) } ?: 0
        val startMillis = System.currentTimeMillis()
        try {
            val entityManager = getEntityManager(ds)
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
            val resultSize = if (result == null) {
                sqlLog.result("null")
                0
            } else {
                result.size
            }
            sqlLog.retrieved(resultSize)

            return mapOf("size" to resultSize, "content" to result)
        } catch (e: Exception) {
            MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
            return mapOf("error" to e.message)
        } finally {
            val duration = System.currentTimeMillis() - startMillis
            sqlLog.cost(duration)
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
            MDC.remove(SqlAppender.MDC_SQL_ID)
        }
    }

    @JvmOverloads
    fun update(ds: String = defaultDS, sql: String): Any {
        val entityManager = getEntityManager(ds)
        val transactionManager = getTransactionManager(ds)
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
            return mapOf("error" to e.message)
        } finally {
            val duration = System.currentTimeMillis() - startMillis
            sqlLog.cost(duration)
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
            MDC.remove(SqlAppender.MDC_SQL_ID)
        }
    }

    fun getDs(): List<String> {
        return if (entityManagers.size == 1)
            listOf(defaultDS)
        else
            entityManagers.map {
                val beanName = ApplicationContextHolder.getBeanName(it.entityManagerFactory)!!
                if ("entityManagerFactory" == beanName) {
                    "d"
                } else
                    beanName
                        .substringBefore("EntityManagerFactory")
            }.sortedWith { o1, o2 ->
                if (o1 == defaultDS) {
                    -1
                } else if (o2 == defaultDS) {
                    1
                } else {
                    o1.compareTo(o2)
                }
            }
    }

    private fun getEntityManager(ds: String): EntityManager {
        return if (entityManagers.size == 1)
            entityManagers[0]
        else if (ds == defaultDS) {
            entityManagers.find { ApplicationContextHolder.getBeanName(it.entityManagerFactory) == "entityManagerFactory" }!!
        } else {
            entityManagers.find { ApplicationContextHolder.getBeanName(it.entityManagerFactory) == "${ds}EntityManagerFactory" }!!
        }
    }

    private fun getTransactionManager(ds: String): PlatformTransactionManager {
        return if (transactionManagers.size == 1)
            transactionManagers[0]
        else if (ds == defaultDS) {
            transactionManagers.find { ApplicationContextHolder.getBeanName(it) == "transactionManager" }!!
        } else {
            transactionManagers.find { ApplicationContextHolder.getBeanName(it) == "${ds}TransactionManager" }!!
        }
    }

}