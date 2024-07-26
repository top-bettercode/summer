package top.bettercode.summer.data.jpa.querydsl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.querydsl.QSort
import top.bettercode.summer.data.jpa.BaseService
import java.util.*

/**
 * @author Peter Wu
 */
class QuerydslServiceImpl<T : Any, ID : Any, M : QuerydslRepository<T, ID>>(repository: M) :
    BaseService<T, ID, M>(repository), IQuerydslService<T, ID, M> {

    override fun findOne(predicate: Predicate): Optional<T> {
        return repository.findOne(predicate)
    }

    override fun findAll(predicate: Predicate): Iterable<T> {
        return repository.findAll(predicate)
    }

    override fun findAll(predicate: Predicate, sort: Sort): Iterable<T> {
        return repository.findAll(predicate, sort)
    }

    override fun findAll(
        predicate: Predicate,
        vararg orderSpecifiers: OrderSpecifier<*>?
    ): Iterable<T> {
        return repository.findAll(predicate, *orderSpecifiers)
    }

    override fun findAll(vararg orderSpecifiers: OrderSpecifier<*>?): Iterable<T> {
        return repository.findAll(*orderSpecifiers)
    }

    override fun findAll(predicate: Predicate, pageable: Pageable): Page<T> {
        return repository.findAll(predicate, pageable)
    }

    override fun findAll(
        predicate: Predicate, pageable: Pageable,
        vararg defaultOrderSpecifiers: OrderSpecifier<*>?
    ): Page<T> {
        return repository
            .findAll(
                predicate, PageRequest.of(
                    pageable.pageNumber, pageable.pageSize,
                    pageable.getSortOr(QSort.by(*defaultOrderSpecifiers))
                )
            )
    }

    override fun findAll(
        pageable: Pageable,
        vararg defaultOrderSpecifiers: OrderSpecifier<*>?
    ): Page<T> {
        return repository
            .findAll(
                PageRequest.of(
                    pageable.pageNumber, pageable.pageSize,
                    pageable.getSortOr(QSort.by(*defaultOrderSpecifiers))
                )
            )
    }

    override fun count(predicate: Predicate): Long {
        return repository.count(predicate)
    }

    override fun exists(predicate: Predicate): Boolean {
        return repository.exists(predicate)
    }
}
