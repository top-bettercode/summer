package top.bettercode.simpleframework.data.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * @author Peter Wu
 */
public interface IBaseService<T, ID, M extends BaseRepository<T, ID>> {

  M getRepository();


  <S extends T> S save(S s);

  /**
   * 动态更新，只更新非Null 及 非空（""）字段
   *
   * @param s   对象
   * @param <S> 类型
   * @return 结果
   */
  <S extends T> S dynamicBSave(S s);

  /**
   * 动态更新，只更新非Null字段
   *
   * @param s   对象
   * @param <S> 类型
   * @return 结果
   */
  <S extends T> S dynamicSave(S s);

  <S extends T> List<S> saveAll(Iterable<S> entities);

  void delete(T t);

  void deleteById(ID id);

  void delete(Specification<T> spec);

  void deleteAllById(Iterable<ID> ids);

  void deleteAll(Iterable<? extends T> iterable);

  void deleteAll();

  void deleteInBatch(Iterable<T> entities);

  void deleteAllInBatch();

  long count();

  Optional<T> findById(ID id);

  Optional<T> findFirst(Sort sort);

  boolean existsById(ID id);

  List<T> findAll();

  List<T> findAllById(Iterable<ID> ids);

  List<T> findAll(int size);

  List<T> findAll(int size, Sort sort);

  Page<T> findAll(Pageable pageable);

  List<T> findAll(Sort sort);

  long count(@Nullable Specification<T> spec);

  boolean exists(Specification<T> spec);

  Optional<T> findOne(@Nullable Specification<T> spec);

  Optional<T> findFirst(Specification<T> spec);

  List<T> findAll(@Nullable Specification<T> spec);

  List<T> findAll(Specification<T> spec, int size);

  List<T> findAll(Specification<T> spec, int size, Sort sort);

  Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);

  List<T> findAll(@Nullable Specification<T> spec, Sort sort);

  <S extends T> long count(Example<S> example);

  <S extends T> boolean exists(Example<S> example);

  <S extends T> Optional<S> findOne(Example<S> example);

  <S extends T> Optional<S> findFirst(Example<S> example);

  <S extends T> List<S> findAll(Example<S> example);

  <S extends T> List<S> findAll(Example<S> example, int size);

  <S extends T> List<S> findAll(Example<S> example, int size, Sort sort);

  <S extends T> Page<S> findAll(Example<S> example, Pageable pageable);

  <S extends T> List<S> findAll(Example<S> example, Sort sort);
}
