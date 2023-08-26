package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.provider.PersistenceProvider
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.data.jpa.repository.query.QueryUtils
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
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
        private val auditorAware: AuditorAware<*>?,
        private val entityInformation: JpaEntityInformation<T, ID>, override val entityManager: EntityManager
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager), JpaExtRepository<T, ID> {
    private val sqlLog = LoggerFactory.getLogger("org.hibernate.SQL")
    private val provider: PersistenceProvider = PersistenceProvider.fromEntityManager(entityManager)
    private val extJpaSupport: ExtJpaSupport
    private val escapeCharacter = EscapeCharacter.DEFAULT
    private val notDeleted: Any?
    private val deleted: Any?
    private val notDeletedSpec: Specification<T>
    private val deletedSpec: Specification<T>

    init {
        extJpaSupport = DefaultExtJpaSupport(jpaExtProperties, domainClass)
        notDeleted = extJpaSupport.logicalDeletedFalseValue
        deleted = extJpaSupport.logicalDeletedTrueValue
        notDeletedSpec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
            builder.equal(
                    root.get<Any>(extJpaSupport.logicalDeletedPropertyName), notDeleted)
        }
        deletedSpec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
            builder.equal(
                    root.get<Any>(extJpaSupport.logicalDeletedPropertyName), deleted)
        }
    }

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
            if (extJpaSupport.supportLogicalDeleted() && !extJpaSupport.logicalDeletedSeted(entity)) {
                extJpaSupport.setUnLogicalDeleted(entity)
            }
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
    override fun lowLevelUpdate(spec: UpdateSpecification<T>): Int {
        return update(spec = spec, lowLevel = true, physical = false, mdcId = ".lowLevelUpdate")
    }

    @Transactional
    override fun physicalUpdate(spec: UpdateSpecification<T>): Int {
        return update(spec = spec, lowLevel = false, physical = true, mdcId = ".physicalUpdate")
    }

    @Transactional
    override fun update(spec: UpdateSpecification<T>): Int {
        return update(spec = spec, lowLevel = false, physical = false, mdcId = ".update")
    }

    private fun update(spec: UpdateSpecification<T>, lowLevel: Boolean, physical: Boolean, mdcId: String): Int {
        var mdc = false
        return try {
            mdc = mdcPutId(mdcId)
            var spec1: Specification<T>? = spec
            if (!physical && extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
            val builder = entityManager.criteriaBuilder
            val criteriaUpdate = spec.createCriteriaUpdate(domainClass, builder)
            val root = criteriaUpdate.root
            if (!lowLevel) {
                val lastModifiedDatePropertyName = extJpaSupport.lastModifiedDatePropertyName
                if (lastModifiedDatePropertyName != null) {
                    criteriaUpdate[lastModifiedDatePropertyName] = extJpaSupport.lastModifiedDateNowValue
                }
                val lastModifiedByPropertyName = extJpaSupport.lastModifiedByPropertyName
                if (auditorAware != null && lastModifiedByPropertyName != null) {
                    criteriaUpdate[lastModifiedByPropertyName] = extJpaSupport.lastModifiedBy(auditorAware.currentAuditor.get())
                }
                val versionPropertyName = extJpaSupport.versionPropertyName
                if (versionPropertyName != null) {
                    val versionIncValue = extJpaSupport.versionIncValue
                    if (versionIncValue is Number) {
                        val path = root.get<Number>(versionPropertyName)
                        criteriaUpdate.set(path, builder.sum(path, versionIncValue))
                    } else {
                        criteriaUpdate[versionPropertyName] = versionIncValue
                    }
                }
            }
            val predicate = spec1!!.toPredicate(root, builder.createQuery(), builder)
            if (predicate != null) {
                criteriaUpdate.where(predicate)
            }
            val affected = entityManager.createQuery(criteriaUpdate).executeUpdate()
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} row affected", affected)
            }
            affected
        } finally {
            cleanMdc(mdc)
        }
    }


    @Transactional
    override fun <S : T> physicalUpdate(s: S, spec: Specification<T>): Int {
        return update(s, spec, true, ".physicalUpdate")
    }

    @Transactional
    override fun <S : T> update(s: S, spec: Specification<T>): Int {
        return update(s, spec, false, ".update")
    }

    private fun <S : T> update(s: S, spec: Specification<T>, physical: Boolean, mdcId: String): Int {
        var mdc = false
        return try {
            mdc = mdcPutId(mdcId)
            var spec1: Specification<T>? = spec
            if (!physical && extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
            val builder = entityManager.criteriaBuilder
            val domainClass = domainClass
            val criteriaUpdate = builder.createCriteriaUpdate(domainClass)
            val root = criteriaUpdate.from(domainClass)
            val beanWrapper = DirectFieldAccessFallbackBeanWrapper(s)
            for (attribute in root.model.singularAttributes) {
                val attributeName = attribute.name
                val attributeValue = beanWrapper.getPropertyValue(attributeName) ?: continue
                criteriaUpdate[attributeName] = attributeValue
            }
            val lastModifiedDatePropertyName = extJpaSupport.lastModifiedDatePropertyName
            if (lastModifiedDatePropertyName != null) {
                criteriaUpdate[lastModifiedDatePropertyName] = extJpaSupport.lastModifiedDateNowValue
            }
            val lastModifiedByPropertyName = extJpaSupport.lastModifiedByPropertyName
            if (auditorAware != null && lastModifiedByPropertyName != null) {
                criteriaUpdate[lastModifiedByPropertyName] = extJpaSupport.lastModifiedBy(auditorAware.currentAuditor.get())
            }
            val versionPropertyName = extJpaSupport.versionPropertyName
            if (versionPropertyName != null) {
                val versionIncValue = extJpaSupport.versionIncValue
                if (versionIncValue is Number) {
                    val path = root.get<Number>(versionPropertyName)
                    criteriaUpdate.set(path, builder.sum(path, versionIncValue))
                } else {
                    criteriaUpdate[versionPropertyName] = versionIncValue
                }
            }
            val predicate = spec1!!.toPredicate(root, builder.createQuery(), builder)
            if (predicate != null) {
                criteriaUpdate.where(predicate)
            }
            val affected = entityManager.createQuery(criteriaUpdate).executeUpdate()
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} row affected", affected)
            }
            affected
        } finally {
            cleanMdc(mdc)
        }
    }

    @Deprecated("")
    @Transactional
    override fun <S : T> dynamicSave(s: S): S {
        var mdc = false
        return try {
            mdc = mdcPutId(".dynamicSave")
            if (extJpaSupport.supportLogicalDeleted() && !extJpaSupport.logicalDeletedSeted(s)) {
                extJpaSupport.setUnLogicalDeleted(s)
            }
            if (isNew(s, true)) {
                entityManager.persist(s)
                s
            } else {
                val optional = findById(entityInformation.getId(s)!!)
                if (optional.isPresent) {
                    val exist = optional.get()
                    s.nullFrom(exist)
                    entityManager.merge(s)
                } else {
                    entityManager.persist(s)
                    s
                }
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun delete(entity: T) {
        var mdc = false
        try {
            mdc = mdcPutId(".delete")
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setLogicalDeleted(entity)
                entityManager.merge(entity)
            } else {
                super.delete(entity)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun delete(spec: Specification<T>): Int {
        var mdc = false
        return try {
            mdc = mdcPutId(".delete")
            if (extJpaSupport.supportLogicalDeleted()) {
                logicalDelete(spec)
            } else {
                physicalDelete(spec)
            }
        } finally {
            cleanMdc(mdc)
        }
    }

    private fun logicalDelete(spec: Specification<T>): Int {
        var spec1: Specification<T>? = spec
        val builder = entityManager.criteriaBuilder
        val domainClass = domainClass
        val criteriaUpdate = builder.createCriteriaUpdate(domainClass)
        val root = criteriaUpdate.from(domainClass)
        spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
        val predicate = spec1!!.toPredicate(root, builder.createQuery(), builder)
        if (predicate != null) {
            criteriaUpdate.where(predicate)
        }
        criteriaUpdate.set(root.get(extJpaSupport.logicalDeletedPropertyName), deleted)
        val affected = entityManager.createQuery(criteriaUpdate).executeUpdate()
        if (sqlLog.isDebugEnabled) {
            sqlLog.debug("{} row affected", affected)
        }
        return affected
    }

    private fun physicalDelete(spec: Specification<T>?): Int {
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
        return affected
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
            if (extJpaSupport.supportLogicalDeleted()) {
                logicalDelete { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection<ID?>(ids)) }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                Assert.notNull(entities, "The given Iterable of entities not be null!")
                if (!entities.iterator().hasNext()) {
                    return
                }
                val logicalDeleteName = extJpaSupport.logicalDeletedPropertyName
                val oldName = "old$logicalDeleteName"
                val queryString = String.format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.entityName,
                        logicalDeleteName,
                        logicalDeleteName, logicalDeleteName, oldName)
                var iterator = entities.iterator()
                if (iterator.hasNext()) {
                    val alias = "e"
                    val builder = StringBuilder(queryString)
                    builder.append(" and (")
                    var i = 0
                    while (iterator.hasNext()) {
                        iterator.next()
                        builder.append(String.format(" %s = ?%d", alias, ++i))
                        if (iterator.hasNext()) {
                            builder.append(" or")
                        }
                    }
                    builder.append(" )")
                    val query = entityManager.createQuery(builder.toString())
                    iterator = entities.iterator()
                    i = 0
                    query.setParameter(logicalDeleteName, deleted)
                    query.setParameter(oldName, notDeleted)
                    while (iterator.hasNext()) {
                        query.setParameter(++i, iterator.next())
                    }
                    val affected = query.executeUpdate()
                    if (sqlLog.isDebugEnabled) {
                        sqlLog.debug("{} row affected", affected)
                    }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                val logicalDeleteName = extJpaSupport.logicalDeletedPropertyName
                val oldName = "old$logicalDeleteName"
                val affected = entityManager.createQuery(String.format(SOFT_DELETE_ALL_QUERY_STRING, entityInformation.entityName,
                        logicalDeleteName, logicalDeleteName, logicalDeleteName, oldName))
                        .setParameter(logicalDeleteName, deleted)
                        .setParameter(oldName, notDeleted)
                        .executeUpdate()
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
            if (extJpaSupport.supportLogicalDeleted()) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                spec = spec.and(notDeletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                spec = spec.and(notDeletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(
                            root[entityInformation.idAttribute], id)
                }
                spec = spec.and(notDeletedSpec)
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
            val result: List<T> = if (extJpaSupport.supportLogicalDeleted()) {
                super.findAll(notDeletedSpec)
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
            var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection<ID?>(ids)) }
            if (extJpaSupport.supportLogicalDeleted()) {
                spec = spec.and(notDeletedSpec)
            }
            val all = super.findAll(spec)
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
            val result: List<T> = if (extJpaSupport.supportLogicalDeleted()) {
                super.findAll(notDeletedSpec, sort)
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
            val result: Page<T> = if (extJpaSupport.supportLogicalDeleted()) {
                super.findAll(notDeletedSpec, pageable)
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
            var spec: Specification<T>? = null
            if (extJpaSupport.supportLogicalDeleted()) {
                spec = notDeletedSpec
            }
            findUnpaged(spec, PageRequest.of(0, size))
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun findAll(size: Int, sort: Sort): List<T> {
        var mdc = false
        return try {
            mdc = mdcPutId(".findAll")
            var spec: Specification<T>? = null
            if (extJpaSupport.supportLogicalDeleted()) {
                spec = notDeletedSpec
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
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
            var spec: Specification<T>? = null
            if (extJpaSupport.supportLogicalDeleted()) {
                spec = notDeletedSpec
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) notDeletedSpec else spec1.and(notDeletedSpec)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setUnLogicalDeleted(example.probe)
            }
            super.findOne(example)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T, R> findBy(
            example: Example<S>,
            queryFunction: Function<FetchableFluentQuery<S>, R>,
    ): R {
        var mdc = false
        return try {
            mdc = mdcPutId(".findBy")
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setUnLogicalDeleted(example.probe)
            }
            super.findBy(example, queryFunction)
        } finally {
            cleanMdc(mdc)
        }
    }

    override fun <S : T> count(example: Example<S>): Long {
        var mdc = false
        return try {
            mdc = mdcPutId(".count")
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setUnLogicalDeleted(example.probe)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setUnLogicalDeleted(example.probe)
            }
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
        if (extJpaSupport.supportLogicalDeleted()) {
            extJpaSupport.setUnLogicalDeleted(example.probe)
        }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setUnLogicalDeleted(example.probe)
            }
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
            if (extJpaSupport.supportLogicalDeleted()) {
                extJpaSupport.setUnLogicalDeleted(example.probe)
            }
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
            val count: Long = if (extJpaSupport.supportLogicalDeleted()) {
                val logicalDeleteName = extJpaSupport.logicalDeletedPropertyName
                val queryString = String.format(QueryUtils.COUNT_QUERY_STRING + " WHERE " + EQUALS_CONDITION_STRING,
                        provider.countQueryPlaceholder, entityInformation.entityName, "x",
                        logicalDeleteName, logicalDeleteName)
                entityManager.createQuery(queryString, Long::class.javaObjectType).setParameter(logicalDeleteName,
                        notDeleted).singleResult
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
            var reslut = 0
            if (extJpaSupport.supportLogicalDeleted()) {
                val logicalDeleteName = extJpaSupport.logicalDeletedPropertyName
                reslut = entityManager.createQuery(String.format("delete from %s x where x.%s = :%s", entityInformation.entityName,
                        logicalDeleteName,
                        logicalDeleteName)).setParameter(logicalDeleteName,
                        deleted).executeUpdate()
            }
            if (sqlLog.isDebugEnabled) {
                sqlLog.debug("{} rows affected", reslut)
            }
            reslut
        } finally {
            cleanMdc(mdc)
        }
    }

    @Transactional
    override fun deleteFromRecycleBin(id: ID) {
        var mdc = false
        try {
            mdc = mdcPutId(".deleteFromRecycleBin")
            if (extJpaSupport.supportLogicalDeleted()) {
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
            if (extJpaSupport.supportLogicalDeleted()) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection<ID?>(ids)) }
                spec = spec.and(deletedSpec)
                physicalDelete(spec)
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
        var spec1 = spec
        var mdc = false
        try {
            mdc = mdcPutId(".deleteFromRecycleBin")
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
                physicalDelete(spec1)
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
            val count: Long = if (extJpaSupport.supportLogicalDeleted()) {
                super.count(deletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
                    builder.equal(root[entityInformation.idAttribute], id)
                }
                spec = spec.and(deletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                var spec = Specification { root: Root<T>, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> root[entityInformation.idAttribute].`in`(toCollection<ID?>(ids)) }
                spec = spec.and(deletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
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
            val result: List<T> = if (extJpaSupport.supportLogicalDeleted()) {
                super.findAll(deletedSpec)
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
            val result: Page<T> = if (extJpaSupport.supportLogicalDeleted()) {
                super.findAll(deletedSpec, pageable)
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
            val result: List<T> = if (extJpaSupport.supportLogicalDeleted()) {
                super.findAll(deletedSpec, sort)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
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
        return if (extJpaSupport.supportLogicalDeleted()) {
            spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
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
            if (extJpaSupport.supportLogicalDeleted()) {
                spec1 = if (spec1 == null) deletedSpec else spec1.and(deletedSpec)
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
        const val SOFT_DELETE_ALL_QUERY_STRING = "update %s e set e.%s = :%s where e.%s = :%s"
        private const val EQUALS_CONDITION_STRING = "%s.%s = :%s"
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
