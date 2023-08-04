package top.bettercode.summer.data.jpa.query

import org.slf4j.MDC
import org.springframework.data.jpa.repository.query.AbstractJpaQuery
import org.springframework.data.jpa.repository.query.JpaParametersParameterAccessor
import org.springframework.data.jpa.repository.query.JpaQueryExecution

/**
 * @author Peter Wu
 */
class JpaQueryLogExecution(private val delegate: JpaQueryExecution, private val id: String?) : JpaQueryExecution() {
    override fun execute(
            query: AbstractJpaQuery,
            accessor: JpaParametersParameterAccessor
    ): Any? {
        return try {
            MDC.put("id", id)
            delegate.execute(query, accessor)
        } finally {
            MDC.remove("id")
        }
    }

    override fun doExecute(
            query: AbstractJpaQuery, accessor: JpaParametersParameterAccessor
    ): Any? {
        return null
    }
}
