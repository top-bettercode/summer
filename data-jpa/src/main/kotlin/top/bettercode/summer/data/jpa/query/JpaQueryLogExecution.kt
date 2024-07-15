package top.bettercode.summer.data.jpa.query

import org.springframework.data.jpa.repository.query.AbstractJpaQuery
import org.springframework.data.jpa.repository.query.JpaParametersParameterAccessor
import org.springframework.data.jpa.repository.query.JpaQueryExecution
import top.bettercode.summer.data.jpa.support.JpaUtil

/**
 * @author Peter Wu
 */
class JpaQueryLogExecution(private val delegate: JpaQueryExecution, private val id: String) :
    JpaQueryExecution() {
    override fun execute(
        query: AbstractJpaQuery,
        accessor: JpaParametersParameterAccessor
    ): Any? {
        val pageable = accessor.pageable
        return JpaUtil.mdcId(id = id, pageable = pageable) {
            delegate.execute(query, accessor)
        }
    }

    override fun doExecute(
        query: AbstractJpaQuery, accessor: JpaParametersParameterAccessor
    ): Any? {
        return null
    }
}
