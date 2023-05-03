package top.bettercode.summer.data.jpa.querydsl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import top.bettercode.summer.data.jpa.IBaseService
import java.util.*

/**
 * @author Peter Wu
 */
interface IQuerydslService<T, ID, M : QuerydslRepository<T, ID>> : IBaseService<T, ID, M> {
    fun findOne(predicate: Predicate): Optional<T>
    fun findAll(predicate: Predicate): Iterable<T>
    fun findAll(predicate: Predicate, sort: Sort): Iterable<T>
    fun findAll(predicate: Predicate, vararg orderSpecifiers: OrderSpecifier<*>?): Iterable<T>
    fun findAll(vararg orderSpecifiers: OrderSpecifier<*>?): Iterable<T>
    fun findAll(predicate: Predicate, pageable: Pageable): Page<T>
    fun findAll(
            predicate: Predicate, pageable: Pageable, vararg defaultOrderSpecifiers: OrderSpecifier<*>?,
    ): Page<T>

    fun findAll(pageable: Pageable, vararg defaultOrderSpecifiers: OrderSpecifier<*>?): Page<T>
    fun count(predicate: Predicate): Long
    fun exists(predicate: Predicate): Boolean
}
