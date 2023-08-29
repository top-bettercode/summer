package top.bettercode.summer.data.jpa.querydsl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import org.springframework.data.domain.*
import java.util.*

/**
 * Interface to allow execution of QueryDsl [Predicate] instances.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
interface RecycleQuerydslPredicateExecutor<T> {
    /**
     * Returns a single entity matching the given [Predicate] or [Optional.empty] if
     * none was found.
     *
     * @param predicate must not be null.
     * @return a single entity matching the given [Predicate] or [Optional.empty] if
     * none was found.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the predicate yields
     * more than one result.
     */
    fun findOneFromRecycleBin(predicate: Predicate): Optional<T>

    /**
     * Returns all entities matching the given [Predicate]. In case no match could be found an
     * empty [Iterable] is returned.
     *
     * @param predicate must not be null.
     * @return all entities matching the given [Predicate].
     */
    fun findAllFromRecycleBin(predicate: Predicate): Iterable<T>

    /**
     * Returns all entities matching the given [Predicate] applying the given [Sort]. In
     * case no match could be found an empty [Iterable] is returned.
     *
     * @param predicate must not be null.
     * @param sort      the [Sort] specification to sort the results by, may be
     * [Sort.empty], must not be null.
     * @return all entities matching the given [Predicate].
     * @since 1.10
     */
    fun findAllFromRecycleBin(predicate: Predicate, sort: Sort): Iterable<T>

    /**
     * Returns all entities matching the given [Predicate] applying the given
     * [OrderSpecifier]s. In case no match could be found an empty [Iterable] is
     * returned.
     *
     * @param predicate must not be null.
     * @param orders    the [OrderSpecifier]s to sort the results by.
     * @return all entities matching the given [Predicate] applying the given
     * [OrderSpecifier]s.
     */
    fun findAllFromRecycleBin(predicate: Predicate, vararg orders: OrderSpecifier<*>?): Iterable<T>

    /**
     * Returns all entities ordered by the given [OrderSpecifier]s.
     *
     * @param orders the [OrderSpecifier]s to sort the results by.
     * @return all entities ordered by the given [OrderSpecifier]s.
     */
    fun findAllFromRecycleBin(vararg orders: OrderSpecifier<*>?): Iterable<T>

    /**
     * Returns a [Page] of entities matching the given [Predicate]. In case no match could
     * be found, an empty [Page] is returned.
     *
     * @param predicate must not be null.
     * @param pageable  may be [Pageable.unpaged], must not be null.
     * @return a [Page] of entities matching the given [Predicate].
     */
    fun findAllFromRecycleBin(predicate: Predicate, pageable: Pageable): Page<T>

    /**
     * Returns the number of instances matching the given [Predicate].
     *
     * @param predicate the [Predicate] to count instances for, must not be null.
     * @return the number of instances matching the [Predicate].
     */
    fun countRecycleBin(predicate: Predicate): Long

    /**
     * Checks whether the data store contains elements that match the given [Predicate].
     *
     * @param predicate the [Predicate] to use for the existence check, must not be
     * null.
     * @return true if the data store contains elements that match the given
     * [Predicate].
     */
    fun existsInRecycleBin(predicate: Predicate): Boolean
}