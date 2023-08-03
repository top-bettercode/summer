package top.bettercode.summer.data.jpa.config

import org.hibernate.engine.query.spi.QueryPlanCache
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.data.jpa.query.mybatis.hibernate.MybatisQueryInterpreterStandardImpl
import java.util.function.Consumer
import javax.persistence.EntityManager

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
class JpaMybatisEntityManagerConfiguration(
        entityManagers: List<EntityManager>,
        mybatisProperties: MybatisProperties
) {
    init {
        if (!mybatisProperties.useTupleTransformer) {
            entityManagers.forEach(Consumer { entityManager: EntityManager ->
                val factoryImplementor = entityManager.entityManagerFactory
                        .unwrap(SessionFactoryImplementor::class.java)
                @Suppress("DEPRECATION") val queryPlanCache = factoryImplementor.queryPlanCache
                val field = QueryPlanCache::class.java.getDeclaredField("nativeQueryInterpreter")
                field.isAccessible = true
                field[queryPlanCache] = MybatisQueryInterpreterStandardImpl(factoryImplementor)
            })
        }
    }
}
