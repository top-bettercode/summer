package top.bettercode.summer.data.jpa.support

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import javax.persistence.EntityManager
import javax.transaction.Transactional

/**
 * @author Peter Wu
 */
@Endpoint(id = "update")
open class UpdateEndpoint(private val entityManager: EntityManager) {

    @WriteOperation
    @Transactional
    open fun write(sql: String): Any {
        val query = entityManager.createNativeQuery(sql)
        val affected = query.executeUpdate()
        return mapOf("affected" to affected)
    }

}