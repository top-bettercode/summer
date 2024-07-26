package top.bettercode.summer.data.jpa.query

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import java.util.*

/**
 * 回收
 *
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
</ID></T> */
interface RecycleExecutor<T, ID> {

    fun deleteAllRecycleBin(): Long
    fun deleteFromRecycleBin(id: ID)
    fun deleteAllByIdFromRecycleBin(ids: Iterable<ID>): Long
    fun deleteFromRecycleBin(spec: Specification<T>?): Long

    fun countRecycleBin(): Long
    fun countRecycleBin(spec: Specification<T>?): Long
    fun existsInRecycleBin(spec: Specification<T>?): Boolean

    fun findByIdFromRecycleBin(id: ID): Optional<T>
    fun findAllByIdFromRecycleBin(ids: Iterable<ID>): List<T>

    fun findOneFromRecycleBin(spec: Specification<T>?): Optional<T>
    fun findFirstFromRecycleBin(spec: Specification<T>?): Optional<T>

    fun findAllFromRecycleBin(): List<T>
    fun findAllFromRecycleBin(size: Int): List<T>
    fun findAllFromRecycleBin(size: Int, sort: Sort): List<T>
    fun findAllFromRecycleBin(pageable: Pageable): Page<T>
    fun findAllFromRecycleBin(sort: Sort): List<T>
    fun findAllFromRecycleBin(spec: Specification<T>?): List<T>
    fun findAllFromRecycleBin(spec: Specification<T>?, size: Int): List<T>
    fun findAllFromRecycleBin(spec: Specification<T>?, size: Int, sort: Sort): List<T>
    fun findAllFromRecycleBin(spec: Specification<T>?, pageable: Pageable): Page<T>
    fun findAllFromRecycleBin(spec: Specification<T>?, sort: Sort): List<T>
}
