package top.bettercode.summer.data.jpa.query

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.util.Assert
import java.util.*
import javax.persistence.criteria.*
import javax.persistence.criteria.CriteriaBuilder.Trimspec

/**
 * @author Peter Wu
 */
class SpecPath<P, T : Any?, M : SpecMatcher<T, M>>(
    private val specMatcher: M,
    /**
     * name of the attribute
     */
    val propertyName: String
) : SpecPredicate<T, M> {
    /**
     * 忽略大小写
     */
    private var ignoreCase = false

    /**
     * 不参与计算查询条件
     */
    var isIgnored = false
        private set

    /**
     * 用于指定如何修剪字符串。
     */
    private var trimspec: Trimspec? = null

    /**
     * 条件匹配方式
     */
    private var matcher = PathMatcher.EQ

    /**
     * 条件查询值
     */
    var criteria: P? = null
        private set

    /**
     * 条件查询值2,用于范围查询
     */
    var criteria2: P? = null
        private set

    /**
     * 集合条件查询值,用于范围查询
     */
    var criterias: Collection<P>? = null
        private set

    /**
     * 更新值
     */
    var criteriaUpdate: P? = null
        private set

    //--------------------------------------------
    var isSetCriteria: Boolean = false

    var isSetCriteriaUpdate: Boolean = false

    //--------------------------------------------
    fun toPath(root: Root<T>): Path<*> {
        return toPath(root, propertyName)
    }

    @Suppress("UNCHECKED_CAST")
    override fun toPredicate(root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate? {
        if (isIgnored) {
            return null
        }
        val path = this.toPath(root)
        val matcher = matcher
        val pathJavaType = path.javaType
        when (matcher) {
            PathMatcher.IS_TRUE -> return criteriaBuilder.isTrue(path as Expression<Boolean>)
            PathMatcher.IS_FALSE -> return criteriaBuilder.isFalse(path as Expression<Boolean>)
            PathMatcher.IS_NULL -> return criteriaBuilder.isNull(path)
            PathMatcher.IS_NOT_NULL -> return criteriaBuilder.isNotNull(path)
            else -> {}
        }
        if (pathJavaType == String::class.java) {
            when (matcher) {
                PathMatcher.IS_EMPTY -> return criteriaBuilder.equal(path, "")
                PathMatcher.IS_NOT_EMPTY -> return criteriaBuilder.notEqual(path, "")
                PathMatcher.IS_NULL_OR_EMPTY -> return criteriaBuilder.or(
                    criteriaBuilder.isNull(path),
                    criteriaBuilder.equal(path, "")
                )

                PathMatcher.IS_NOT_NULL_OR_EMPTY -> return criteriaBuilder.and(
                    criteriaBuilder.isNotNull(path),
                    criteriaBuilder.notEqual(path, "")
                )

                else -> {}
            }
        }
        if (!isSetCriteria) {
            return null
        }
        if (criteria != null) {
            when (matcher) {
                PathMatcher.BETWEEN -> {
                    return criteriaBuilder.between(
                        path as Expression<Comparable<Comparable<*>>>,
                        criteria as Comparable<Comparable<*>>,
                        criteria2 as Comparable<Comparable<*>>
                    )
                }

                PathMatcher.GT -> return criteriaBuilder.greaterThan(
                    path as Expression<Comparable<Comparable<*>>>,
                    criteria as Comparable<Comparable<*>>
                )

                PathMatcher.GE -> return criteriaBuilder.greaterThanOrEqualTo(
                    path as Expression<Comparable<Comparable<*>>>,
                    criteria as Comparable<Comparable<*>>
                )

                PathMatcher.LT -> return criteriaBuilder.lessThan(
                    path as Expression<Comparable<Comparable<*>>>,
                    criteria as Comparable<Comparable<*>>
                )

                PathMatcher.LE -> return criteriaBuilder.lessThanOrEqualTo(
                    path as Expression<Comparable<Comparable<*>>>,
                    criteria as Comparable<Comparable<*>>
                )

                else -> {}
            }
        }
        if (pathJavaType == String::class.java) {
            var criteria = this.criteria
            var stringExpression: Expression<String> = path as Expression<String>
            if (criteria != null) {
                val ignoreCase = ignoreCase
                if (ignoreCase) {
                    stringExpression = criteriaBuilder.lower(stringExpression)
                    if (criteria is String) {
                        criteria = criteria.lowercase(Locale.getDefault()) as P
                    }
                }
                if (trimspec != null && criteria is String) {
                    when (trimspec) {
                        Trimspec.LEADING -> criteria = criteria.trimStart() as P
                        Trimspec.TRAILING -> criteria = criteria.trimEnd() as P
                        Trimspec.BOTH -> criteria = criteria.trim() as P
                        else -> {}
                    }
                }
                criteria as Any

                when (matcher) {
                    PathMatcher.LIKE -> return criteriaBuilder.like(
                        stringExpression,
                        criteria as String
                    )

                    PathMatcher.NOT_LIKE -> return criteriaBuilder.notLike(
                        stringExpression,
                        criteria as String
                    )

                    PathMatcher.STARTING -> return criteriaBuilder.like(
                        stringExpression,
                        criteria.starting()
                    )

                    PathMatcher.NOT_STARTING -> return criteriaBuilder.notLike(
                        stringExpression,
                        criteria.starting()
                    )

                    PathMatcher.ENDING -> return criteriaBuilder.like(
                        stringExpression,
                        criteria.ending()
                    )

                    PathMatcher.NOT_ENDING -> return criteriaBuilder.notLike(
                        stringExpression,
                        criteria.ending()
                    )

                    PathMatcher.CONTAINING -> return criteriaBuilder.like(
                        stringExpression,
                        criteria.containing()
                    )

                    PathMatcher.NOT_CONTAINING -> return criteriaBuilder.notLike(
                        stringExpression,
                        criteria.containing()
                    )

                    else -> {}
                }
            }
            when (matcher) {
                PathMatcher.EQ -> return criteriaBuilder.equal(stringExpression, criteria)
                PathMatcher.NE -> return criteriaBuilder.notEqual(stringExpression, criteria)
                PathMatcher.IN -> {
                    Assert.notNull(criterias, "IN matcher with wrong criteria")
                    val collect = criterias!!
                        .map { s: P ->
                            if (ignoreCase) s.toString()
                                .lowercase(Locale.getDefault()) else s.toString()
                        }
                    return stringExpression.`in`(collect)
                }

                PathMatcher.NOT_IN -> {
                    Assert.notNull(criterias, "IN matcher with wrong criteria")
                    val notInCollect = criterias!!
                        .map { s: P ->
                            if (ignoreCase) s.toString()
                                .lowercase(Locale.getDefault()) else s.toString()
                        }
                    return criteriaBuilder.not(stringExpression.`in`(notInCollect))
                }

                else -> {}
            }
        } else {
            when (matcher) {
                PathMatcher.EQ -> return criteriaBuilder.equal(path, criteria)
                PathMatcher.NE -> return criteriaBuilder.notEqual(path, criteria)
                PathMatcher.IN -> {
                    Assert.notNull(criterias, "IN matcher with wrong criteria")
                    return path.`in`(criterias)
                }

                PathMatcher.NOT_IN -> {
                    Assert.notNull(criterias, "IN matcher with wrong criteria")
                    return criteriaBuilder.not(path.`in`(criterias))
                }

                else -> {}
            }
        }
        return null
    }

    fun criteria(criteria: P?): SpecPath<P, T, M> {
        this.isSetCriteria = true
        this.criteria = criteria
        return this
    }

    fun criteriaUpdate(criteriaUpdate: P?): M {
        this.isSetCriteriaUpdate = true
        this.criteriaUpdate = criteriaUpdate
        return specMatcher
    }

    fun sortBy(direction: Sort.Direction): M {
        specMatcher.sortBy(direction, propertyName)
        return specMatcher
    }

    fun asc(): M {
        return this.sortBy(Sort.Direction.ASC)
    }

    fun desc(): M {
        return this.sortBy(Sort.Direction.DESC)
    }

    fun ignoreCase(): M {
        ignoreCase = true
        return specMatcher
    }

    @JvmOverloads
    fun trim(trimspec: Trimspec = Trimspec.BOTH): M {
        this.trimspec = trimspec
        return specMatcher
    }

    fun ignored(): M {
        isIgnored = true
        return specMatcher
    }

    fun withMatcher(matcher: PathMatcher): M {
        this.matcher = matcher
        return specMatcher
    }

    fun withMatcher(criteria: P?, matcher: PathMatcher): M {
        this.criteria(criteria)
        return this.withMatcher(matcher)
    }

    val isTrue: M
        get() = this.withMatcher(PathMatcher.IS_TRUE)
    val isFalse: M
        get() = this.withMatcher(PathMatcher.IS_FALSE)
    val isNull: M
        get() = this.withMatcher(PathMatcher.IS_NULL)
    val isNotNull: M
        get() = this.withMatcher(PathMatcher.IS_NOT_NULL)
    val isEmpty: M
        get() = this.withMatcher(PathMatcher.IS_EMPTY)
    val isNotEmpty: M
        get() = this.withMatcher(PathMatcher.IS_NOT_EMPTY)
    val isNullOrEmpty: M
        get() = this.withMatcher(PathMatcher.IS_NULL_OR_EMPTY)
    val isNotNullOrEmpty: M
        get() = this.withMatcher(PathMatcher.IS_NOT_NULL_OR_EMPTY)

    fun equal(): M {
        return eq()
    }

    fun eq(): M {
        return this.withMatcher(PathMatcher.EQ)
    }

    fun notEqual(): M {
        return ne()
    }

    fun ne(): M {
        return this.withMatcher(PathMatcher.NE)
    }

    fun gt(): M {
        return this.withMatcher(PathMatcher.GT)
    }

    fun ge(): M {
        return this.withMatcher(PathMatcher.GE)
    }

    fun lt(): M {
        return this.withMatcher(PathMatcher.LT)
    }

    fun le(): M {
        return this.withMatcher(PathMatcher.LE)
    }

    fun like(): M {
        return this.withMatcher(PathMatcher.LIKE)
    }

    fun starting(): M {
        return this.withMatcher(PathMatcher.STARTING)
    }

    fun ending(): M {
        return this.withMatcher(PathMatcher.ENDING)
    }

    fun containing(): M {
        return this.withMatcher(PathMatcher.CONTAINING)
    }

    fun notStarting(): M {
        return this.withMatcher(PathMatcher.NOT_STARTING)
    }

    fun notEnding(): M {
        return this.withMatcher(PathMatcher.NOT_ENDING)
    }

    fun notContaining(): M {
        return this.withMatcher(PathMatcher.NOT_CONTAINING)
    }

    fun notLike(): M {
        return this.withMatcher(PathMatcher.NOT_LIKE)
    }

    //--------------------------------------------

    fun equal(criteria: P?): M {
        return eq(criteria)
    }

    fun eq(criteria: P?): M {
        return withMatcher(criteria, PathMatcher.EQ)
    }

    fun notEqual(criteria: P?): M {
        return ne(criteria)
    }

    fun ne(criteria: P?): M {
        return withMatcher(criteria, PathMatcher.NE)
    }

    fun gt(criteria: P): M {
        return withMatcher(criteria, PathMatcher.GT)
    }

    fun ge(criteria: P): M {
        return withMatcher(criteria, PathMatcher.GE)
    }

    fun lt(criteria: P): M {
        return withMatcher(criteria, PathMatcher.LT)
    }

    fun le(criteria: P): M {
        return withMatcher(criteria, PathMatcher.LE)
    }

    fun between(first: P, second: P): M {
        this.criteria2 = second
        return withMatcher(first, PathMatcher.BETWEEN)
    }

    @Suppress("UNCHECKED_CAST")
    fun like(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.LIKE)
    }

    @Suppress("UNCHECKED_CAST")
    fun starting(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.STARTING)
    }

    @Suppress("UNCHECKED_CAST")
    fun ending(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.ENDING)
    }

    @Suppress("UNCHECKED_CAST")
    fun containing(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.CONTAINING)
    }

    @Suppress("UNCHECKED_CAST")
    fun notStarting(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.NOT_STARTING)
    }

    @Suppress("UNCHECKED_CAST")
    fun notEnding(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.NOT_ENDING)
    }

    @Suppress("UNCHECKED_CAST")
    fun notContaining(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.NOT_CONTAINING)
    }

    @Suppress("UNCHECKED_CAST")
    fun notLike(criteria: String?): M {
        return withMatcher(criteria as P?, PathMatcher.NOT_LIKE)
    }

    fun `in`(vararg criteria: P): M {
        this.isSetCriteria = true
        this.criterias = criteria.toList()
        return this.withMatcher(PathMatcher.IN)
    }

    fun `in`(criterias: Collection<P>): M {
        this.isSetCriteria = true
        this.criterias = criterias
        return this.withMatcher(PathMatcher.IN)
    }

    fun notIn(vararg criteria: P): M {
        this.isSetCriteria = true
        this.criterias = criteria.toList()
        return this.withMatcher(PathMatcher.NOT_IN)
    }

    fun notIn(criterias: Collection<P>): M {
        this.isSetCriteria = true
        this.criterias = criterias
        return this.withMatcher(PathMatcher.NOT_IN)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SpecPath::class.java)

        //--------------------------------------------
        fun Any.containing(): String {
            return "%$this%"
        }

        fun Any.ending(): String {
            return "%$this"
        }

        fun Any.starting(): String {
            return "$this%"
        }

        fun <T> toPath(root: Root<T>, propertyName: String): Path<*> {
            val split = propertyName.split(".")
            var path: Path<*>? = null
            for (s in split) {
                path = if (path == null) root.get<Any>(s) else path.get<Any>(s)
            }
            return path ?: throw IllegalArgumentException("Path not found: $propertyName")
        }
    }
}
