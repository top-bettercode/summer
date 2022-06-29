package top.bettercode.simpleframework.data.jpa;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Peter Wu
 */
public class BaseService<T, ID, M extends BaseRepository<T, ID>> implements
    IBaseService<T, ID, M> {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final M repository;

  public BaseService(M repository) {
    this.repository = repository;
  }

  @Override
  public M getRepository() {
    return repository;
  }

  @Override
  public <S extends T> int save(S s, Specification<T> spec) {
    return repository.save(s, spec);
  }

  @Override
  public <S extends T> S dynamicSave(S s) {
    return repository.dynamicSave(s);
  }

  @Override
  public int delete(Specification<T> spec) {
    return repository.delete(spec);
  }

  @Override
  public void deleteAllById(Iterable<? extends ID> ids) {
    repository.deleteAllById(ids);
  }

  @Override
  public boolean exists(Specification<T> spec) {
    return repository.exists(spec);
  }

  @Override
  public Optional<T> findFirst(Specification<T> spec) {
    return repository.findFirst(spec);
  }

  @Override
  public <S extends T> Optional<S> findFirst(Example<S> example) {
    return repository.findFirst(example);
  }

  @Override
  public List<T> findAll(int size) {
    return repository.findAll(size);
  }

  @Override
  public List<T> findAll(int size, Sort sort) {
    return repository.findAll(size, sort);
  }

  @Override
  public List<T> findAll(Specification<T> spec, int size) {
    return repository.findAll(spec, size);
  }

  @Override
  public List<T> findAll(Specification<T> spec, int size, Sort sort) {
    return repository.findAll(spec, size, sort);
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example, int size) {
    return repository.findAll(example, size);
  }

  @Override
  public <S extends T> List<S> findAll(Example<S> example, int size,
      Sort sort) {
    return repository.findAll(example, size, sort);
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
  public void deleteAllInBatch(Iterable<T> entities) {
    repository.deleteAllInBatch(entities);
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
  public <S extends T> S save(S entity) {
    return repository.save(entity);
  }

  @Override
  public Optional<T> findById(ID id) {
    return repository.findById(id);
  }

  @Override
  public Optional<T> findFirst(Sort sort) {
    return repository.findFirst(sort);
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
  public void delete(T entity) {
    repository.delete(entity);
  }

  @Override
  public void deleteAll(Iterable<? extends T> entities) {
    repository.deleteAll(entities);
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
  public <S extends T> Page<S> findAll(Example<S> example,
      Pageable pageable) {
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
  public Optional<T> findOne(Specification<T> spec) {
    return repository.findOne(spec);
  }

  @Override
  public List<T> findAll(Specification<T> spec) {
    return repository.findAll(spec);
  }

  @Override
  public Page<T> findAll(Specification<T> spec, Pageable pageable) {
    return repository.findAll(spec, pageable);
  }

  @Override
  public List<T> findAll(Specification<T> spec, Sort sort) {
    return repository.findAll(spec, sort);
  }

  @Override
  public long count(Specification<T> spec) {
    return repository.count(spec);
  }
}
