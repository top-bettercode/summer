package cn.bestwu.simpleframework.data.jpa;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;

/**
 * @author Peter Wu
 */
public class BaseServiceImpl<T, ID, M extends BaseRepository<T, ID>> implements
    IBaseService<T, ID, M> {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final M repository;

  public BaseServiceImpl(M repository) {
    this.repository = repository;
  }

  @Override
  public M getRepository() {
    return repository;
  }

  @Override
  public List<T> findAll() {
    return repository.findAll();
  }

  @Override
  public List<T> findAll(Sort sort) {
    return repository.findAll(sort);
  }

  @Override
  public List<T> findAllById(Iterable<ID> ids) {
    return repository.findAllById(ids);
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    return repository.saveAll(entities);
  }

  @Override
  public void deleteInBatch(Iterable<T> entities) {
    repository.deleteInBatch(entities);
  }

  @Override
  public void deleteAllInBatch() {
    repository.deleteAllInBatch();
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example) {
    return repository.findAll(example);
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
    return repository.findAll(example, sort);
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }

  @Override
  public <S extends T> S save(S s) {
    return repository.save(s);
  }

  @Override
  public <S extends T> S dynamicSave(S s) {
    return repository.dynamicSave(s);
  }

  @Override
  public <S extends T> S dynamicBSave(S s) {
    return repository.dynamicBSave(s);
  }

  @Override
  public Optional<T> findById(ID id) {
    return repository.findById(id);
  }

  @Override
  public boolean existsById(ID id) {
    return repository.existsById(id);
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public void deleteById(ID id) {
    repository.deleteById(id);
  }

  @Override
  public void delete(T t) {
    repository.delete(t);
  }

  @Override
  public void deleteAll(Iterable<? extends T> iterable) {
    repository.deleteAll(iterable);
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    return repository.findOne(example);
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    return repository.findAll(example, pageable);
  }

  @Override
  public <S extends T> long count(Example<S> example) {
    return repository.count(example);
  }

  @Override
  public <S extends T> boolean exists(Example<S> example) {
    return repository.exists(example);
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
