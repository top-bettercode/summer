package top.bettercode.summer.data.jpa.query

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.lang.Nullable
import java.util.*

/**
 * 回收
 *
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
</ID></T> */
interface RecycleExecutor<T, ID> {
    fun cleanRecycleBin(): Int
    fun deleteFromRecycleBin(id: ID)
    fun deleteAllByIdFromRecycleBin(ids: Iterable<ID>)
    fun deleteFromRecycleBin(spec: Specification<T>?)
    fun countRecycleBin(): Long
    fun countRecycleBin(@Nullable spec: Specification<T>): Long
    fun existsInRecycleBin(spec: Specification<T>): Boolean
    fun findByIdFromRecycleBin(id: ID): Optional<T>
    fun findAllByIdFromRecycleBin(ids: Iterable<ID>): List<T>
    fun findOneFromRecycleBin(@Nullable spec: Specification<T>?): Optional<T>
    fun findFirstFromRecycleBin(spec: Specification<T>?): Optional<T>
    fun findAllFromRecycleBin(): List<T>
    fun findAllFromRecycleBin(size: Int): List<T>
    fun findAllFromRecycleBin(size: Int, sort: Sort): List<T>
    fun findAllFromRecycleBin(pageable: Pageable): Page<T>
    fun findAllFromRecycleBin(sort: Sort): List<T>
    fun findAllFromRecycleBin(@Nullable spec: Specification<T>?): List<T>
    fun findAllFromRecycleBin(spec: Specification<T>?, size: Int): List<T>
    fun findAllFromRecycleBin(spec: Specification<T>?, size: Int, sort: Sort): List<T>
    fun findAllFromRecycleBin(@Nullable spec: Specification<T>?, pageable: Pageable): Page<T>
    fun findAllFromRecycleBin(@Nullable spec: Specification<T>?, sort: Sort): List<T>
}
