package top.bettercode.summer.data.jpa.support

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.lang.Nullable
import javax.persistence.EntityManager
import javax.persistence.Tuple

/**
 * @author Peter Wu
 */
@Endpoint(id = "query")
open class QueryEndpoint(private val entityManager: EntityManager) {

    @WriteOperation
    fun read(sql: String, @Nullable page: Int?, @Nullable size: Int?): Any {
        val query = entityManager.createNativeQuery(sql, Tuple::class.java)
        @Suppress("DEPRECATION")
        query.unwrap(org.hibernate.query.Query::class.java)
            .setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE)
        query.firstResult = page?.let { (it - 1).times(size ?: 20) } ?: 0
        query.setMaxResults(size ?: 20)

        val resultList = query.resultList
        return mapOf("size" to resultList.size, "content" to resultList)
    }
}