package top.bettercode.summer.data.jpa.querydsl

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.SimplePath
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.data.jpa.support.DefaultExtJpaSupport

/**
 * @author Peter Wu
 */
open class QuerydslSoftDeleteSupport<T>(
        jpaExtProperties: JpaExtProperties, domainClass: Class<*>,
        entityPath: EntityPath<T>?
) : DefaultExtJpaSupport(jpaExtProperties, domainClass) {
    private var path: SimplePath<Any>? = null

    init {
        if (supportSoftDeleted()) {
            if (entityPath != null) {
                path = Expressions.path(softDeletedPropertyType, entityPath,
                        softDeletedPropertyName)
            }
        }
    }

    fun andTruePredicate(predicate: Predicate?): Predicate {
        return getPredicate(predicate, softDeletedTrueValue)
    }

    fun andFalsePredicate(predicate: Predicate?): Predicate {
        return getPredicate(predicate, softDeletedFalseValue)
    }

    protected fun getPredicate(predicate: Predicate?, value: Any?): Predicate {
        return if (predicate == null) {
            path!!.eq(value)
        } else {
            path!!.eq(value).and(predicate)
        }
    }
}
