package top.bettercode.summer.data.jpa.query

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.domain.Sort
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import top.bettercode.summer.data.jpa.metamodel.SingularAttributeValue
import top.bettercode.summer.data.jpa.support.ExtJpaSupport
import top.bettercode.summer.data.jpa.support.UpdateSpecification
import java.util.*
import java.util.stream.Collectors
import javax.persistence.criteria.*
import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.Attribute.PersistentAttributeType
import javax.persistence.metamodel.ManagedType

/**
 * @author Peter Wu
 */
open class SpecMatcher<T : Any?, M : SpecMatcher<T, M>> protected constructor(
        val matchMode: SpecMatcherMode, probe: Any?
) : UpdateSpecification<T>, SpecPredicate<T, M> {

    private val log: Logger = LoggerFactory.getLogger(SpecMatcher::class.java)

    private val specPredicates: MutableMap<String, SpecPredicate<T, M>> = LinkedHashMap()
    private val orders: MutableList<Sort.Order> = ArrayList()
    private val typed: M
    private val probe: Any?
    override var idAttribute: SingularAttributeValue<T, *>? = null
    override var versionAttribute: SingularAttributeValue<T, *>? = null

    //--------------------------------------------
    init {
        this.probe = probe
        @Suppress("UNCHECKED_CAST", "LeakingThis")
        typed = this as M
    }

    override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, cb: CriteriaBuilder): Predicate? {
        if (orders.isNotEmpty()) {
            val orders = orders.stream().map { o: Sort.Order ->
                val path: Path<*> = SpecPath.toPath(root, o.property)
                if (o.direction.isDescending) cb.desc(path) else cb.asc(path)
            }.collect(Collectors.toList())
            query.orderBy(orders)
        }
        return this.toPredicate(root, cb)
    }

    override fun toPredicate(root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate? {
        if (probe != null) {
            setPathDefaultValue("", root, root.model, probe, probe.javaClass,
                    PathNode("root", null, probe))
        }
        val predicates: MutableList<Predicate> = ArrayList()
        for (it in specPredicates.values) {
            val predicate = it.toPredicate(root, criteriaBuilder)
            if (predicate != null) {
                predicates.add(predicate)
            } else if (it is SpecPath<*, *, *>) {
                if (idAttribute != null && it.propertyName == idAttribute?.name) {
                    this.idAttribute = null
                } else if (versionAttribute != null && it.propertyName == versionAttribute?.name) {
                    this.versionAttribute = null
                }
            }
        }
        if (predicates.isEmpty()) {
            return null
        }
        val restrictions = predicates.toTypedArray<Predicate>()
        return if (matchMode == SpecMatcherMode.ALL) criteriaBuilder.and(*restrictions) else criteriaBuilder.or(*restrictions)
    }

    private fun setPathDefaultValue(path: String, from: Path<*>, type: ManagedType<*>, probe: Any, probeType: Class<*>, currentNode: PathNode) {
        val beanWrapper = DirectFieldAccessFallbackBeanWrapper(probe)
        for (attribute in type.singularAttributes) {
            val currentPath = if (path.isBlank()) attribute.name else path + "." + attribute.name
            val specPath: SpecPath<Any, T, M> = path(currentPath)
            if (specPath.isIgnored) {
                continue
            }
            val attributeValue: Any? = try {
                beanWrapper.getPropertyValue(attribute.name)
            } catch (e: Exception) {
                log.debug("获取属性值失败", e)
                null
            }
            if (attributeValue == null || "" == attributeValue) {
                continue
            }
            if (attribute.persistentAttributeType == PersistentAttributeType.EMBEDDED || isAssociation(attribute) && from !is From<*, *>) {
                setPathDefaultValue(currentPath, from.get<Any>(attribute.name),
                        attribute.type as ManagedType<*>, attributeValue, probeType, currentNode)
                continue
            }
            if (isAssociation(attribute)) {
                val node = currentNode.add(attribute.name, attributeValue)
                if (node.spansCycle()) {
                    throw InvalidDataAccessApiUsageException(String.format(
                            "Path '%s' from root %s must not span a cyclic property reference!\r\n%s",
                            currentPath,
                            ClassUtils.getShortName(probeType), node))
                }
                setPathDefaultValue(currentPath, (from as From<*, *>).join<Any, Any>(attribute.name),
                        attribute.type as ManagedType<*>, attributeValue, probeType, node)
                continue
            }
            if (!specPath.isSetCriteria) {
                specPath.criteria(attributeValue)
            }
        }
    }

    override fun createCriteriaUpdate(domainClass: Class<T>, criteriaBuilder: CriteriaBuilder, extJpaSupport: ExtJpaSupport<T>): CriteriaUpdate<T> {
        val criteriaUpdate = criteriaBuilder.createCriteriaUpdate(domainClass)
        criteriaUpdate.from(domainClass)
        specPredicates.values.filter { it is SpecPath<*, *, *> }.map { it as SpecPath<*, T, M> }.forEach {
            if (it.isSetCriteriaUpdate) {
                criteriaUpdate[it.propertyName] = it.criteriaUpdate
            }
            val idAttribute = extJpaSupport.idAttribute
            if (idAttribute != null && it.propertyName == idAttribute.name) {
                this.idAttribute = SingularAttributeValue(idAttribute, it.criteria!!)
            } else {
                val versionAttribute = extJpaSupport.versionAttribute
                if (versionAttribute != null && it.propertyName == versionAttribute.name) {
                    this.versionAttribute = SingularAttributeValue(versionAttribute, it.criteria!!)
                }
            }
        }

        return criteriaUpdate
    }

    fun <P> path(propertyName: String): SpecPath<P, T, M> {
        Assert.hasText(propertyName, "propertyName can not be blank.")
        val predicate = specPredicates.computeIfAbsent(propertyName
        ) { s: String -> return@computeIfAbsent SpecPath<P, T, M>(typed, s) }
        @Suppress("UNCHECKED_CAST")
        return predicate as SpecPath<P, T, M>
    }

    //--------------------------------------------
    fun all(matcher: (M) -> M): M {
        return this.all(null, matcher)
    }

    fun all(other: T?, matcher: (M) -> M): M {

        val constructor = typed.javaClass.declaredConstructors[0]
        constructor.isAccessible = true
        @Suppress("UNCHECKED_CAST") val otherMatcher = constructor.newInstance(SpecMatcherMode.ALL, other) as M
        matcher(otherMatcher)
        specPredicates["all" + UUID.randomUUID().toString()] = otherMatcher
        return typed
    }

    fun any(matcher: (M) -> M): M {
        return this.any(null, matcher)
    }

    fun any(other: T?, matcher: (M) -> M): M {
        val constructor = typed.javaClass.declaredConstructors[0]
        constructor.isAccessible = true
        @Suppress("UNCHECKED_CAST") val otherMatcher = constructor.newInstance(SpecMatcherMode.ANY, other) as M
        matcher(otherMatcher)
        specPredicates["any" + UUID.randomUUID().toString()] = otherMatcher
        return typed
    }

    //--------------------------------------------
    fun sortBy(direction: Sort.Direction, vararg propertyName: String): M {
        orders.addAll(Arrays.stream(propertyName) //
                .map { it: String -> Sort.Order(direction, it) } //
                .collect(Collectors.toList()))
        return typed
    }

    fun asc(vararg propertyName: String): M {
        return this.sortBy(Sort.Direction.ASC, *propertyName)
    }

    fun desc(vararg propertyName: String): M {
        return this.sortBy(Sort.Direction.DESC, *propertyName)
    }

    fun <P> criteriaUpdate(propertyName: String, criteriaUpdate: P?): M {
        return path<P>(propertyName).criteriaUpdate(criteriaUpdate)
    }

    fun <P> criteria(propertyName: String, criteria: P?): M {
        path<P>(propertyName).criteria(criteria)
        return typed
    }

    fun <P> criteria(propertyName: String, criteria: P?, matcher: PathMatcher): M {
        return path<P>(propertyName).criteria(criteria).withMatcher(matcher)
    }

    fun <P> equal(propertyName: String, criteria: P?): M {
        return eq<P>(propertyName, criteria)
    }

    fun <P> eq(propertyName: String, criteria: P?): M {
        return criteria(propertyName, criteria, PathMatcher.EQ)
    }

    fun <P> notEqual(propertyName: String, criteria: P?): M {
        return ne(propertyName, criteria)
    }

    fun <P> ne(propertyName: String, criteria: P?): M {
        return criteria(propertyName, criteria, PathMatcher.NE)
    }

    fun <Y : Comparable<Y>?> gt(propertyName: String, criteria: Y): M {
        return criteria(propertyName, criteria, PathMatcher.GT)
    }

    fun <Y : Comparable<Y>?> ge(propertyName: String, criteria: Y): M {
        return criteria(propertyName, criteria, PathMatcher.GE)
    }

    fun <Y : Comparable<Y>?> lt(propertyName: String, criteria: Y): M {
        return criteria(propertyName, criteria, PathMatcher.LT)
    }

    fun <Y : Comparable<Y>?> le(propertyName: String, criteria: Y): M {
        return criteria(propertyName, criteria, PathMatcher.LE)
    }

    fun <Y : Comparable<Y>?> between(propertyName: String, first: Y, second: Y): M {
        return path<Y>(propertyName).between(first, second)
    }

    fun like(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.LIKE)
    }

    fun starting(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.STARTING)
    }

    fun ending(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.ENDING)
    }

    fun containing(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.CONTAINING)
    }

    fun notStarting(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.NOT_STARTING)
    }

    fun notEnding(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.NOT_ENDING)
    }

    fun notContaining(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.NOT_CONTAINING)
    }

    fun notLike(propertyName: String, criteria: String?): M {
        return criteria(propertyName, criteria, PathMatcher.NOT_LIKE)
    }

    fun <E> `in`(propertyName: String, vararg criteria: E): M {
        return path<E>(propertyName).`in`(*criteria)
    }

    fun <E> `in`(propertyName: String, criteria: Collection<E>): M {
        return path<E>(propertyName).`in`(criteria)
    }

    fun <E> notIn(propertyName: String, vararg criteria: E): M {
        return path<E>(propertyName).notIn(*criteria)
    }

    fun <E> notIn(propertyName: String, criteria: Collection<E>): M {
        return path<E>(propertyName).notIn(criteria)
    }

    companion object {
        private val ASSOCIATION_TYPES: Set<PersistentAttributeType>

        //--------------------------------------------
        init {
            ASSOCIATION_TYPES = EnumSet.of(PersistentAttributeType.MANY_TO_MANY,  //
                    PersistentAttributeType.MANY_TO_ONE,  //
                    PersistentAttributeType.ONE_TO_MANY,  //
                    PersistentAttributeType.ONE_TO_ONE)
        }

        private fun isAssociation(attribute: Attribute<*, *>): Boolean {
            return ASSOCIATION_TYPES.contains(attribute.persistentAttributeType)
        }
    }
}
