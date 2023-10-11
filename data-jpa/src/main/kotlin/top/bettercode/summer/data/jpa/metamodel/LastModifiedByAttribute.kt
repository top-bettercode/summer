package top.bettercode.summer.data.jpa.metamodel

import org.springframework.data.domain.AuditorAware
import top.bettercode.summer.web.support.ApplicationContextHolder
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.metamodel.SingularAttribute

/**
 *
 * @author Peter Wu
 */
class LastModifiedByAttribute<X, T>(
        singularAttribute: SingularAttribute<X, T>, private val auditorAware: AuditorAware<*>?,
) : SingularAttributeExt<X, T>(singularAttribute, isLastModifiedBy = true) {

    /**
     * 设置LastModifiedBy
     */
    fun setLastModifiedBy(criteriaUpdate: CriteriaUpdate<X>) {
        val auditor = auditorAware?.currentAuditor?.orElse(null)
        if (auditor != null) {
            criteriaUpdate.set(this.singularAttribute, ApplicationContextHolder.conversionService.convert(auditor, this.javaType))
        }
    }


}