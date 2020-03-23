package cn.bestwu.simpleframework.data.jpa.query;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Interface to allow execution of QueryDsl {@link Predicate} instances.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public interface RecycleQuerydslPredicateExecutor<T> {

  /**
   * Returns a single entity matching the given {@link Predicate} or {@link Optional#empty()} if
   * none was found.
   *
   * @param predicate must not be {@literal null}.
   * @return a single entity matching the given {@link Predicate} or {@link Optional#empty()} if
   * none was found.
   * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the predicate yields
   * more than one result.
   */
  Optional<T> findOneFromRecycleBin(Predicate predicate);

  /**
   * Returns all entities matching the given {@link Predicate}. In case no match could be found an
   * empty {@link Iterable} is returned.
   *
   * @param predicate must not be {@literal null}.
   * @return all entities matching the given {@link Predicate}.
   */
  Iterable<T> findAllFromRecycleBin(Predicate predicate);

  /**
   * Returns all entities matching the given {@link Predicate} applying the given {@link Sort}. In
   * case no match could be found an empty {@link Iterable} is returned.
   *
   * @param predicate must not be {@literal null}.
   * @param sort the {@link Sort} specification to sort the results by, may be {@link Sort#empty()},
   * must not be {@literal null}.
   * @return all entities matching the given {@link Predicate}.
   * @since 1.10
   */
  Iterable<T> findAllFromRecycleBin(Predicate predicate, Sort sort);

  /**
   * Returns all entities matching the given {@link Predicate} applying the given {@link
   * OrderSpecifier}s. In case no match could be found an empty {@link Iterable} is returned.
   *
   * @param predicate must not be {@literal null}.
   * @param orders the {@link OrderSpecifier}s to sort the results by.
   * @return all entities matching the given {@link Predicate} applying the given {@link
   * OrderSpecifier}s.
   */
  Iterable<T> findAllFromRecycleBin(Predicate predicate, OrderSpecifier<?>... orders);

  /**
   * Returns all entities ordered by the given {@link OrderSpecifier}s.
   *
   * @param orders the {@link OrderSpecifier}s to sort the results by.
   * @return all entities ordered by the given {@link OrderSpecifier}s.
   */
  Iterable<T> findAllFromRecycleBin(OrderSpecifier<?>... orders);

  /**
   * Returns a {@link Page} of entities matching the given {@link Predicate}. In case no match could
   * be found, an empty {@link Page} is returned.
   *
   * @param predicate must not be {@literal null}.
   * @param pageable may be {@link Pageable#unpaged()}, must not be {@literal null}.
   * @return a {@link Page} of entities matching the given {@link Predicate}.
   */
  Page<T> findAllFromRecycleBin(Predicate predicate, Pageable pageable);

  /**
   * Returns the number of instances matching the given {@link Predicate}.
   *
   * @param predicate the {@link Predicate} to count instances for, must not be {@literal null}.
   * @return the number of instances matching the {@link Predicate}.
   */
  long countRecycleBin(Predicate predicate);

  /**
   * Checks whether the data store contains elements that match the given {@link Predicate}.
   *
   * @param predicate the {@link Predicate} to use for the existence check, must not be {@literal
   * null}.
   * @return {@literal true} if the data store contains elements that match the given {@link
   * Predicate}.
   */
  boolean existsInRecycleBin(Predicate predicate);

}