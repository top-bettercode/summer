package top.bettercode.summer.data.jpa.query.mybatis.hibernate

import org.hibernate.engine.query.internal.NativeQueryInterpreterStandardImpl
import org.hibernate.engine.query.spi.QueryPlanCache
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.loader.custom.CustomLoader
import org.hibernate.loader.custom.CustomQuery
import javax.persistence.EntityManager

class MybatisQueryInterpreterStandardImpl(sessionFactory: SessionFactoryImplementor?) : NativeQueryInterpreterStandardImpl(sessionFactory) {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun createCustomLoader(
            customQuery: CustomQuery,
            sessionFactory: SessionFactoryImplementor
    ): CustomLoader {
        return MybatisLoader(customQuery, sessionFactory)
    }

    companion object {

        fun changeNativeQueryInterpreter(entityManager: EntityManager) {
            val factoryImplementor = entityManager.entityManagerFactory
                    .unwrap(SessionFactoryImplementor::class.java)
            @Suppress("DEPRECATION") val queryPlanCache = factoryImplementor.queryPlanCache
            val field = QueryPlanCache::class.java.getDeclaredField("nativeQueryInterpreter")
            field.isAccessible = true
            field[queryPlanCache] = MybatisQueryInterpreterStandardImpl(factoryImplementor)
        }

    }
}
