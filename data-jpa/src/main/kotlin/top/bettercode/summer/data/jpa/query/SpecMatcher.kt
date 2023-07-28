package top.bettercode.summer.data.jpa.query

import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import top.bettercode.summer.data.jpa.query.SpecPath.BetweenValue
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
//--------------------------------------------
        val matchMode: SpecMatcherMode, probe: T?
) : Specification<T>, SpecPredicate<T, M> {
    private val specPredicates: MutableMap<String, SpecPredicate<T, M>> = LinkedHashMap()
    private val orders: MutableList<Sort.Order> = ArrayList()
    private val typed: M
    private val probe: Any?

    //--------------------------------------------
    init {
        this.probe = probe
        @Suppress("UNCHECKED_CAST")
        typed = this as M
    }

    override fun toPredicate(
            root: Root<T>, query: CriteriaQuery<*>, cb: CriteriaBuilder
    ): Predicate? {
        if (orders.isNotEmpty()) {
            val orders = orders.stream().map<Order> { o: Sort.Order ->
                val path: Path<*>? = SpecPath.toPath<T>(root, o.property)
                if (o.direction.isDescending) cb.desc(path) else cb.asc(path)
            }.collect(Collectors.toList<Order>())
            query.orderBy(orders)
        }
        return this.toPredicate(root, cb)
    }

    override fun toPredicate(root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate? {
        if (probe != null) {
            setSpecPathDefaultValue("", root, root.model, probe, probe.javaClass,
                    PathNode("root", null, probe))
        }
        val predicates: MutableList<Predicate> = ArrayList()
        for (specPredicate in specPredicates.values) {
            val predicate = specPredicate.toPredicate(root, criteriaBuilder)
            if (predicate != null) {
                predicates.add(predicate)
            }
        }
        if (predicates.isEmpty()) {
            return null
        }
        val restrictions = predicates.toTypedArray<Predicate>()
        return if (matchMode == SpecMatcherMode.ALL) criteriaBuilder.and(*restrictions) else criteriaBuilder.or(*restrictions)
    }

    fun setSpecPathDefaultValue(path: String, from: Path<*>, type: ManagedType<*>, value: Any, probeType: Class<*>, currentNode: PathNode?) {
        val beanWrapper = DirectFieldAccessFallbackBeanWrapper(
                value)
        for (attribute in type.singularAttributes) {
            val currentPath = if (!StringUtils.hasText(path)) attribute.name else path + "." + attribute.name
            val specPath = specPath(currentPath)
            if (specPath.isIgnoredPath) {
                continue
            }
            val attributeValue = beanWrapper.getPropertyValue(attribute.name)
            if (attributeValue == null || "" == attributeValue) {
                continue
            }
            if (attribute.persistentAttributeType == PersistentAttributeType.EMBEDDED || isAssociation(attribute) && from !is From<*, *>) {
                setSpecPathDefaultValue(currentPath, from.get<Any>(attribute.name),
                        attribute.type as ManagedType<*>, attributeValue, probeType, currentNode)
                continue
            }
            if (isAssociation(attribute)) {
                val node = currentNode!!.add(attribute.name, attributeValue)
                if (node.spansCycle()) {
                    throw InvalidDataAccessApiUsageException(String.format(
                            "Path '%s' from root %s must not span a cyclic property reference!\r\n%s",
                            currentPath,
                            ClassUtils.getShortName(probeType), node))
                }
                setSpecPathDefaultValue(currentPath, (from as From<*, *>).join<Any, Any>(attribute.name),
                        attribute.type as ManagedType<*>, attributeValue, probeType, node)
                continue
            }
            if (!specPath.isSetValue) {
                specPath.setValue(attributeValue)
            }
        }
    }

    fun specPath(propertyName: String): SpecPath<T, M> {
        Assert.hasText(propertyName, "propertyName can not be blank.")
        return specPredicates.computeIfAbsent(propertyName
        ) { s: String -> SpecPath(typed, s) } as SpecPath<T, M>
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
        specPredicates[UUID.randomUUID().toString()] = otherMatcher
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
        specPredicates[UUID.randomUUID().toString()] = otherMatcher
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

    fun withMatcher(propertyName: String, value: Any?, matcher: PathMatcher?): M {
        return specPath(propertyName).setValue(value).withMatcher(matcher!!)
    }

    fun equal(propertyName: String, value: Any?): M {
        return withMatcher(propertyName, value, PathMatcher.EQ)
    }

    fun notEqual(propertyName: String, value: Any?): M {
        return withMatcher(propertyName, value, PathMatcher.NE)
    }

    fun <Y : Comparable<Y>?> gt(propertyName: String, value: Y): M {
        return withMatcher(propertyName, value, PathMatcher.GT)
    }

    fun <Y : Comparable<Y>?> ge(propertyName: String, value: Y): M {
        return withMatcher(propertyName, value, PathMatcher.GE)
    }

    fun <Y : Comparable<Y>?> lt(propertyName: String, value: Y): M {
        return withMatcher(propertyName, value, PathMatcher.LT)
    }

    fun <Y : Comparable<Y>?> le(propertyName: String, value: Y): M {
        return withMatcher(propertyName, value, PathMatcher.LE)
    }

    fun <Y : Comparable<Y>?> between(
            propertyName: String, first: Y,
            second: Y
    ): M {
        return withMatcher(propertyName, BetweenValue(first, second), PathMatcher.BETWEEN)
    }

    fun like(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.LIKE)
    }

    fun starting(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.STARTING)
    }

    fun ending(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.ENDING)
    }

    fun containing(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.CONTAINING)
    }

    fun notStarting(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.NOT_STARTING)
    }

    fun notEnding(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.NOT_ENDING)
    }

    fun notContaining(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.NOT_CONTAINING)
    }

    fun notLike(propertyName: String, value: String?): M {
        return withMatcher(propertyName, value, PathMatcher.NOT_LIKE)
    }

    @SafeVarargs
    fun <E> `in`(propertyName: String, vararg value: E): M {
        return withMatcher(propertyName, listOf(*value), PathMatcher.IN)
    }

    fun `in`(propertyName: String, value: Collection<*>?): M {
        return withMatcher(propertyName, value, PathMatcher.IN)
    }

    @SafeVarargs
    fun <E> notIn(propertyName: String, vararg value: E): M {
        return withMatcher(propertyName, listOf(*value), PathMatcher.NOT_IN)
    }

    fun notIn(propertyName: String, value: Collection<*>?): M {
        return withMatcher(propertyName, value, PathMatcher.NOT_IN)
    }

    companion object {
        private const val serialVersionUID = 1L
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
