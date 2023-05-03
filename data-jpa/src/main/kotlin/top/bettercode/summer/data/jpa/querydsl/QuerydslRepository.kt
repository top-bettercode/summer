package top.bettercode.summer.data.jpa.querydsl

import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.NoRepositoryBean
import top.bettercode.summer.data.jpa.BaseRepository

/**
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
</ID></T> */
@NoRepositoryBean
interface QuerydslRepository<T, ID> : BaseRepository<T, ID>, QuerydslPredicateExecutor<T>, RecycleQuerydslPredicateExecutor<T> 