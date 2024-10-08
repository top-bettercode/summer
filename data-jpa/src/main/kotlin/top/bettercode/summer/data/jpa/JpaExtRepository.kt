package top.bettercode.summer.data.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
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
interface JpaExtRepository<T, ID> : JpaRepository<T, ID>, JpaSpecificationExecutor<T>,
    RecycleExecutor<T, ID> {

    val entityManager: EntityManager

    fun detach(entity: Any) {
        entityManager.detach(entity)
    }

    fun clear() {
        entityManager.clear()
    }

    fun <S : T> persist(entity: S): S

    fun updateLowLevel(spec: UpdateSpecification<T>): Long

    fun updatePhysical(spec: UpdateSpecification<T>): Long

    fun update(spec: UpdateSpecification<T>): Long

    fun <S : T> updateLowLevel(s: S?, spec: UpdateSpecification<T>): Long

    fun <S : T> updatePhysical(s: S?, spec: UpdateSpecification<T>): Long

    fun <S : T> update(s: S?, spec: UpdateSpecification<T>): Long

    /**
     * 动态更新，只更新非Null字段
     *
     * @param s   对象
     * @param <S> 类型
     * @return 结果
    </S> */
    fun <S : T> saveDynamic(s: S): S
    fun delete(spec: Specification<T>): Long
    fun deletePhysical(spec: Specification<T>): Long

    fun exists(spec: Specification<T>): Boolean
    fun existsPhysical(spec: Specification<T>?): Boolean
    fun countPhysical(spec: Specification<T>?): Long
    fun findFirst(sort: Sort): Optional<T>
    fun findFirst(spec: Specification<T>?): Optional<T>

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

}
