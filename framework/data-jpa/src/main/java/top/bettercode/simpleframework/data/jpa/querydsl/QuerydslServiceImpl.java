package top.bettercode.simpleframework.data.jpa.querydsl;

import top.bettercode.simpleframework.data.jpa.BaseService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;

/**
 * @author Peter Wu
 */
public class QuerydslServiceImpl<T, ID, M extends QuerydslRepository<T, ID>> extends
    BaseService<T, ID, M> implements
    IQuerydslService<T, ID, M> {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  public QuerydslServiceImpl(M repository) {
    super(repository);
  }

  @Override
  public Optional<T> findOne(Predicate predicate) {
    return repository.findOne(predicate);
  }

  @Override
  public Iterable<T> findAll(Predicate predicate) {
    return repository.findAll(predicate);
  }

  @Override
  public Iterable<T> findAll(Predicate predicate, Sort sort) {
    return repository.findAll(predicate, sort);
  }

  @Override
  public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orderSpecifiers) {
    return repository.findAll(predicate, orderSpecifiers);
  }

  @Override
  public Iterable<T> findAll(OrderSpecifier<?>... orderSpecifiers) {
    return repository.findAll(orderSpecifiers);
  }

  @Override
  public Page<T> findAll(Predicate predicate, Pageable pageable) {
    return repository.findAll(predicate, pageable);
  }

  @Override
  public Page<T> findAll(Predicate predicate, Pageable pageable,
      OrderSpecifier<?>... defaultOrderSpecifiers) {
    return repository
        .findAll(predicate, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
            pageable.getSortOr(QSort.by(defaultOrderSpecifiers))));
  }

  @Override
  public Page<T> findAll(Pageable pageable,
      OrderSpecifier<?>... defaultOrderSpecifiers) {
    return repository
        .findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
            pageable.getSortOr(QSort.by(defaultOrderSpecifiers))));
  }

  @Override
  public long count(Predicate predicate) {
    return repository.count(predicate);
  }

  @Override
  public boolean exists(Predicate predicate) {
    return repository.exists(predicate);
  }
}
