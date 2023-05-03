package top.bettercode.summer.data.jpa

import org.springframework.data.repository.NoRepositoryBean

/**
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
</ID></T> */
@NoRepositoryBean
interface BaseRepository<T, ID> : JpaExtRepository<T, ID> 