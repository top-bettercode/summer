package top.bettercode.summer.data.jpa.query.mybatis.hibernate

import org.hibernate.engine.query.internal.NativeQueryInterpreterStandardImpl
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.loader.custom.CustomLoader
import org.hibernate.loader.custom.CustomQuery

class MybatisQueryInterpreterStandardImpl(sessionFactory: SessionFactoryImplementor?) : NativeQueryInterpreterStandardImpl(sessionFactory) {
    @Deprecated("Deprecated in Java")
    override fun createCustomLoader(
            customQuery: CustomQuery,
            sessionFactory: SessionFactoryImplementor
    ): CustomLoader {
        return MybatisLoader(customQuery, sessionFactory)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
