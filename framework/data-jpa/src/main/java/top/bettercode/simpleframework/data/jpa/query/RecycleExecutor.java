package top.bettercode.simpleframework.data.jpa.query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * 回收
 *
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
 */
public interface RecycleExecutor<T, ID> {

  int cleanRecycleBin();

  void deleteFromRecycleBin(ID id);

  void deleteAllByIdFromRecycleBin(Iterable<ID> ids);

  void deleteFromRecycleBin(Specification<T> spec);

  long countRecycleBin();

  long countRecycleBin(@Nullable Specification<T> spec);

  boolean existsInRecycleBin(Specification<T> spec);

  Optional<T> findByIdFromRecycleBin(ID id);

  List<T> findAllByIdFromRecycleBin(Iterable<ID> ids);

  Optional<T> findOneFromRecycleBin(@Nullable Specification<T> spec);

  Optional<T> findFirstFromRecycleBin(Specification<T> spec);

  List<T> findAllFromRecycleBin();

  List<T> findAllFromRecycleBin(int size);

  List<T> findAllFromRecycleBin(int size, Sort sort);

  Page<T> findAllFromRecycleBin(Pageable pageable);

  List<T> findAllFromRecycleBin(Sort sort);

  List<T> findAllFromRecycleBin(@Nullable Specification<T> spec);

  List<T> findAllFromRecycleBin(Specification<T> spec, int size);

  List<T> findAllFromRecycleBin(Specification<T> spec, int size, Sort sort);

  Page<T> findAllFromRecycleBin(@Nullable Specification<T> spec, Pageable pageable);

  List<T> findAllFromRecycleBin(@Nullable Specification<T> spec, Sort sort);
}
