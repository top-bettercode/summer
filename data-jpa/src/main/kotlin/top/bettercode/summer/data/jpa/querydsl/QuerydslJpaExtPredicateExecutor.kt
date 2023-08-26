package top.bettercode.summer.data.jpa.querydsl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.support.CrudMethodMetadata
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor
import org.springframework.data.querydsl.EntityPathResolver
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import top.bettercode.summer.data.jpa.config.JpaExtProperties
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
    private val logicalDeleteSupport: QuerydslLogicalDeleteSupport<T>

    init {
        logicalDeleteSupport = QuerydslLogicalDeleteSupport(jpaExtProperties,
                entityInformation.javaType, resolver.createPath(entityInformation.javaType))
    }

    override fun findOne(predicate: Predicate): Optional<T> {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.findOne(predicate1)
    }

    override fun findAll(predicate: Predicate): List<T> {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.findAll(predicate1)
    }

    override fun findAll(predicate: Predicate, vararg orders: OrderSpecifier<*>): List<T> {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.findAll(predicate1, *orders)
    }

    override fun findAll(predicate: Predicate, sort: Sort): List<T> {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.findAll(predicate1, sort)
    }

    override fun findAll(vararg orders: OrderSpecifier<*>): List<T> {
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            super.findAll(logicalDeleteSupport.andFalsePredicate(null), *orders)
        } else {
            super.findAll(*orders)
        }
    }

    override fun findAll(predicate: Predicate, pageable: Pageable): Page<T> {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.findAll(predicate1, pageable)
    }

    override fun count(predicate: Predicate): Long {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.count(predicate1)
    }

    override fun exists(predicate: Predicate): Boolean {
        var predicate1 = predicate
        if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andFalsePredicate(predicate1)
        }
        return super.exists(predicate1)
    }

    override fun findOneFromRecycleBin(predicate: Predicate?): Optional<T> {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.findOne(predicate1)
        } else {
            Optional.empty()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate?): Iterable<T> {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.findAll(predicate1)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate?, sort: Sort): Iterable<T> {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.findAll(predicate1, sort)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate?, vararg orders: OrderSpecifier<*>?): Iterable<T> {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.findAll(predicate1, *orders)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(vararg orders: OrderSpecifier<*>?): Iterable<T> {
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            super.findAll(logicalDeleteSupport.andTruePredicate(null), *orders)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(predicate: Predicate?, pageable: Pageable): Page<T> {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.findAll(predicate1, pageable)
        } else {
            Page.empty()
        }
    }

    override fun countRecycleBin(predicate: Predicate?): Long {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.count(predicate1)
        } else {
            0
        }
    }

    override fun existsInRecycleBin(predicate: Predicate?): Boolean {
        var predicate1 = predicate
        return if (logicalDeleteSupport.supportLogicalDeleted()) {
            predicate1 = logicalDeleteSupport.andTruePredicate(predicate1)
            super.exists(predicate1)
        } else {
            false
        }
    }
}
