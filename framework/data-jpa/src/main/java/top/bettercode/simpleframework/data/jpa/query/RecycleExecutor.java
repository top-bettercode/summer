package top.bettercode.simpleframework.data.jpa.query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回收
 *
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
 */
public interface RecycleExecutor<T, ID> {

  @Transactional
  void cleanRecycleBin();

  @Transactional
  void deleteFromRecycleBin(ID id);

  @Transactional
  void deleteFromRecycleBin(Specification<T> example);

  long countRecycleBin();

  List<T> findAllFromRecycleBin();

  Optional<T> findByIdFromRecycleBin(ID id);

  Optional<T> findOneFromRecycleBin(@Nullable Specification<T> spec);

  List<T> findAllFromRecycleBin(@Nullable Specification<T> spec);

  Page<T> findAllFromRecycleBin(@Nullable Specification<T> spec, Pageable pageable);

  List<T> findAllFromRecycleBin(@Nullable Specification<T> spec, Sort sort);

  long countRecycleBin(@Nullable Specification<T> spec);

  boolean existsInRecycleBin(Specification<T> spec);
}
