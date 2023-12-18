package top.bettercode.summer.data.jpa.config

import org.springframework.context.annotation.Configuration
import top.bettercode.summer.data.jpa.query.mybatis.hibernate.JdbcExtSelectExecutorStandardImpl

/**
 * JdbcSelectExecutorConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
class JdbcSelectExecutorConfiguration(mybatisProperties: MybatisProperties) {

    init {
        if (!mybatisProperties.useTupleTransformer) {
            JdbcExtSelectExecutorStandardImpl.changeInstance()
        }
    }

}
