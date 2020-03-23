package cn.bestwu.simpleframework.data.jpa;

import cn.bestwu.simpleframework.data.jpa.query.RecycleQuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @param <T> T
 * @param <ID> ID
 * @author Peter Wu
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaExtRepository<T, ID>,
    QuerydslPredicateExecutor<T>, RecycleQuerydslPredicateExecutor<T> {

}