package top.bettercode.simpleframework.data.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import top.bettercode.simpleframework.data.jpa.query.RecycleExecutor;

/**
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
 */
@NoRepositoryBean
public interface JpaExtRepository<T, ID> extends JpaRepository<T, ID>, QueryByExampleExecutor<T>,
    JpaSpecificationExecutor<T>,
    RecycleExecutor<T, ID> {

  Optional<T> findFirst(Specification<T> spec);

  List<T> findAll(Specification<T> spec, int size);

  List<T> findAll(Specification<T> spec, int size, Sort sort);

  <S extends T> Optional<S> findFirst(Example<S> example);

  /**
   * 根据ID查询数据，包括已逻辑删除的数据
   *
   * @param id ID
   * @return 结果
   */
  Optional<T> findHardById(ID id);

  /**
   * 动态更新，只更新非Null字段
   *
   * @param s   对象
   * @param <S> 类型
   * @return 结果
   */
  <S extends T> S dynamicSave(S s);

  /**
   * 动态更新，只更新非Null 及 非空（""）字段
   *
   * @param s   对象
   * @param <S> 类型
   * @return 结果
   */
  <S extends T> S dynamicBSave(S s);

  List<T> findAll(int size);

  List<T> findAll(int size, Sort sort);

  boolean exists(Specification<T> spec);

  void delete(Specification<T> spec);

  void deleteAllById(Iterable<ID> ids);

  <S extends T> List<S> findAll(Example<S> example, int size);

  <S extends T> List<S> findAll(Example<S> example, int size, Sort sort);

}
