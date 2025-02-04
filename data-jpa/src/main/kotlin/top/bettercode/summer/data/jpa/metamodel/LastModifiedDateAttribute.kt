package top.bettercode.summer.data.jpa.metamodel

import top.bettercode.summer.web.support.ApplicationContextHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
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
        @Suppress("UNCHECKED_CAST")
        criteriaUpdate.set(this.singularAttribute, now(javaType) as T)
    }

    companion object {
        fun now(javaType: Class<*>): Any {
            val now: Any = when (javaType) {
                LocalDateTime::class.java -> LocalDateTime.now()
                java.sql.Date::class.java -> java.sql.Date(System.currentTimeMillis())
                Date::class.java -> Date()
                Long::class.javaObjectType, Long::class.java -> System.currentTimeMillis()
                LocalDate::class.java -> LocalDate.now()
                else -> ApplicationContextHolder.conversionService.convert(LocalDateTime.now(), javaType)
            }
            return now
        }
    }


}