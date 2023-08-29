package top.bettercode.summer.data.jpa.querydsl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.SimplePath
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.support.CrudMethodMetadata
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor
import org.springframework.data.querydsl.EntityPathResolver
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.data.jpa.metamodel.LogicalDeletedAttribute
import top.bettercode.summer.data.jpa.support.DefaultExtJpaSupport
import java.util.*
import javax.persistence.EntityManager

/**
 * @author Peter Wu
 */
class QuerydslJpaExtPredicateExecutor<T : Any>(
        jpaExtProperties: JpaExtProperties,
        entityInformation: JpaEntityInformation<T, Any>,
        entityManager: EntityManager,
        resolver: EntityPathResolver,
        metadata: CrudMethodMetadata
) : QuerydslJpaPredicateExecutor<T>(entityInformation, entityManager, resolver, metadata), QuerydslPredicateExecutor<T>, RecycleQuerydslPredicateExecutor<T> {
    private val logicalDeletedAttribute: LogicalDeletedAttribute<T, *>?
    private var path: SimplePath<Any>? = null

    init {
        val extJpaSupport = DefaultExtJpaSupport<T>(jpaExtProperties, entityManager, null, entityInformation.javaType)
        logicalDeletedAttribute = extJpaSupport.logicalDeletedAttribute
        if (logicalDeletedAttribute != null) {
            val entityPath = resolver.createPath(entityInformation.javaType)
            path = Expressions.path(logicalDeletedAttribute.javaType, entityPath,
                    logicalDeletedAttribute.name)
        }
    }

    private val logicalDeletedSupported: Boolean by lazy {
        path != null
    }

    private fun andNotDeleted(predicate: Predicate): Predicate {
        return path?.eq(logicalDeletedAttribute!!.falseValue)?.and(predicate) ?: predicate
    }

    private fun andDeleted(predicate: Predicate): Predicate {
        return path?.eq(logicalDeletedAttribute!!.trueValue)?.and(predicate) ?: predicate
    }

    private val notDeleted by lazy {
        path!!.eq(logicalDeletedAttribute!!.falseValue)
    }

    private val deleted by lazy {
        path!!.eq(logicalDeletedAttribute!!.trueValue)
    }

    override fun findOne(predicate: Predicate): Optional<T> {
        return super.findOne(andNotDeleted(predicate))
    }

    override fun findAll(predicate: Predicate): List<T> {
        return super.findAll(andNotDeleted(predicate))
    }

    override fun findAll(predicate: Predicate, vararg orders: OrderSpecifier<*>): List<T> {
        return super.findAll(andNotDeleted(predicate), *orders)
    }

    override fun findAll(predicate: Predicate, sort: Sort): List<T> {
        return super.findAll(andNotDeleted(predicate), sort)
    }

    override fun findAll(vararg orders: OrderSpecifier<*>): List<T> {
        return if (logicalDeletedSupported) {
            super.findAll(notDeleted, *orders)
        } else {
            super.findAll(*orders)
        }
    }

    override fun findAll(predicate: Predicate, pageable: Pageable): Page<T> {
        return super.findAll(andNotDeleted(predicate), pageable)
    }

    override fun count(predicate: Predicate): Long {
        return super.count(andNotDeleted(predicate))
    }

    override fun exists(predicate: Predicate): Boolean {
        return super.exists(andNotDeleted(predicate))
    }

    override fun findOneFromRecycleBin(predicate: Predicate): Optional<T> {
        return if (logicalDeletedSupported) {
            super.findOne(andDeleted(predicate))
        } else {
            Optional.empty()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate): Iterable<T> {
        return if (logicalDeletedSupported) {
            super.findAll(andDeleted(predicate))
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate, sort: Sort): Iterable<T> {
        return if (logicalDeletedSupported) {
            super.findAll(andDeleted(predicate), sort)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate, vararg orders: OrderSpecifier<*>?): Iterable<T> {
        return if (logicalDeletedSupported) {
            super.findAll(andDeleted(predicate), *orders)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(vararg orders: OrderSpecifier<*>?): Iterable<T> {
        return if (logicalDeletedSupported) {
            super.findAll(deleted, *orders)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate, pageable: Pageable): Page<T> {
        return if (logicalDeletedSupported) {
            super.findAll(andDeleted(predicate), pageable)
        } else {
            Page.empty()
        }
    }

    override fun countRecycleBin(predicate: Predicate): Long {
        return if (logicalDeletedSupported) {
            super.count(andDeleted(predicate))
        } else {
            0
        }
    }

    override fun existsInRecycleBin(predicate: Predicate): Boolean {
        return if (logicalDeletedSupported) {
            super.exists(andDeleted(predicate))
        } else {
            false
        }
    }
}
