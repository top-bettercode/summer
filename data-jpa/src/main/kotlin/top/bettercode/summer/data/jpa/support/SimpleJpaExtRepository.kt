package top.bettercode.summer.data.jpa.support

import org.springframework.beans.BeanUtils
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.JpaExtRepository
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.tools.lang.util.BeanUtil.nullFrom
import java.util.*
import java.util.function.Function
import javax.persistence.EntityManager
import javax.persistence.EntityNotFoundException
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * @author Peter Wu
 */
class SimpleJpaExtRepository<T : Any, ID : Any>(
    jpaExtProperties: JpaExtProperties,
    auditorAware: AuditorAware<*>,
    private val entityInformation: JpaEntityInformation<T, ID>,
    @Suppress("RedundantModalityModifier")
    final override val entityManager: EntityManager,
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager), JpaExtRepository<T, ID> {
    private val extJpaSupport: ExtJpaSupport<T> =
        DefaultExtJpaSupport(jpaExtProperties, entityManager, auditorAware, domainClass)
    private val escapeCharacter = EscapeCharacter.DEFAULT

    private fun <S : T> isNew(entity: S, dynamicSave: Boolean): Boolean {
        val idType = entityInformation.idType
        return if (String::class.java == idType) {
            val id = entityInformation.getId(entity)
            if ("" == id) {
                DirectFieldAccessFallbackBeanWrapper(entity).setPropertyValue(
                    entityInformation.idAttribute!!.name, null
                )
                true
            } else {
                entityIsNew(entity, dynamicSave)
            }
        } else {
            entityIsNew(entity, dynamicSave)
        }
    }

    private fun <S : T> entityIsNew(entity: S, dynamicSave: Boolean): Boolean {
        return if (dynamicSave && entityInformation is JpaMetamodelEntityInformation<*, *>) {
            val id = entityInformation.getId(entity)
            val idType = entityInformation.getIdType()
            if (!idType.isPrimitive) {
                return id == null
            }
            if (id is Number) {
                return (id as Number).toLong() == 0L
            }
            throw IllegalArgumentException(
                String.format(
                    "Unsupported primitive id type %s!",
                    idType
                )
            )
        } else {
            entityInformation.isNew(entity)
        }
    }

    //--------------------------------------------

    @Transactional
    override fun <S : T> save(entity: S): S {
        extJpaSupport.logicalDeletedAttribute?.setFalseIf(entity)
        val result = if (isNew(entity, false)) {
            entityManager.persist(entity)
            entity
        } else {
            entityManager.merge(entity)
        }
        return result
    }

    @Transactional
    override fun <S : T> saveDynamic(s: S): S {
        extJpaSupport.logicalDeletedAttribute?.setFalseIf(s)

        var form = false
        val entity: T = if (s::class.java != domainClass) {
            val newInstance = domainClass.getConstructor().newInstance()
            BeanUtils.copyProperties(s, newInstance)
            form = true
            newInstance
        } else {
            s
        }
        if (isNew(entity, true)) {
            entityManager.persist(entity)
        } else {
            val optional = findById(entityInformation.getId(entity)!!)
            if (optional.isPresent) {
                val exist = optional.get()
                entity.nullFrom(exist)
                entityManager.merge(entity)
            } else {
                entityManager.persist(entity)
            }
        }
        if (form) {
            BeanUtils.copyProperties(entity, s)
        }
        return s
    }

    @Transactional
    override fun updateLowLevel(spec: UpdateSpecification<T>): Long {
        return update(
            s = null,
            spec = spec,
            lowLevel = true,
            physical = true,
        )
    }

    @Transactional
    override fun updatePhysical(spec: UpdateSpecification<T>): Long {
        return update(
            s = null,
            spec = spec,
            lowLevel = false,
            physical = true,
        )
    }

    @Transactional
    override fun update(spec: UpdateSpecification<T>): Long {
        return update(s = null, spec = spec, lowLevel = false, physical = false)
    }

    @Transactional
    override fun <S : T> updateLowLevel(s: S?, spec: UpdateSpecification<T>): Long {
        return update(
            s = s,
            spec = spec,
            lowLevel = true,
            physical = true,
        )
    }

    @Transactional
    override fun <S : T> updatePhysical(s: S?, spec: UpdateSpecification<T>): Long {
        return update(
            s = s,
            spec = spec,
            lowLevel = false,
            physical = true,
        )
    }

    @Transactional
    override fun <S : T> update(s: S?, spec: UpdateSpecification<T>): Long {
        return update(s = s, spec = spec, lowLevel = false, physical = false)
    }

    private fun <S : T> update(
        s: S?,
        spec: UpdateSpecification<T>,
        lowLevel: Boolean,
        physical: Boolean,
    ): Long {
        var spec1: Specification<T> = spec
        if (!physical) {
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
        }
        val builder = entityManager.criteriaBuilder
        val criteriaUpdate = spec.createCriteriaUpdate(domainClass, builder, extJpaSupport)
        val root = criteriaUpdate.root

        if (s != null) {
            val beanWrapper = DirectFieldAccessFallbackBeanWrapper(s)
            for (attribute in root.model.singularAttributes) {
                val attributeName = attribute.name
                val attributeValue = beanWrapper.getPropertyValue(attributeName)
                    ?: continue
                criteriaUpdate[attributeName] = attributeValue
            }
        }
        if (!lowLevel) {
            extJpaSupport.lastModifiedDateAttribute?.setLastModifiedDate(criteriaUpdate)

            extJpaSupport.lastModifiedByAttribute?.setLastModifiedBy(criteriaUpdate)

            extJpaSupport.versionAttribute?.setVersion(criteriaUpdate, root, builder)
        }

        val predicate = spec1.toPredicate(root, builder.createQuery(), builder)
        if (predicate != null) {
            criteriaUpdate.where(predicate)
        }
        val affected = entityManager.createQuery(criteriaUpdate).executeUpdate()
        val idAttribute = spec.idAttribute
        val versionAttribute = spec.versionAttribute
        if (idAttribute != null && versionAttribute != null && affected == 0) {
            throw ObjectOptimisticLockingFailureException(domainClass, idAttribute.value)
        }
        return affected.toLong()
    }

    @Transactional
    override fun delete(entity: T) {
        extJpaSupport.logicalDeletedAttribute?.apply {
            delete(entity)
            entityManager.merge(entity)
        } ?: super.delete(entity)
    }

    @Transactional
    override fun delete(spec: Specification<T>): Long {
        val result = if (extJpaSupport.logicalDeletedSupported) {
            doLogicalDelete(spec)
        } else {
            doPhysicalDelete(spec)
        }
        return result
    }

    @Transactional
    override fun deletePhysical(spec: Specification<T>): Long {
        val result = doPhysicalDelete(spec)
        return result
    }

    private fun doLogicalDelete(spec: Specification<T>?): Long {
        var spec1: Specification<T>? = spec
        val builder = entityManager.criteriaBuilder
        val domainClass = domainClass
        val criteriaUpdate = builder.createCriteriaUpdate(domainClass)
        val root = criteriaUpdate.from(domainClass)
        val logicalDeletedAttribute = extJpaSupport.logicalDeletedAttribute!!
        spec1 = logicalDeletedAttribute.andNotDeleted(spec1)
        val predicate = spec1.toPredicate(root, builder.createQuery(), builder)
        if (predicate != null) {
            criteriaUpdate.where(predicate)
        }
        logicalDeletedAttribute.setLogicalDeleted(criteriaUpdate, true)
        val affected = entityManager.createQuery(criteriaUpdate).executeUpdate()
        return affected.toLong()
    }

    private fun doPhysicalDelete(spec: Specification<T>?): Long {
        val builder = entityManager.criteriaBuilder
        val domainClass = domainClass
        val criteriaDelete = builder.createCriteriaDelete(domainClass)
        val root = criteriaDelete.from(domainClass)
        if (spec != null) {
            val predicate = spec.toPredicate(root, builder.createQuery(), builder)
            if (predicate != null) {
                criteriaDelete.where(predicate)
            }
        }
        val affected = entityManager.createQuery(criteriaDelete).executeUpdate()
        return affected.toLong()
    }

    @Transactional
    override fun deleteAllById(ids: Iterable<ID>) {
        delete { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
            root[entityInformation.idAttribute].`in`(
                toCollection(ids)
            )
        }
    }

    override fun deleteAllByIdInBatch(ids: Iterable<ID>) {
        if (extJpaSupport.logicalDeletedSupported) {
            doLogicalDelete { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                root[entityInformation.idAttribute].`in`(
                    toCollection(ids)
                )
            }
        } else {
            super.deleteAllByIdInBatch(ids)
        }
    }

    @Transactional
    override fun deleteAllInBatch(entities: Iterable<T>) {
        if (extJpaSupport.logicalDeletedSupported) {
            Assert.notNull(entities, "The given Iterable of entities not be null!")
            if (entities.iterator().hasNext()) {
                val ids: List<ID?> = entities.map { entityInformation.getId(it) }
                val spec =
                    Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                        root[entityInformation.idAttribute].`in`(ids)
                    }

                doLogicalDelete(spec)
            }
        } else {
            super.deleteAllInBatch(entities)
        }
    }

    @Transactional
    override fun deleteAllInBatch() {
        if (extJpaSupport.logicalDeletedSupported) {
            doLogicalDelete(null)
        } else {
            super.deleteAllInBatch()
        }
    }

    override fun findById(id: ID): Optional<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                        root[entityInformation.idAttribute], id
                    )
                }
            spec = extJpaSupport.logicalDeletedAttribute!!.andNotDeleted(spec)
            super.findOne(spec)
        } else {
            super.findById(id)
        }
    }

    override fun findPhysicalById(id: ID): Optional<T> {
        return super.findById(id)
    }

    @Deprecated("", ReplaceWith("getById(id)"))
    override fun getOne(id: ID): T {
        return getById(id)
    }

    override fun getById(id: ID): T {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                        root[entityInformation.idAttribute], id
                    )
                }
            spec = extJpaSupport.logicalDeletedAttribute!!.andNotDeleted(spec)
            super.findOne(spec)
                .orElseThrow { EntityNotFoundException("Unable to find $domainClass with id $id") }
        } else {
            super.getById(id)
        }
    }

    override fun existsById(id: ID): Boolean {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                        root[entityInformation.idAttribute], id
                    )
                }
            spec = extJpaSupport.logicalDeletedAttribute!!.andNotDeleted(spec)
            super.count(spec) > 0
        } else {
            super.existsById(id)
        }
    }

    override fun findAll(): List<T> {
        val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
            super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification)
        } else {
            super.findAll()
        }
        return result
    }

    override fun findAllById(ids: Iterable<ID>): List<T> {
        val spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
            root[entityInformation.idAttribute].`in`(toCollection(ids))
        }
        val all = super.findAll(
            extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec)
                ?: spec
        )
        return all
    }

    override fun findAll(sort: Sort): List<T> {
        val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
            super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification, sort)
        } else {
            super.findAll(sort)
        }
        return result
    }

    override fun findAll(pageable: Pageable): Page<T> {
        val result: Page<T> = if (extJpaSupport.logicalDeletedSupported) {
            super.findAll(
                extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification,
                pageable
            )
        } else {
            super.findAll(pageable)
        }
        return result
    }

    override fun findAll(size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        val spec: Specification<T>? =
            extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
        return findUnpaged(spec, pageable)
    }

    override fun findAll(size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        val spec: Specification<T>? =
            extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
        return findUnpaged(spec, pageable)
    }

    override fun count(spec: Specification<T>?): Long {
        var spec1: Specification<T>? = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        val count = super.count(spec1)
        return count
    }

    override fun countPhysical(spec: Specification<T>?): Long {
        return super.count(spec)
    }

    override fun exists(spec: Specification<T>): Boolean {
        return count(spec) > 0
    }

    override fun existsPhysical(spec: Specification<T>?): Boolean {
        return countPhysical(spec) > 0
    }

    override fun findFirst(sort: Sort): Optional<T> {
        val pageable = PageRequest.of(0, 1, sort)
        val spec = extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
        return findUnpaged(spec, pageable).stream().findFirst()
    }

    override fun findFirst(spec: Specification<T>?): Optional<T> {
        val pageable = PageRequest.of(0, 1)
        var spec1 = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return findUnpaged(spec1, pageable).stream().findFirst()
    }

    override fun findOne(spec: Specification<T>?): Optional<T> {
        var spec1: Specification<T>? = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return super.findOne(spec1)
    }

    override fun findAll(spec: Specification<T>?): List<T> {
        var spec1: Specification<T>? = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return super.findAll(spec1)
    }

    override fun findAll(spec: Specification<T>?, size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        var spec1 = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return findUnpaged(spec1, pageable)
    }

    override fun findAll(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        var spec1 = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return findUnpaged(spec1, pageable)
    }

    private fun findUnpaged(spec: Specification<T>?, pageable: Pageable): List<T> {
        val query = getQuery(spec, pageable)
        query.setFirstResult(0)
        query.setMaxResults(pageable.pageSize)
        return query.resultList
    }

    override fun findPhysicalAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        return super.findAll(spec, pageable)
    }

    override fun findPhysicalAllById(ids: Iterable<ID>): List<T> {
        return super.findAllById(ids)
    }

    override fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        var spec1: Specification<T>? = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return super.findAll(spec1, pageable)
    }

    override fun findAll(spec: Specification<T>?, sort: Sort): List<T> {
        var spec1: Specification<T>? = spec
        spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
            ?: spec1
        return super.findAll(spec1, sort)
    }

    override fun <S : T> findOne(example: Example<S>): Optional<S> {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        return super.findOne(example)
    }

    override fun <S : T, R : Any> findBy(
        example: Example<S>,
        queryFunction: Function<FetchableFluentQuery<S>, R>
    ): R {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        return super.findBy(example, queryFunction)
    }

    override fun <S : T> count(example: Example<S>): Long {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        return super.count(example)
    }

    override fun <S : T> exists(example: Example<S>): Boolean {
        return count(example) > 0
    }

    override fun <S : T> findAll(example: Example<S>): List<S> {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        return super.findAll(example)
    }

    override fun <S : T> findAll(example: Example<S>, sort: Sort): List<S> {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        return super.findAll(example, sort)
    }

    override fun <S : T> findAll(example: Example<S>, pageable: Pageable): Page<S> {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        return super.findAll(example, pageable)
    }

    override fun count(): Long {
        return if (extJpaSupport.logicalDeletedSupported) {
            count(null)
        } else {
            super.count()
        }
    }

    @Transactional
    override fun deleteAllRecycleBin(): Long {
        var reslut = 0L
        if (extJpaSupport.logicalDeletedSupported) {
            reslut =
                doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
        }
        return reslut
    }

    @Transactional
    override fun deleteFromRecycleBin(id: ID) {
        if (extJpaSupport.logicalDeletedSupported) {
            val spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                        root[entityInformation.idAttribute], id
                    )
                }
            doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
            val entity = findByIdFromRecycleBin(id)
            entity.ifPresent { t: T -> super.delete(t) }
        }
    }

    override fun deleteAllByIdFromRecycleBin(ids: Iterable<ID>): Long {
        return if (extJpaSupport.logicalDeletedSupported) {
            val spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                    root[entityInformation.idAttribute].`in`(toCollection(ids))
                }
            doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
        } else {
            0
        }
    }

    @Transactional
    override fun deleteFromRecycleBin(spec: Specification<T>?): Long {
        return if (extJpaSupport.logicalDeletedSupported) {
            doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
        } else {
            0
        }
    }

    override fun countRecycleBin(): Long {
        return if (extJpaSupport.logicalDeletedSupported) {
            super.count(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
        } else {
            0
        }
    }

    override fun countRecycleBin(spec: Specification<T>?): Long {
        var spec1 = spec
        if (extJpaSupport.logicalDeletedSupported) {
            spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
        }
        return super.count(spec1)
    }

    override fun existsInRecycleBin(spec: Specification<T>?): Boolean {
        return countRecycleBin(spec) > 0
    }

    override fun findByIdFromRecycleBin(id: ID): Optional<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(root[entityInformation.idAttribute], id)
                }
            spec = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
            super.findOne(spec)
        } else {
            Optional.empty()
        }
    }

    override fun findAllByIdFromRecycleBin(ids: Iterable<ID>): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec =
                Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                    root[entityInformation.idAttribute].`in`(toCollection(ids))
                }
            spec = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
            super.findAll(spec)
        } else {
            emptyList()
        }

    }

    override fun findOneFromRecycleBin(spec: Specification<T>?): Optional<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            val spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
            super.findOne(spec1)
        } else {
            Optional.empty()
        }
    }

    override fun findFirstFromRecycleBin(spec: Specification<T>?): Optional<T> {
        val pageable = PageRequest.of(0, 1)
        return findUnpagedFromRecycleBin(spec, pageable).stream().findFirst()
    }

    override fun findAllFromRecycleBin(): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        return findUnpagedFromRecycleBin(null, pageable)
    }

    override fun findAllFromRecycleBin(size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        return findUnpagedFromRecycleBin(null, pageable)
    }

    override fun findAllFromRecycleBin(pageable: Pageable): Page<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            super.findAll(
                extJpaSupport.logicalDeletedAttribute!!.deletedSpecification,
                pageable
            )
        } else {
            Page.empty(pageable)
        }
    }

    override fun findAllFromRecycleBin(sort: Sort): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification, sort)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            val spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
            super.findAll(spec1)
        } else {
            emptyList()
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        return findUnpagedFromRecycleBin(spec, pageable)
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        return findUnpagedFromRecycleBin(spec, pageable)
    }

    private fun findUnpagedFromRecycleBin(spec: Specification<T>?, pageable: Pageable): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec1 = spec
            spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
            findUnpaged(spec1, pageable)
        } else {
            Collections.emptyList()
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, pageable: Pageable): Page<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            val spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
            super.findAll(spec1, pageable)
        } else {
            Page.empty(pageable)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, sort: Sort): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            val spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
            super.findAll(spec1, sort)
        } else {
            emptyList()
        }
    }

    companion object {
        private fun <T> toCollection(iterable: Iterable<T>): Collection<T> {
            if (iterable is Collection<*>) {
                return iterable as Collection<T>
            }
            val list: MutableList<T> = ArrayList()
            for (t in iterable) {
                list.add(t)
            }
            return list
        }
    }
}
