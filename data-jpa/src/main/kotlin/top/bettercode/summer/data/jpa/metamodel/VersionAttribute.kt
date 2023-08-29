package top.bettercode.summer.data.jpa.metamodel

import top.bettercode.summer.data.jpa.support.JpaUtil
import java.time.LocalDateTime
import java.time.temporal.TemporalAccessor
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaUpdate
import javax.persistence.criteria.Root
import javax.persistence.metamodel.SingularAttribute

/**
 *
 * @author Peter Wu
 */
class VersionAttribute<X, T>(singularAttribute: SingularAttribute<X, T>) : SingularAttributeExt<X, T>(singularAttribute) {

    val isDateType: Boolean = Date::class.java.isAssignableFrom(javaType)
            || TemporalAccessor::class.java.isAssignableFrom(javaType)

    val isNumberType: Boolean = Number::class.java.isAssignableFrom(javaType)

    /**
     * 设置Version
     */
    fun setVersion(criteriaUpdate: CriteriaUpdate<X>, root: Root<X>, builder: CriteriaBuilder) {
        if (isNumberType) {
            val path = root.get<Number>(this.name)
            criteriaUpdate.set(path, builder.sum(path, 1))
        } else if (isDateType) {
            criteriaUpdate.set(this.singularAttribute, JpaUtil.convert(LocalDateTime.now(), javaType)
            )
        } else {
            throw UnsupportedOperationException("Unsupported version type: $javaType")
        }

    }

}