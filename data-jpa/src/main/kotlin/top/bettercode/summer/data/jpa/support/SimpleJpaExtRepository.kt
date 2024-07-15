package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
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
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.total
import top.bettercode.summer.tools.lang.util.BeanUtil.nullFrom
import java.util.*
import java.util.function.Function
import javax.persistence.EntityManager
import javax.persistence.EntityNotFoundException
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import kotlin.math.min

/**
 * @author Peter Wu
 */
class SimpleJpaExtRepository<T : Any, ID>(
    jpaExtProperties: JpaExtProperties,
    auditorAware: AuditorAware<*>,
    private val entityInformation: JpaEntityInformation<T, ID>,
    @Suppress("RedundantModalityModifier") final override val entityManager: EntityManager
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager), JpaExtRepository<T, ID> {
    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")
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

    private fun <M> mdcId(id: String, pageable: Pageable? = null, run: () -> M): M {
        return JpaUtil.mdcId(entityInformation.entityName + id, pageable, run)
    }

    private fun logflush() {
        if (sqlLog.isDebugEnabled) {
            super.flush()
        }
    }
    //--------------------------------------------

    override fun detach(entity: Any) {
        entityManager.detach(entity)
    }

    override fun clear() {
        entityManager.clear()
    }

    @Transactional
    override fun deleteById(id: ID) {
        mdcId(".deleteById") {
            Assert.notNull(id, "The given id must not be null!")
            findById(id).ifPresent { entity: T -> this.delete(entity) }
            logflush()
        }
    }

    override fun deleteAll(entities: Iterable<T>) {
        mdcId(".deleteAll") {
            super.deleteAll(entities)
        }
    }

    override fun deleteAll() {
        mdcId(".deleteAll") {
            super.deleteAll()
        }
    }

    override fun <S : T> saveAndFlush(entity: S): S {
        return mdcId(".saveAndFlush") {
            super.saveAndFlush(entity)
        }
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> {
        return mdcId(".saveAll") {
            super.saveAll(entities)
        }
    }

    override fun <S : T> saveAllAndFlush(entities: Iterable<S>): List<S> {
        return mdcId(".saveAllAndFlush") {
            super.saveAllAndFlush(entities)
        }
    }

    @Deprecated("")
    override fun deleteInBatch(entities: Iterable<T>) {
        mdcId(".deleteInBatch") {
            @Suppress("DEPRECATION")
            super<SimpleJpaRepository>.deleteInBatch(entities)
        }
    }

    @Transactional
    override fun <S : T> save(entity: S): S {
        return mdcId(".save") {
            extJpaSupport.logicalDeletedAttribute?.setFalseIf(entity)
            val result = if (isNew(entity, false)) {
                entityManager.persist(entity)
                entity
            } else {
                entityManager.merge(entity)
            }
            logflush()
            result
        }
    }

    @Transactional
    override fun <S : T> dynamicSave(s: S): S {
        return mdcId(".dynamicSave") {
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
            logflush()
            s
        }
    }

    @Transactional
    override fun lowLevelUpdate(spec: UpdateSpecification<T>): Long {
        return update(
            s = null,
            spec = spec,
            lowLevel = true,
            physical = true,
            mdcId = ".lowLevelUpdate"
        )
    }

    @Transactional
    override fun physicalUpdate(spec: UpdateSpecification<T>): Long {
        return update(
            s = null,
            spec = spec,
            lowLevel = false,
            physical = true,
            mdcId = ".physicalUpdate"
        )
    }

    @Transactional
    override fun update(spec: UpdateSpecification<T>): Long {
        return update(s = null, spec = spec, lowLevel = false, physical = false, mdcId = ".update")
    }

    @Transactional
    override fun <S : T> lowLevelUpdate(s: S?, spec: UpdateSpecification<T>): Long {
        return update(
            s = s,
            spec = spec,
            lowLevel = true,
            physical = true,
            mdcId = ".lowLevelUpdate"
        )
    }

    @Transactional
    override fun <S : T> physicalUpdate(s: S?, spec: UpdateSpecification<T>): Long {
        return update(
            s = s,
            spec = spec,
            lowLevel = false,
            physical = true,
            mdcId = ".physicalUpdate"
        )
    }

    @Transactional
    override fun <S : T> update(s: S?, spec: UpdateSpecification<T>): Long {
        return update(s = s, spec = spec, lowLevel = false, physical = false, mdcId = ".update")
    }

    private fun <S : T> update(
        s: S?,
        spec: UpdateSpecification<T>,
        lowLevel: Boolean,
        physical: Boolean,
        mdcId: String
    ): Long {
        return mdcId(mdcId) {
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
            if (sqlLog.isInfoEnabled) {
                sqlLog.affected(affected)
            }
            val idAttribute = spec.idAttribute
            val versionAttribute = spec.versionAttribute
            if (idAttribute != null && versionAttribute != null && affected == 0) {
                throw ObjectOptimisticLockingFailureException(domainClass, idAttribute.value)
            }
            logflush()
            affected.toLong()
        }
    }

    @Transactional
    override fun delete(entity: T) {
        mdcId(".delete") {
            extJpaSupport.logicalDeletedAttribute?.apply {
                delete(entity)
                entityManager.merge(entity)
            } ?: super.delete(entity)
            logflush()
        }
    }

    @Transactional
    override fun delete(spec: Specification<T>): Long {
        return mdcId(".delete") {
            val result = if (extJpaSupport.logicalDeletedSupported) {
                doLogicalDelete(spec)
            } else {
                doPhysicalDelete(spec)
            }
            logflush()
            result
        }
    }

    @Transactional
    override fun physicalDelete(spec: Specification<T>): Long {
        return mdcId(".physicalDelete") {
            val result = doPhysicalDelete(spec)
            logflush()
            result
        }
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
        if (sqlLog.isInfoEnabled) {
            sqlLog.affected(affected)
        }
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
        if (sqlLog.isInfoEnabled) {
            sqlLog.affected(affected)
        }
        return affected.toLong()
    }

    @Transactional
    override fun deleteAllById(ids: Iterable<ID>) {
        mdcId(".deleteAllById") {
            delete { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                root[entityInformation.idAttribute].`in`(
                    toCollection(ids)
                )
            }
            logflush()
        }
    }

    override fun deleteAllByIdInBatch(ids: Iterable<ID>) {
        mdcId(".deleteAllByIdInBatch") {
            if (extJpaSupport.logicalDeletedSupported) {
                doLogicalDelete { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                    root[entityInformation.idAttribute].`in`(
                        toCollection(ids)
                    )
                }
            } else {
                super.deleteAllByIdInBatch(ids)
            }
            logflush()
        }
    }

    @Transactional
    override fun deleteAllInBatch(entities: Iterable<T>) {
        mdcId(".deleteAllInBatch") {
            if (extJpaSupport.logicalDeletedSupported) {
                Assert.notNull(entities, "The given Iterable of entities not be null!")
                if (entities.iterator().hasNext()) {
                    val ids: List<ID?> = entities.map { entityInformation.getId(it) }
                    val spec =
                        Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                            root[entityInformation.idAttribute].`in`(ids)
                        }

                    val affected = doLogicalDelete(spec)
                    if (sqlLog.isInfoEnabled) {
                        sqlLog.affected(affected)
                    }
                }
            } else {
                super.deleteAllInBatch(entities)
            }
            logflush()
        }
    }

    @Transactional
    override fun deleteAllInBatch() {
        mdcId(".deleteAllInBatch") {
            if (extJpaSupport.logicalDeletedSupported) {
                val affected = doLogicalDelete(null)
                if (sqlLog.isInfoEnabled) {
                    sqlLog.affected(affected)
                }
            } else {
                super.deleteAllInBatch()
            }
            logflush()
        }
    }

    override fun findById(id: ID): Optional<T> {
        return mdcId(".findById") {
            if (extJpaSupport.logicalDeletedSupported) {
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
    }

    override fun findPhysicalById(id: ID): Optional<T> {
        return mdcId(".findPhysicalById") {
            super.findById(id)
        }
    }

    @Deprecated("")
    override fun getOne(id: ID): T {
        return mdcId(".getOne") {
            getById(id)
        }
    }

    override fun getById(id: ID): T {
        return mdcId(".getById") {
            if (extJpaSupport.logicalDeletedSupported) {
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
    }

    override fun existsById(id: ID): Boolean {
        return mdcId(".existsById") {
            if (extJpaSupport.logicalDeletedSupported) {
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
    }

    override fun findAll(): List<T> {
        return mdcId(".findAll") {
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification)
            } else {
                super.findAll()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    override fun findAllById(ids: Iterable<ID>): List<T> {
        return mdcId(".findAllById") {
            val spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                root[entityInformation.idAttribute].`in`(toCollection(ids))
            }
            val all = super.findAll(
                extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec)
                    ?: spec
            )
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(all.size)
            }
            all
        }
    }

    override fun findAll(sort: Sort): List<T> {
        return mdcId(".findAll") {
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification, sort)
            } else {
                super.findAll(sort)
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    override fun findAll(pageable: Pageable): Page<T> {
        return mdcId(".findAll", pageable) {
            val result: Page<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(
                    extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification,
                    pageable
                )
            } else {
                super.findAll(pageable)
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(result.totalElements)
                sqlLog.retrieved(result.content.size)
            }
            result
        }
    }

    override fun findAll(size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        return mdcId(".findAll", pageable) {
            val spec: Specification<T>? =
                extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
            findUnpaged(spec, pageable)
        }
    }

    override fun findAll(size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        return mdcId(".findAll", pageable) {
            val spec: Specification<T>? =
                extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
            findUnpaged(spec, pageable)
        }
    }

    override fun count(spec: Specification<T>?): Long {
        return mdcId(".count") {
            var spec1: Specification<T>? = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            val count = super.count(spec1)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(count)
            }
            count
        }
    }

    override fun countPhysical(spec: Specification<T>?): Long {
        return mdcId(".count") {
            val count = super.count(spec)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(count)
            }
            count
        }
    }

    override fun exists(spec: Specification<T>): Boolean {
        return mdcId(".exists") {
            count(spec) > 0
        }
    }

    override fun existsPhysical(spec: Specification<T>?): Boolean {
        return mdcId(".exists") {
            countPhysical(spec) > 0
        }
    }

    override fun findFirst(sort: Sort): Optional<T> {
        val pageable = PageRequest.of(0, 1, sort)
        return mdcId(".findFirst", pageable) {
            val spec = extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
            findUnpaged(spec, pageable).stream().findFirst()
        }
    }

    override fun findFirst(spec: Specification<T>?): Optional<T> {
        val pageable = PageRequest.of(0, 1)
        return mdcId(".findFirst", pageable) {
            var spec1 = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            findUnpaged(spec1, pageable).stream().findFirst()
        }
    }

    override fun findOne(spec: Specification<T>?): Optional<T> {
        return mdcId(".findOne") {
            var spec1: Specification<T>? = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            super.findOne(spec1)
        }
    }

    override fun findAll(spec: Specification<T>?): List<T> {
        return mdcId(".findAll") {
            var spec1: Specification<T>? = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            val all = super.findAll(spec1)
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(all.size)
            }
            all
        }
    }

    override fun findAll(spec: Specification<T>?, size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        return mdcId(".findAll", pageable) {
            var spec1 = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            findUnpaged(spec1, pageable)
        }
    }

    override fun findAll(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        return mdcId(".findAll", pageable) {
            var spec1 = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            findUnpaged(spec1, pageable)
        }
    }

    private fun findUnpaged(spec: Specification<T>?, pageable: PageRequest): List<T> {
        val query = getQuery(spec, pageable)
        query.setFirstResult(pageable.offset.toInt())
        query.setMaxResults(pageable.pageSize)
        val content = query.resultList
        if (sqlLog.isInfoEnabled) {
            sqlLog.retrieved(content.size)
        }
        return PageableList(
            content, pageable,
            min(pageable.pageSize, content.size).toLong()
        )
    }

    override fun findPhysicalAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        return mdcId(".findPhysicalAll", pageable) {
            val all = super.findAll(spec, pageable)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(all.totalElements)
                sqlLog.retrieved(all.content.size)
            }
            all
        }
    }

    override fun findPhysicalAllById(ids: Iterable<ID>): List<T> {
        return mdcId(".findPhysicalAllById") {
            val all = super.findAllById(ids)
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(all.size)
            }
            all
        }
    }

    override fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        return mdcId(".findAll", pageable) {
            var spec1: Specification<T>? = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            val all = super.findAll(spec1, pageable)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(all.totalElements)
                sqlLog.retrieved(all.content.size)
            }
            all
        }
    }

    override fun findAll(spec: Specification<T>?, sort: Sort): List<T> {
        return mdcId(".findAll") {
            var spec1: Specification<T>? = spec
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1)
                ?: spec1
            val all = super.findAll(spec1, sort)
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(all.size)
            }
            all
        }
    }

    override fun <S : T> findFirst(example: Example<S>): Optional<S> {
        val pageable = PageRequest.of(0, 1)
        return mdcId(".findFirst", pageable) {
            findUnpaged(example, pageable).stream().findFirst()
        }
    }

    override fun <S : T> findOne(example: Example<S>): Optional<S> {
        return mdcId(".findOne") {
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            super.findOne(example)
        }
    }

    override fun <S : T, R> findBy(
        example: Example<S>,
        queryFunction: Function<FetchableFluentQuery<S>, R>
    ): R {
        return mdcId(".findBy") {
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            super.findBy(example, queryFunction)
        }
    }

    override fun <S : T> count(example: Example<S>): Long {
        return mdcId(".count") {
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val count = super.count(example)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(count)
            }
            count
        }
    }

    override fun <S : T> exists(example: Example<S>): Boolean {
        return mdcId(".exists") {
            count(example) > 0
        }
    }

    override fun <S : T> findAll(example: Example<S>): List<S> {
        return mdcId(".findAll") {
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val all = super.findAll(example)
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(all.size)
            }
            all
        }
    }

    override fun <S : T> findAll(example: Example<S>, size: Int): List<S> {
        val pageable = PageRequest.of(0, size)
        return mdcId(".findAll", pageable) {
            findUnpaged(example, pageable)
        }
    }

    override fun <S : T> findAll(example: Example<S>, size: Int, sort: Sort): List<S> {
        val pageable = PageRequest.of(0, size, sort)
        return mdcId(".findAll", pageable) {
            findUnpaged(example, pageable)
        }
    }

    private fun <S : T> findUnpaged(example: Example<S>, pageable: PageRequest): List<S> {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        val probeType = example.probeType
        val query = getQuery(
            ExampleSpecification(example, escapeCharacter), probeType,
            pageable
        )
        if (pageable.isPaged) {
            query.setFirstResult(pageable.offset.toInt())
            query.setMaxResults(pageable.pageSize)
        }
        val content = query.resultList
        if (sqlLog.isInfoEnabled) {
            sqlLog.retrieved(content.size)
        }
        return PageableList(content, pageable, min(pageable.pageSize, content.size).toLong())
    }

    override fun <S : T> findAll(example: Example<S>, sort: Sort): List<S> {
        return mdcId(".findAll") {
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val all = super.findAll(example, sort)
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(all.size)
            }
            all
        }
    }

    override fun <S : T> findAll(example: Example<S>, pageable: Pageable): Page<S> {
        return mdcId(".findAll", pageable) {
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val all = super.findAll(example, pageable)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(all.totalElements)
                sqlLog.retrieved(all.content.size)
            }
            all
        }
    }

    override fun count(): Long {
        return mdcId(".count") {
            val count: Long = if (extJpaSupport.logicalDeletedSupported) {
                count(null)
            } else {
                super.count()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(count)
            }
            count
        }
    }

    @Transactional
    override fun cleanRecycleBin(): Int {
        return mdcId(".cleanRecycleBin") {
            var reslut = 0L
            if (extJpaSupport.logicalDeletedSupported) {
                reslut =
                    doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.affected(reslut)
            }
            logflush()
            reslut.toInt()
        }
    }

    @Transactional
    override fun deleteFromRecycleBin(id: ID) {
        mdcId(".deleteFromRecycleBin") {
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
                logflush()
            }
        }
    }

    override fun deleteAllByIdFromRecycleBin(ids: Iterable<ID>) {
        mdcId(".deleteAllByIdFromRecycleBin") {
            if (extJpaSupport.logicalDeletedSupported) {
                val spec =
                    Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                        root[entityInformation.idAttribute].`in`(toCollection(ids))
                    }
                doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
            } else {
                if (sqlLog.isInfoEnabled) {
                    sqlLog.affected(0)
                } else {
                }
            }
        }
    }

    @Transactional
    override fun deleteFromRecycleBin(spec: Specification<T>?) {
        mdcId(".deleteFromRecycleBin") {
            val result = if (extJpaSupport.logicalDeletedSupported) {
                doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
            } else {
                0
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.affected(result)
            }
            logflush()
        }
    }

    override fun countRecycleBin(): Long {
        return mdcId(".countRecycleBin") {
            val count: Long = if (extJpaSupport.logicalDeletedSupported) {
                super.count(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
            } else {
                0
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(count)
            }
            count
        }
    }

    override fun countRecycleBin(spec: Specification<T>?): Long {
        return mdcId(".countRecycleBin") {
            var spec1 = spec
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
            }
            val count = super.count(spec1)
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(count)
            }
            count
        }
    }

    override fun existsInRecycleBin(spec: Specification<T>?): Boolean {
        return mdcId(".existsInRecycleBin") {
            countRecycleBin(spec) > 0
        }
    }

    override fun findByIdFromRecycleBin(id: ID): Optional<T> {
        return mdcId(".findByIdFromRecycleBin") {
            if (extJpaSupport.logicalDeletedSupported) {
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
    }

    override fun findAllByIdFromRecycleBin(ids: Iterable<ID>): List<T> {
        return mdcId(".findAllByIdFromRecycleBin") {
            val result: List<T>
            if (extJpaSupport.logicalDeletedSupported) {
                var spec =
                    Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? ->
                        root[entityInformation.idAttribute].`in`(toCollection(ids))
                    }
                spec = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
                result = super.findAll(spec)
            } else {
                result = emptyList()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    override fun findOneFromRecycleBin(spec: Specification<T>?): Optional<T> {
        return mdcId(".findOneFromRecycleBin") {
            var spec1 = spec
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                super.findOne(spec1)
            } else {
                Optional.empty()
            }
        }
    }

    override fun findFirstFromRecycleBin(spec: Specification<T>?): Optional<T> {
        val pageable = PageRequest.of(0, 1)
        return mdcId(".findFirstFromRecycleBin", pageable) {
            findUnpagedFromRecycleBin(spec, pageable).stream().findFirst()
        }
    }

    override fun findAllFromRecycleBin(): List<T> {
        return mdcId(".findAllFromRecycleBin") {
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
            } else {
                emptyList()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    override fun findAllFromRecycleBin(size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        return mdcId(".findAllFromRecycleBin", pageable) {
            findUnpagedFromRecycleBin(null, pageable)
        }
    }

    override fun findAllFromRecycleBin(size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        return mdcId(".findAllFromRecycleBin", pageable) {
            findUnpagedFromRecycleBin(null, pageable)
        }
    }

    override fun findAllFromRecycleBin(pageable: Pageable): Page<T> {
        return mdcId(".findAllFromRecycleBin", pageable) {
            val result: Page<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(
                    extJpaSupport.logicalDeletedAttribute!!.deletedSpecification,
                    pageable
                )
            } else {
                Page.empty(pageable)
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(result.totalElements)
                sqlLog.retrieved(result.content.size)
            }
            result
        }
    }

    override fun findAllFromRecycleBin(sort: Sort): List<T> {
        return mdcId(".findAllFromRecycleBin") {
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification, sort)
            } else {
                emptyList()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?): List<T> {
        return mdcId(".findAllFromRecycleBin") {
            var spec1 = spec
            val result: List<T>
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                result = super.findAll(spec1)
            } else {
                result = emptyList()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, size: Int): List<T> {
        val pageable = PageRequest.of(0, size)
        return mdcId(".findAllFromRecycleBin", pageable) {
            findUnpagedFromRecycleBin(spec, pageable)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        val pageable = PageRequest.of(0, size, sort)
        return mdcId(".findAllFromRecycleBin", pageable) {
            findUnpagedFromRecycleBin(spec, pageable)
        }
    }

    private fun findUnpagedFromRecycleBin(spec: Specification<T>?, pageable: PageRequest): List<T> {
        return if (extJpaSupport.logicalDeletedSupported) {
            var spec1 = spec
            spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
            findUnpaged(spec1, pageable)
        } else {
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(0)
            }
            PageableList(pageable)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, pageable: Pageable): Page<T> {
        return mdcId(".findAllFromRecycleBin", pageable) {
            var spec1 = spec
            val result: Page<T>
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                result = super.findAll(spec1, pageable)
            } else {
                result = Page.empty(pageable)
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.total(result.totalElements)
                sqlLog.retrieved(result.content.size)
            }
            result
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, sort: Sort): List<T> {
        return mdcId(".findAllFromRecycleBin") {
            var spec1 = spec
            val result: List<T>
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                result = super.findAll(spec1, sort)
            } else {
                result = emptyList()
            }
            if (sqlLog.isInfoEnabled) {
                sqlLog.retrieved(result.size)
            }
            result
        }
    }

    companion object {
        private fun <T> toCollection(ts: Iterable<T>): Collection<T> {
            if (ts is Collection<*>) {
                return ts as Collection<T>
            }
            val tCollection: MutableList<T> = ArrayList()
            for (t in ts) {
                tCollection.add(t)
            }
            return tCollection
        }
    }
}
