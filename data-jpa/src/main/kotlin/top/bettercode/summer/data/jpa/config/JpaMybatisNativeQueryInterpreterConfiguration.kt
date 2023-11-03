package top.bettercode.summer.data.jpa.config

import org.springframework.context.annotation.Configuration
import top.bettercode.summer.data.jpa.query.mybatis.hibernate.MybatisQueryInterpreterStandardImpl
import javax.persistence.EntityManager

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
class JpaMybatisNativeQueryInterpreterConfiguration(
        entityManagers: List<EntityManager>,
        mybatisProperties: MybatisProperties
) {
    init {
        if (!mybatisProperties.useTupleTransformer) {
            entityManagers.forEach { entityManager: EntityManager ->
                MybatisQueryInterpreterStandardImpl.changeNativeQueryInterpreter(entityManager)
            }
        }
    }
}
