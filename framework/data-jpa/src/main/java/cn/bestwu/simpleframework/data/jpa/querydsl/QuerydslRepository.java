package cn.bestwu.simpleframework.data.jpa.querydsl;

import cn.bestwu.simpleframework.data.jpa.BaseRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
 */
@NoRepositoryBean
public interface QuerydslRepository<T, ID> extends BaseRepository<T, ID>,
    QuerydslPredicateExecutor<T>, RecycleQuerydslPredicateExecutor<T> {

}