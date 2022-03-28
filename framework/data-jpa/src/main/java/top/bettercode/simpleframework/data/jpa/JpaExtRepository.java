package top.bettercode.simpleframework.data.jpa;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
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

  EntityManager getEntityManager();

  <S extends T> int save(S s, Specification<T> spec);

  /**
   * 动态更新，只更新非Null字段
   *
   * @param s   对象
   * @param <S> 类型
   * @return 结果
   */
  <S extends T> S dynamicSave(S s);

  /**
   * 动态更新
   *
   * @param s           对象
   * @param ignoreEmpty 是否不更新空字符字段
   * @param <S>         类型
   * @return 结果
   */
  <S extends T> S dynamicSave(S s, boolean ignoreEmpty);

  int delete(Specification<T> spec);

  boolean exists(Specification<T> spec);

  Optional<T> findFirst(Sort sort);

  Optional<T> findFirst(Specification<T> spec);

  <S extends T> Optional<S> findFirst(Example<S> example);

  /**
   * 根据ID查询数据，包括已逻辑删除的数据
   *
   * @param id ID
   * @return 结果
   */
  Optional<T> findHardById(ID id);

  List<T> findAll(int size);

  List<T> findAll(int size, Sort sort);

  List<T> findAll(Specification<T> spec, int size);

  List<T> findAll(Specification<T> spec, int size, Sort sort);

  <S extends T> List<S> findAll(Example<S> example, int size);

  <S extends T> List<S> findAll(Example<S> example, int size, Sort sort);
}
