package top.bettercode.summer.data.jpa

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.QueryByExampleExecutor
import org.springframework.transaction.annotation.Transactional
import top.bettercode.summer.data.jpa.query.RecycleExecutor
import top.bettercode.summer.data.jpa.support.UpdateSpecification
import java.util.*
import javax.persistence.EntityManager

/**
 * @param <T>  T
 * @param <ID> ID
 * @author Peter Wu
</ID></T> */
@NoRepositoryBean
interface JpaExtRepository<T, ID> : JpaRepository<T, ID>, QueryByExampleExecutor<T>, JpaSpecificationExecutor<T>, RecycleExecutor<T, ID> {
    val entityManager: EntityManager

    @Transactional
    fun <S : T> lowLevelUpdate(s: S, spec: UpdateSpecification<T>): Long

    @Transactional
    fun <S : T> physicalUpdate(s: S, spec: UpdateSpecification<T>): Long

    @Transactional
    fun <S : T> update(s: S, spec: UpdateSpecification<T>): Long

    @Transactional
    fun lowLevelUpdate(spec: UpdateSpecification<T>): Long

    @Transactional
    fun physicalUpdate(spec: UpdateSpecification<T>): Long

    @Transactional
    fun update(spec: UpdateSpecification<T>): Long

    /**
     * 动态更新，只更新非Null字段
     *
     * @param s   对象
     * @param <S> 类型
     * @return 结果
    </S> */
    fun <S : T> dynamicSave(s: S): S
    fun delete(spec: Specification<T>): Long
    fun exists(spec: Specification<T>): Boolean
    fun existsPhysical(spec: Specification<T>?): Boolean
    fun countPhysical(spec: Specification<T>?): Long
    fun findFirst(sort: Sort): Optional<T>
    fun findFirst(spec: Specification<T>?): Optional<T>
    fun <S : T> findFirst(example: Example<S>): Optional<S>

    /**
     * 根据ID查询数据，包括已逻辑删除的数据
     *
     * @param id ID
     * @return 结果
     */
    fun findPhysicalById(id: ID): Optional<T>

    /**
     * 包括已逻辑删除的数据
     *
     * @param spec     条件
     * @param pageable 分页信息
     * @return 分页数据
     */
    fun findPhysicalAll(spec: Specification<T>?, pageable: Pageable): Page<T>

    /**
     * 包括已逻辑删除的数据
     *
     * @param ids ID
     * @return 数据
     */
    fun findPhysicalAllById(ids: Iterable<ID>): List<T>
    fun findAll(size: Int): List<T>
    fun findAll(size: Int, sort: Sort): List<T>
    fun findAll(spec: Specification<T>?, size: Int): List<T>
    fun findAll(spec: Specification<T>?, size: Int, sort: Sort): List<T>
    fun <S : T> findAll(example: Example<S>, size: Int): List<S>
    fun <S : T> findAll(example: Example<S>, size: Int, sort: Sort): List<S>

}
