package top.bettercode.summer.data.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional
import top.bettercode.summer.data.jpa.support.UpdateSpecification
import java.util.*

/**
 * @author Peter Wu
 */
interface IBaseService<T:Any, ID:Any, M : BaseRepository<T, ID>> {

    fun getRepository(): M

    fun <E> findAllPageByPage(totalPages: Int, query: (Pageable) -> List<E>): List<E>

    fun <E> findAllPageByPage(pageSize: Int, totalPages: Int, query: (Pageable) -> List<E>): List<E>

    fun <S : T> save(s: S): S

    /**
     * 动态更新，只更新非Null字段
     *
     * @param s   对象
     * @param <S> 类型
     * @return 结果
    </S> */
    fun <S : T> saveDynamic(s: S): S

    fun <S : T> saveAll(entities: Iterable<S>): List<S>

    @Transactional
    fun update(spec: UpdateSpecification<T>): Long

    @Transactional
    fun <S : T> update(s: S?, spec: UpdateSpecification<T>): Long

    fun delete(t: T)
    fun deleteById(id: ID)
    fun delete(spec: Specification<T>): Long
    fun deleteAllById(ids: Iterable<ID>)
    fun deleteAll(entities: Iterable<T>)
    fun deleteAll()
    fun deleteAllInBatch(entities: Iterable<T>)
    fun deleteAllInBatch()
    fun count(): Long
    fun findById(id: ID): Optional<T>
    fun findFirst(sort: Sort): Optional<T>
    fun existsById(id: ID): Boolean
    fun findAll(): List<T>
    fun findAllById(ids: Iterable<ID>): List<T>

    fun findAll(size: Int): List<T>
    fun findAll(offset: Long, size: Int): List<T>
    fun findAll(size: Int, sort: Sort): List<T>
    fun findAll(offset: Long, size: Int, sort: Sort): List<T>

    fun findAll(pageable: Pageable): Page<T>
    fun findAll(sort: Sort): List<T>
    fun count(spec: Specification<T>?): Long
    fun exists(spec: Specification<T>): Boolean
    fun findOne(spec: Specification<T>?): Optional<T>
    fun findFirst(spec: Specification<T>?): Optional<T>

    fun findAll(spec: Specification<T>?): List<T>

    fun findAll(spec: Specification<T>?, size: Int): List<T>
    fun findAll(spec: Specification<T>?, offset: Long, size: Int): List<T>
    fun findAll(spec: Specification<T>?, size: Int, sort: Sort): List<T>
    fun findAll(spec: Specification<T>?, offset: Long, size: Int, sort: Sort): List<T>

    fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T>
    fun findAll(spec: Specification<T>?, sort: Sort): List<T>
}
