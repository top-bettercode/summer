package top.bettercode.summer.data.jpa.support

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.MDC_SQL_LIMIT
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.MDC_SQL_OFFSET
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.end
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.result
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.start
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.sql.Date
import java.text.SimpleDateFormat
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
    private val defaultDS = "primary"
    private val objectMapper: ObjectMapper
        get() {
            val objectMapper = ObjectMapper()
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            objectMapper.setDateFormat(dateFormat)


            // 创建模块并注册自定义序列化器
            val module = SimpleModule()
            module.addSerializer(Date::class.java, object : JsonSerializer<Date>() {
                override fun serialize(p0: Date, p1: JsonGenerator, p2: SerializerProvider?) {
                    p1.writeString(dateFormat.format(p0))
                }
            })
            // 将模块注册到 ObjectMapper
            objectMapper.registerModule(module)

            val serializationConfig = objectMapper.serializationConfig
            val config = serializationConfig.with(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
            objectMapper.setConfig(config)
            return objectMapper
        }

    @JvmOverloads
    fun query(
        ds: String = defaultDS,
        sql: String,
        page: Int,
        size: Int
    ): Any {
        try {
            val entityManager = getEntityManager(ds)
            MDC.put(SqlAppender.MDC_SQL_ID, "Endpoint.query")
            sqlLog.start()
            val start = System.currentTimeMillis()
            val query = entityManager.createNativeQuery(sql, Tuple::class.java)
            @Suppress("DEPRECATION")
            query.unwrap(org.hibernate.query.Query::class.java)
                .setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
            if (page > 0) {
                val offset = (page - 1).times(size)
                query.firstResult = offset
                query.setMaxResults(size)
                sqlLog.offset(offset.toLong())
                sqlLog.limit(size)
            }

            val result = query.resultList
            val resultSize = if (result == null) {
                sqlLog.result("null")
                0
            } else {
                result.size
            }
            sqlLog.retrieved(resultSize)
            val duration = System.currentTimeMillis() - start

            val map = mapOf("size" to resultSize, "duration" to duration, "content" to result)
            return objectMapper.writeValueAsBytes(map)
        } catch (e: Exception) {
            MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
            return mapOf("error" to e.message)
        } finally {
            sqlLog.end()
            MDC.remove(SqlAppender.MDC_SQL_ID)
            MDC.remove(MDC_SQL_OFFSET)
            MDC.remove(MDC_SQL_LIMIT)
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
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
        try {
            MDC.put(SqlAppender.MDC_SQL_ID, "Endpoint.update")
            sqlLog.start()
            val start = System.currentTimeMillis()
            val query = entityManager.createNativeQuery(sql)
            val affected = query.executeUpdate()
            transactionManager.commit(status)
            sqlLog.affected(affected.toString())
            val duration = System.currentTimeMillis() - start
            return mapOf("affected" to affected, "duration" to duration)
        } catch (e: Exception) {
            // 如果发生异常，回滚事务
            transactionManager.rollback(status)
            MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
            return mapOf("error" to e.message)
        } finally {
            sqlLog.end()
            MDC.remove(SqlAppender.MDC_SQL_ID)
            MDC.remove(SqlAppender.MDC_SQL_ERROR)
        }
    }

    fun getDs(): List<String> {
        return if (entityManagers.size == 1)
            listOf(defaultDS)
        else
            entityManagers.map {
                val beanName = ApplicationContextHolder.getBeanName(it.entityManagerFactory)!!
                if ("entityManagerFactory" == beanName) {
                    defaultDS
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