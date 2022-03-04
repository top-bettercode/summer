package top.bettercode.simpleframework.data.jpa.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import top.bettercode.simpleframework.data.jpa.IBaseService;

/**
 * @author Peter Wu
 */
public interface IQuerydslService<T, ID, M extends QuerydslRepository<T, ID>> extends
    IBaseService<T, ID, M> {

  Optional<T> findOne(Predicate predicate);

  Iterable<T> findAll(Predicate predicate);

  Iterable<T> findAll(Predicate predicate, Sort sort);

  Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orderSpecifiers);

  Iterable<T> findAll(OrderSpecifier<?>... orderSpecifiers);

  Page<T> findAll(Predicate predicate, Pageable pageable);

  Page<T> findAll(Predicate predicate, Pageable pageable,
      OrderSpecifier<?>... defaultOrderSpecifiers);

  Page<T> findAll(Pageable pageable, OrderSpecifier<?>... defaultOrderSpecifiers);

  long count(Predicate predicate);

  boolean exists(Predicate predicate);
}
