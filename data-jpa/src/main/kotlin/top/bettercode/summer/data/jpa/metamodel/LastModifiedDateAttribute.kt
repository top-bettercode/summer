package top.bettercode.summer.data.jpa.metamodel

import top.bettercode.summer.data.jpa.support.JpaUtil
import java.time.LocalDateTime
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.metamodel.SingularAttribute

/**
 *
 * @author Peter Wu
 */
class LastModifiedDateAttribute<X, T>(singularAttribute: SingularAttribute<X, T>) : SingularAttributeExt<X, T>(singularAttribute, isLastModifiedDate = true) {

    /**
     * 设置LastModifiedDate
     */
    fun setLastModifiedDate(criteriaUpdate: CriteriaUpdate<X>) {
        criteriaUpdate.set(this.singularAttribute, JpaUtil.convert(LocalDateTime.now(), javaType))
    }
}