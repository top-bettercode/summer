package top.bettercode.summer.data.jpa.support

import org.springframework.data.jpa.domain.Specification
import top.bettercode.summer.data.jpa.metamodel.SingularAttributeValue
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaUpdate

/**
 *
 * @author Peter Wu
 */
interface UpdateSpecification<T> : Specification<T> {

    fun createCriteriaUpdate(domainClass: Class<T>, criteriaBuilder: CriteriaBuilder, extJpaSupport: ExtJpaSupport<T>): CriteriaUpdate<T>

    var idAttribute: SingularAttributeValue<T, *>?
    var versionAttribute: SingularAttributeValue<T, *>?
}