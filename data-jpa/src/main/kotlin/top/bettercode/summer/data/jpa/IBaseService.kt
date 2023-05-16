package top.bettercode.summer.data.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.lang.Nullable
import java.util.*

/**
 * @author Peter Wu
 */
interface IBaseService<T, ID, M : BaseRepository<T, ID>> {

    fun getRepository(): M

    fun <S : T> save(s: S): S
    fun <S : T> save(s: S, spec: Specification<T>): Int

    /**
     * 动态更新，只更新非Null字段
     *
     * @param s   对象
     * @param <S> 类型
     * @return 结果
    </S> */
    @Deprecated("""不建议再使用, 请使用以下方式替代
    <p>
    entity.nullFrom(exist);
    <p>
    save(entity);
    <p>""")
    fun <S : T> dynamicSave(s: S): S?
    fun <S : T> saveAll(entities: Iterable<S>): List<S>
    fun delete(t: T)
    fun deleteById(id: ID)
    fun delete(spec: Specification<T>): Int
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
    fun findAll(size: Int, sort: Sort): List<T>
    fun findAll(pageable: Pageable): Page<T>
    fun findAll(sort: Sort): List<T>
    fun count(@Nullable spec: Specification<T>): Long
    fun exists(spec: Specification<T>): Boolean
    fun findOne(@Nullable spec: Specification<T>): Optional<T>
    fun findFirst(spec: Specification<T>): Optional<T>
    fun findAll(@Nullable spec: Specification<T>): List<T>
    fun findAll(spec: Specification<T>, size: Int): List<T>
    fun findAll(spec: Specification<T>, size: Int, sort: Sort): List<T>
    fun findAll(@Nullable spec: Specification<T>, pageable: Pageable): Page<T>
    fun findAll(@Nullable spec: Specification<T>, sort: Sort): List<T>
}
