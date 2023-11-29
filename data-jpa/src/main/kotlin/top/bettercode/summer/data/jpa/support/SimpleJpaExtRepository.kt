package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
import kotlin.math.min

/**
 * @author Peter Wu
 */
class SimpleJpaExtRepository<T : Any, ID>(
        jpaExtProperties: JpaExtProperties,
        auditorAware: AuditorAware<*>?,
        private val entityInformation: JpaEntityInformation<T, ID>,
        @Suppress("RedundantModalityModifier") final override val entityManager: EntityManager
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager), JpaExtRepository<T, ID> {
    private val sqlLog = LoggerFactory.getLogger("org.hibernate.SQL")
    private val extJpaSupport: ExtJpaSupport<T> = DefaultExtJpaSupport(jpaExtProperties, entityManager, auditorAware, domainClass)
    private val escapeCharacter = EscapeCharacter.DEFAULT

    private fun <S : T> isNew(entity: S, dynamicSave: Boolean): Boolean {
        val idType = entityInformation.idType
        return if (String::class.java == idType) {
            val id = entityInformation.getId(entity)
            if ("" == id) {
                DirectFieldAccessFallbackBeanWrapper(entity).setPropertyValue(
                        entityInformation.idAttribute!!.name, null)
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
            throw IllegalArgumentException(String.format("Unsupported primitive id type %s!", idType))
        } else {
            entityInformation.isNew(entity)
        }
    }

    private fun mdcPutId(id: String): Boolean {
        return if (MDC.get("id") == null) {
            MDC.put("id", entityInformation.entityName + id)
            true
        } else {
            false
        }
    }

    private fun cleanMdc(mdc: Boolean) {
        if (mdc) {
            MDC.remove("id")
        }
    }

    //--------------------------------------------

    override fun detach(entity: T) {
        entityManager.detach(entity)
    }

    override fun clear() {
        entityManager.clear()
    }

    @Transactional
    override fun deleteById(id: ID) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteById")
            Assert.notNull(id, "The given id must not be null!")
            findById(id).ifPresent { entity: T -> this.delete(entity) }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun deleteAll(entities: Iterable<T>) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAll")
            super.deleteAll(entities)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun deleteAll() {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAll")
            super.deleteAll()
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> saveAndFlush(entity: S): S {
        var mdc = false
        return try {
            mdc = mdcPutId(".saveAndFlush")
            super.saveAndFlush(entity)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".saveAll")
            super.saveAll(entities)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> saveAllAndFlush(entities: Iterable<S>): List<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".saveAllAndFlush")
            super.saveAllAndFlush(entities)
        } finally {
            cleanMdc(mdc)
        }
    }

    @Deprecated("")
    override fun deleteInBatch(entities: Iterable<T>) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteInBatch")
            @Suppress("DEPRECATION")
            super<SimpleJpaRepository>.deleteInBatch(entities)
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun <S : T> save(entity: S): S {
        var mdc = false
        return try {
            mdc = mdcPutId(".save")
            extJpaSupport.logicalDeletedAttribute?.setFalseIf(entity)
            if (isNew(entity, false)) {
                entityManager.persist(entity)
                entity
            } else {
                entityManager.merge(entity)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun <S : T> dynamicSave(s: S): S {
        var mdc = false
        return try {
            mdc = mdcPutId(".dynamicSave")
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
            s
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun lowLevelUpdate(spec: UpdateSpecification<T>): Long {
        return update(s = null, spec = spec, lowLevel = true, physical = true, mdcId = ".lowLevelUpdate")
    }

    @Transactional
    override fun physicalUpdate(spec: UpdateSpecification<T>): Long {
        return update(s = null, spec = spec, lowLevel = false, physical = true, mdcId = ".physicalUpdate")
    }

    @Transactional
    override fun update(spec: UpdateSpecification<T>): Long {
        return update(s = null, spec = spec, lowLevel = false, physical = false, mdcId = ".update")
    }

    @Transactional
    override fun <S : T> lowLevelUpdate(s: S?, spec: UpdateSpecification<T>): Long {
        return update(s = s, spec = spec, lowLevel = true, physical = true, mdcId = ".lowLevelUpdate")
    }

    @Transactional
    override fun <S : T> physicalUpdate(s: S?, spec: UpdateSpecification<T>): Long {
        return update(s = s, spec = spec, lowLevel = false, physical = true, mdcId = ".physicalUpdate")
    }

    @Transactional
    override fun <S : T> update(s: S?, spec: UpdateSpecification<T>): Long {
        return update(s = s, spec = spec, lowLevel = false, physical = false, mdcId = ".update")
    }

    private fun <S : T> update(s: S?, spec: UpdateSpecification<T>, lowLevel: Boolean, physical: Boolean, mdcId: String): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(mdcId)
            var spec1: Specification<T> = spec
            if (!physical) {
                spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            }
            val builder = entityManager.criteriaBuilder
            val criteriaUpdate = spec.createCriteriaUpdate(domainClass, builder, extJpaSupport)
            val root = criteriaUpdate.root

            if (s != null) {
                val beanWrapper = DirectFieldAccessFallbackBeanWrapper(s)
                for (attribute in root.model.singularAttributes) {
                    val attributeName = attribute.name
                    val attributeValue = beanWrapper.getPropertyValue(attributeName) ?: continue
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
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} row affected", affected)
            }
            val idAttribute = spec.idAttribute
            val versionAttribute = spec.versionAttribute
            if (idAttribute != null && versionAttribute != null && affected == 0) {
                throw ObjectOptimisticLockingFailureException(domainClass, idAttribute.value)
            }
            affected.toLong()
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun delete(entity: T) {
        var mdc = false
        try {
            mdc = mdcPutId(".delete")
            extJpaSupport.logicalDeletedAttribute?.apply {
                delete(entity)
                entityManager.merge(entity)
            } ?: super.delete(entity)
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun delete(spec: Specification<T>): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".delete")
            if (extJpaSupport.logicalDeletedSupported) {
                doLogicalDelete(spec)
            } else {
                doPhysicalDelete(spec)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun physicalDelete(spec: Specification<T>): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".physicalDelete")
            doPhysicalDelete(spec)
        } finally {
            cleanMdc(mdc)
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
        if (sqlLog.isDebugEnabled) {
            sqlLog.debug("{} row affected", affected)
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
        if (sqlLog.isDebugEnabled) {
            sqlLog.debug("{} row affected", affected)
        }
        return affected.toLong()
    }

    @Transactional
    override fun deleteAllById(ids: Iterable<ID>) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAllById")
            delete { root: Root<T>, query: CriteriaQuery<*>?, builder: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection(ids)) }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun deleteAllByIdInBatch(ids: Iterable<ID>) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAllByIdInBatch")
            if (extJpaSupport.logicalDeletedSupported) {
                doLogicalDelete { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection(ids)) }
            } else {
                super.deleteAllByIdInBatch(ids)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun deleteAllInBatch(entities: Iterable<T>) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAllInBatch")
            if (extJpaSupport.logicalDeletedSupported) {
                Assert.notNull(entities, "The given Iterable of entities not be null!")
                if (!entities.iterator().hasNext()) {
                    return
                }
                val ids: List<ID?> = entities.map { entityInformation.getId(it) }
                val spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(ids) }

                val affected = doLogicalDelete(spec)
                if (sqlLog.isDebugEnabled) {
                    sqlLog.debug("{} row affected", affected)
                }
            } else {
                super.deleteAllInBatch(entities)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun deleteAllInBatch() {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAllInBatch")
            if (extJpaSupport.logicalDeletedSupported) {
                val affected = doLogicalDelete(null)
                if (sqlLog.isDebugEnabled) {
                    sqlLog.debug("{} row affected", affected)
                }
            } else {
                super.deleteAllInBatch()
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findById(id: ID): Optional<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findById")
            if (extJpaSupport.logicalDeletedSupported) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                spec = extJpaSupport.logicalDeletedAttribute!!.andNotDeleted(spec)
                super.findOne(spec)
            } else {
                super.findById(id)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findPhysicalById(id: ID): Optional<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findPhysicalById")
            super.findById(id)
        } finally {
            cleanMdc(mdc)
        }
    }

    @Deprecated("")
    override fun getOne(id: ID): T {
        var mdc = false
        return try {
            mdc = mdcPutId(".getOne")
            getById(id)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun getById(id: ID): T {
        var mdc = false
        return try {
            mdc = mdcPutId(".getById")
            if (extJpaSupport.logicalDeletedSupported) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                spec = extJpaSupport.logicalDeletedAttribute!!.andNotDeleted(spec)
                super.findOne(spec).orElseThrow { EntityNotFoundException("Unable to find $domainClass with id $id") }
            } else {
                super.getById(id)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun existsById(id: ID): Boolean {
        var mdc = false
        return try {
            mdc = mdcPutId(".existsById")
            if (extJpaSupport.logicalDeletedSupported) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                spec = extJpaSupport.logicalDeletedAttribute!!.andNotDeleted(spec)
                super.count(spec) > 0
            } else {
                super.existsById(id)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification)
            } else {
                super.findAll()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllById(ids: Iterable<ID>): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllById")
            val spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection(ids)) }
            val all = super.findAll(extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec)
                    ?: spec)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", all.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(sort: Sort): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification, sort)
            } else {
                super.findAll(sort)
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(pageable: Pageable): Page<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            val result: Page<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.notDeletedSpecification, pageable)
            } else {
                super.findAll(pageable)
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", result.totalElements)
                sqlLog.debug("{} rows retrieved", result.content.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(size: Int): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            val spec: Specification<T>? = extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
            findUnpaged(spec, PageRequest.of(0, size))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(size: Int, sort: Sort): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            val spec: Specification<T>? = extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
            findUnpaged(spec, PageRequest.of(0, size, sort))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun count(spec: Specification<T>?): Long {
        var spec1: Specification<T>? = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".count")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            val count = super.count(spec1)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", count)
            }
            count
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun countPhysical(spec: Specification<T>?): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".count")
            val count = super.count(spec)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", count)
            }
            count
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun exists(spec: Specification<T>): Boolean {
        var mdc = false
        return try {
            mdc = mdcPutId(".exists")
            count(spec) > 0
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun existsPhysical(spec: Specification<T>?): Boolean {
        var mdc = false
        return try {
            mdc = mdcPutId(".exists")
            countPhysical(spec) > 0
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findFirst(sort: Sort): Optional<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findFirst")
            val spec = extJpaSupport.logicalDeletedAttribute?.notDeletedSpecification
            findUnpaged(spec, PageRequest.of(0, 1, sort)).stream().findFirst()
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findFirst(spec: Specification<T>?): Optional<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findFirst")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            findUnpaged(spec1, PageRequest.of(0, 1)).stream().findFirst()
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findOne(spec: Specification<T>?): Optional<T> {
        var spec1: Specification<T>? = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findOne")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            super.findOne(spec1)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(spec: Specification<T>?): List<T> {
        var spec1: Specification<T>? = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            val all = super.findAll(spec1)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", all.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(spec: Specification<T>?, size: Int): List<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            findUnpaged(spec1, PageRequest.of(0, size))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            findUnpaged(spec1, PageRequest.of(0, size, sort))
        } finally {
            cleanMdc(mdc)
        }
    }

    private fun findUnpaged(spec: Specification<T>?, pageable: PageRequest): List<T> {
        val query = getQuery(spec, pageable)
        query.setFirstResult(pageable.offset.toInt())
        query.setMaxResults(pageable.pageSize)
        val content = query.resultList
        if (sqlLog.isDebugEnabled) {
            sqlLog.debug("{} rows retrieved", content.size)
        }
        return PageableList(content, pageable,
                min(pageable.pageSize, content.size).toLong())
    }

    override fun findPhysicalAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findPhysicalAll")
            val all = super.findAll(spec, pageable)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", all.totalElements)
                sqlLog.debug("{} rows retrieved", all.content.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findPhysicalAllById(ids: Iterable<ID>): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findPhysicalAllById")
            val all = super.findAllById(ids)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", all.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        var spec1: Specification<T>? = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            val all = super.findAll(spec1, pageable)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", all.totalElements)
                sqlLog.debug("{} rows retrieved", all.content.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(spec: Specification<T>?, sort: Sort): List<T> {
        var spec1: Specification<T>? = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            spec1 = extJpaSupport.logicalDeletedAttribute?.andNotDeleted(spec1) ?: spec1
            val all = super.findAll(spec1, sort)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", all.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> findFirst(example: Example<S>): Optional<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findFirst")
            findUnpaged(example, PageRequest.of(0, 1)).stream().findFirst()
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> findOne(example: Example<S>): Optional<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findOne")
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            super.findOne(example)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T, R> findBy(example: Example<S>, queryFunction: Function<FetchableFluentQuery<S>, R>): R {
        var mdc = false
        return try {
            mdc = mdcPutId(".findBy")
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            super.findBy(example, queryFunction)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> count(example: Example<S>): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".count")
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val count = super.count(example)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", count)
            }
            count
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> exists(example: Example<S>): Boolean {
        var mdc = false
        return try {
            mdc = mdcPutId(".exists")
            count(example) > 0
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> findAll(example: Example<S>): List<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val all = super.findAll(example)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", all.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> findAll(example: Example<S>, size: Int): List<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            findUnpaged(example, PageRequest.of(0, size))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> findAll(example: Example<S>, size: Int, sort: Sort): List<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            findUnpaged(example, PageRequest.of(0, size, sort))
        } finally {
            cleanMdc(mdc)
        }
    }

    private fun <S : T> findUnpaged(example: Example<S>, pageable: PageRequest): List<S> {
        extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
        val probeType = example.probeType
        val query = getQuery(ExampleSpecification(example, escapeCharacter), probeType,
                pageable)
        if (pageable.isPaged) {
            query.setFirstResult(pageable.offset.toInt())
            query.setMaxResults(pageable.pageSize)
        }
        val content = query.resultList
        if (sqlLog.isDebugEnabled) {
            sqlLog.debug("{} rows retrieved", content.size)
        }
        return PageableList(content, pageable, min(pageable.pageSize, content.size).toLong())
    }

    override fun <S : T> findAll(example: Example<S>, sort: Sort): List<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val all = super.findAll(example, sort)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", all.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> findAll(example: Example<S>, pageable: Pageable): Page<S> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            extJpaSupport.logicalDeletedAttribute?.restore(example.probe)
            val all = super.findAll(example, pageable)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", all.totalElements)
                sqlLog.debug("{} rows retrieved", all.content.size)
            }
            all
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun count(): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".count")
            val count: Long = if (extJpaSupport.logicalDeletedSupported) {
                count(null)
            } else {
                super.count()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", count)
            }
            count
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun cleanRecycleBin(): Int {
        var mdc = false
        return try {
            mdc = mdcPutId(".cleanRecycleBin")
            var reslut = 0L
            if (extJpaSupport.logicalDeletedSupported) {
                reslut = doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows affected", reslut)
            }
            reslut.toInt()
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun deleteFromRecycleBin(id: ID) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteFromRecycleBin")
            if (extJpaSupport.logicalDeletedSupported) {
                val spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
                val entity = findByIdFromRecycleBin(id)
                entity.ifPresent { t: T -> super.delete(t) }
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun deleteAllByIdFromRecycleBin(ids: Iterable<ID>) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteAllByIdFromRecycleBin")
            if (extJpaSupport.logicalDeletedSupported) {
                val spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection(ids)) }
                doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
            } else {
                if (sqlLog.isDebugEnabled) {
                    sqlLog.debug("{} rows affected", 0)
                }
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun deleteFromRecycleBin(spec: Specification<T>?) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteFromRecycleBin")
            if (extJpaSupport.logicalDeletedSupported) {
                doPhysicalDelete(extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec))
            } else {
                if (sqlLog.isDebugEnabled) {
                    sqlLog.debug("{} rows affected", 0)
                }
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun countRecycleBin(): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".countRecycleBin")
            val count: Long = if (extJpaSupport.logicalDeletedSupported) {
                super.count(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
            } else {
                0
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", count)
            }
            count
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun countRecycleBin(spec: Specification<T>?): Long {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".countRecycleBin")
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
            }
            val count = super.count(spec1)
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", count)
            }
            count
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun existsInRecycleBin(spec: Specification<T>?): Boolean {
        var mdc = false
        return try {
            mdc = mdcPutId(".existsInRecycleBin")
            countRecycleBin(spec) > 0
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findByIdFromRecycleBin(id: ID): Optional<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findByIdFromRecycleBin")
            if (extJpaSupport.logicalDeletedSupported) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(root[entityInformation.idAttribute], id)
                }
                spec = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
                super.findOne(spec)
            } else {
                Optional.empty()
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllByIdFromRecycleBin(ids: Iterable<ID>): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllByIdFromRecycleBin")
            val result: List<T>
            if (extJpaSupport.logicalDeletedSupported) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection(ids)) }
                spec = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec)
                result = super.findAll(spec)
            } else {
                result = emptyList()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findOneFromRecycleBin(spec: Specification<T>?): Optional<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findOneFromRecycleBin")
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                super.findOne(spec1)
            } else {
                Optional.empty()
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findFirstFromRecycleBin(spec: Specification<T>?): Optional<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findFirstFromRecycleBin")
            findUnpagedFromRecycleBin(spec, PageRequest.of(0, 1)).stream().findFirst()
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification)
            } else {
                emptyList()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(size: Int): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            findUnpagedFromRecycleBin(null, PageRequest.of(0, size))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(size: Int, sort: Sort): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            findUnpagedFromRecycleBin(null, PageRequest.of(0, size, sort))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(pageable: Pageable): Page<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            val result: Page<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification, pageable)
            } else {
                Page.empty(pageable)
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", result.totalElements)
                sqlLog.debug("{} rows retrieved", result.content.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(sort: Sort): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            val result: List<T> = if (extJpaSupport.logicalDeletedSupported) {
                super.findAll(extJpaSupport.logicalDeletedAttribute!!.deletedSpecification, sort)
            } else {
                emptyList()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?): List<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            val result: List<T>
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                result = super.findAll(spec1)
            } else {
                result = emptyList()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, size: Int): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            findUnpagedFromRecycleBin(spec, PageRequest.of(0, size))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            findUnpagedFromRecycleBin(spec, PageRequest.of(0, size, sort))
        } finally {
            cleanMdc(mdc)
        }
    }

    private fun findUnpagedFromRecycleBin(spec: Specification<T>?, pageable: PageRequest): List<T> {
        var spec1 = spec
        return if (extJpaSupport.logicalDeletedSupported) {
            spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
            findUnpaged(spec1, pageable)
        } else {
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", 0)
            }
            PageableList(pageable)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, pageable: Pageable): Page<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            val result: Page<T>
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                result = super.findAll(spec1, pageable)
            } else {
                result = Page.empty(pageable)
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("total: {} rows", result.totalElements)
                sqlLog.debug("{} rows retrieved", result.content.size)
            }
            result
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAllFromRecycleBin(spec: Specification<T>?, sort: Sort): List<T> {
        var spec1 = spec
        var mdc = false
        return try {
            mdc = mdcPutId(".findAllFromRecycleBin")
            val result: List<T>
            if (extJpaSupport.logicalDeletedSupported) {
                spec1 = extJpaSupport.logicalDeletedAttribute!!.andDeleted(spec1)
                result = super.findAll(spec1, sort)
            } else {
                result = emptyList()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows retrieved", result.size)
            }
            result
        } finally {
            cleanMdc(mdc)
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
