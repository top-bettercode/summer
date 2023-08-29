package top.bettercode.summer.data.jpa.support

import org.springframework.data.jpa.domain.Specification
import top.bettercode.summer.data.jpa.metamodel.SingularAttributeValue
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaUpdate

/**
 *
 * @author Peter Wu
 */
interface UpdateSpecification<T> : Specification<T> {

    fun createCriteriaUpdate(domainClass: Class<T>, criteriaBuilder: CriteriaBuilder): CriteriaUpdate<T>

    var idAttribute: SingularAttributeValue<T, *>?
    var versionAttribute: SingularAttributeValue<T, *>?
}