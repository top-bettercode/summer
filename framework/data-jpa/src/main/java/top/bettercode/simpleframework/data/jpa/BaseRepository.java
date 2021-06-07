package top.bettercode.simpleframework.data.jpa;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaExtRepository<T, ID> {

}