package top.bettercode.simpleframework.data.jpa;

import java.util.List;
import java.util.Optional;
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

  <S extends T> int save(S s, Specification<T> spec);

  /**
   * 动态更新，只更新非Null字段
   *
   * @param s   对象
   * @param <S> 类型
   * @return 结果
   * @deprecated 不建议再使用, 请使用以下方式替代
   * <p>
   * entity.nullPropertySetFrom(exist);
   * <p>
   * save(entity);
   * <p>
   */
  @Deprecated
  <S extends T> S dynamicSave(S s);

  <S extends T> List<S> saveAll(Iterable<S> entities);

  void delete(T t);

  void deleteById(ID id);

  int delete(Specification<T> spec);

  void deleteAllById(Iterable<? extends ID> ids);

  void deleteAll(Iterable<? extends T> entities);

  void deleteAll();

  void deleteAllInBatch(Iterable<T> entities);

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

}
