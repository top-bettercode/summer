package top.bettercode.summer.data.jpa.query

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.util.ValueEnum
import java.util.*
import java.util.stream.Collectors
import javax.persistence.criteria.*
import javax.persistence.criteria.CriteriaBuilder.Trimspec

/**
 * @author Peter Wu
 */
class SpecPath<T : Any?, M : SpecMatcher<T, M>>(
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
    var criteria: Any? = ValueEnum.NOT_SET
        private set

    /**
     * 更新值
     */
    var criteriaUpdate: Any? = ValueEnum.NOT_SET
        private set

    //--------------------------------------------
    val isSetCriteria: Boolean
        get() = criteria != ValueEnum.NOT_SET

    val isSetCriteriaUpdate: Boolean
        get() = criteriaUpdate != ValueEnum.NOT_SET

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
                PathMatcher.IS_NULL_OR_EMPTY -> return criteriaBuilder.or(criteriaBuilder.isNull(path),
                        criteriaBuilder.equal(path, ""))

                PathMatcher.IS_NOT_NULL_OR_EMPTY -> return criteriaBuilder.and(criteriaBuilder.isNotNull(path),
                        criteriaBuilder.notEqual(path, ""))

                else -> {}
            }
        }
        if (!isSetCriteria) {
            return null
        }
        var criteria = this.criteria
        if (criteria != null) {
            when (matcher) {
                PathMatcher.BETWEEN -> {
                    Assert.isTrue(criteria is BetweenValue<*>,
                            "BETWEEN matcher with wrong criteria")
                    val betweenValue = criteria as BetweenValue<*>
                    return criteriaBuilder.between(path as Expression<Comparable<Comparable<*>>>, betweenValue.first as Comparable<Comparable<*>>,
                            betweenValue.second as Comparable<Comparable<*>>)
                }

                PathMatcher.GT -> return criteriaBuilder.greaterThan(path as Expression<Comparable<Comparable<*>>>, criteria as Comparable<Comparable<*>>)
                PathMatcher.GE -> return criteriaBuilder.greaterThanOrEqualTo(path as Expression<Comparable<Comparable<*>>>, criteria as Comparable<Comparable<*>>)
                PathMatcher.LT -> return criteriaBuilder.lessThan(path as Expression<Comparable<Comparable<*>>>, criteria as Comparable<Comparable<*>>)
                PathMatcher.LE -> return criteriaBuilder.lessThanOrEqualTo(path as Expression<Comparable<Comparable<*>>>, criteria as Comparable<Comparable<*>>)
                else -> {}
            }
        }
        if (pathJavaType == String::class.java) {
            var stringExpression: Expression<String> = path as Expression<String>
            val ignoreCase = ignoreCase
            if (ignoreCase) {
                stringExpression = criteriaBuilder.lower(stringExpression)
                if (criteria is String) {
                    criteria = criteria.toString().lowercase(Locale.getDefault())
                }
            }
            if (trimspec != null && criteria is String) {
                when (trimspec) {
                    Trimspec.LEADING -> criteria = criteria.trimStart()
                    Trimspec.TRAILING -> criteria = criteria.trimEnd()
                    Trimspec.BOTH -> criteria = criteria.trim()
                    else -> {}
                }
            }
            when (matcher) {
                PathMatcher.EQ -> return criteriaBuilder.equal(stringExpression, criteria)
                PathMatcher.NE -> return criteriaBuilder.notEqual(stringExpression, criteria)
                PathMatcher.IN -> {
                    Assert.isTrue(criteria is Collection<*>, "IN matcher with wrong criteria")
                    val collect = (criteria as Collection<*>).stream()
                            .map { s: Any? -> if (ignoreCase) s.toString().lowercase(Locale.getDefault()) else s.toString() }
                            .collect(Collectors.toList())
                    return stringExpression.`in`(collect)
                }

                PathMatcher.NOT_IN -> {
                    Assert.isTrue(criteria is Collection<*>, "NOT IN matcher with wrong criteria")
                    val notInCollect = (criteria as Collection<*>).stream()
                            .map { s: Any? -> if (ignoreCase) s.toString().lowercase(Locale.getDefault()) else s.toString() }
                            .collect(Collectors.toList())
                    return criteriaBuilder.not(stringExpression.`in`(notInCollect))
                }

                else -> {}
            }
            if (criteria != null) {
                when (matcher) {
                    PathMatcher.LIKE -> return criteriaBuilder.like(stringExpression, criteria as String?)
                    PathMatcher.NOT_LIKE -> return criteriaBuilder.notLike(stringExpression, criteria as String?)
                    PathMatcher.STARTING -> return criteriaBuilder.like(stringExpression, starting(criteria))
                    PathMatcher.NOT_STARTING -> return criteriaBuilder.notLike(stringExpression, starting(criteria))
                    PathMatcher.ENDING -> return criteriaBuilder.like(stringExpression, ending(criteria))
                    PathMatcher.NOT_ENDING -> return criteriaBuilder.notLike(stringExpression, ending(criteria))
                    PathMatcher.CONTAINING -> return criteriaBuilder.like(stringExpression, containing(criteria))
                    PathMatcher.NOT_CONTAINING -> return criteriaBuilder.notLike(stringExpression, containing(criteria))
                    else -> {}
                }
            }
        } else {
            when (matcher) {
                PathMatcher.EQ -> return criteriaBuilder.equal(path, criteria)
                PathMatcher.NE -> return criteriaBuilder.notEqual(path, criteria)
                PathMatcher.IN -> {
                    Assert.isTrue(criteria is Collection<*>, "IN matcher with wrong criteria")
                    val collect = criteria as Collection<*>?
                    return path.`in`(collect)
                }

                PathMatcher.NOT_IN -> {
                    Assert.isTrue(criteria is Collection<*>, "IN matcher with wrong criteria")
                    val notInCollect = criteria as Collection<*>?
                    return criteriaBuilder.not(path.`in`(notInCollect))
                }

                else -> {}
            }
        }
        return null
    }

    fun criteria(criteria: Any?): SpecPath<T, M> {
        this.criteria = criteria
        return this
    }

    fun criteriaUpdate(criteriaUpdate: Any?): M {
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
    fun trim(trimspec: Trimspec? = Trimspec.BOTH): M {
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

    fun withMatcher(criteria: Any?, matcher: PathMatcher): M {
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
        return this.withMatcher(PathMatcher.EQ)
    }

    fun notEqual(): M {
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
    fun equal(criteria: Any?): M {
        return withMatcher(criteria, PathMatcher.EQ)
    }

    fun notEqual(criteria: Any?): M {
        return withMatcher(criteria, PathMatcher.NE)
    }

    fun <Y : Comparable<Y>?> gt(criteria: Y): M {
        return withMatcher(criteria, PathMatcher.GT)
    }

    fun <Y : Comparable<Y>?> ge(criteria: Y): M {
        return withMatcher(criteria, PathMatcher.GE)
    }

    fun <Y : Comparable<Y>?> lt(criteria: Y): M {
        return withMatcher(criteria, PathMatcher.LT)
    }

    fun <Y : Comparable<Y>?> le(criteria: Y): M {
        return withMatcher(criteria, PathMatcher.LE)
    }

    fun <Y : Comparable<Y>> between(first: Y, second: Y): M {
        return withMatcher(BetweenValue(first, second), PathMatcher.BETWEEN)
    }

    fun like(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.LIKE)
    }

    fun starting(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.STARTING)
    }

    fun ending(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.ENDING)
    }

    fun containing(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.CONTAINING)
    }

    fun notStarting(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.NOT_STARTING)
    }

    fun notEnding(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.NOT_ENDING)
    }

    fun notContaining(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.NOT_CONTAINING)
    }

    fun notLike(criteria: String?): M {
        return withMatcher(criteria, PathMatcher.NOT_LIKE)
    }

    @SafeVarargs
    fun <E> `in`(vararg criteria: E): M {
        return withMatcher(listOf(*criteria), PathMatcher.IN)
    }

    fun `in`(criteria: Collection<*>?): M {
        return withMatcher(criteria, PathMatcher.IN)
    }

    @SafeVarargs
    fun <E> notIn(vararg criteria: E): M {
        return withMatcher(listOf(*criteria), PathMatcher.NOT_IN)
    }

    fun notIn(criteria: Collection<*>?): M {
        return withMatcher(criteria, PathMatcher.NOT_IN)
    }

    internal class BetweenValue<Y : Comparable<Y>?>(val first: Y, val second: Y)
    companion object {
        private val log = LoggerFactory.getLogger(SpecPath::class.java)

        //--------------------------------------------
        fun containing(criteria: Any): String {
            return "%$criteria%"
        }

        fun ending(criteria: Any): String {
            return "%$criteria"
        }

        fun starting(criteria: Any): String {
            return "$criteria%"
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
