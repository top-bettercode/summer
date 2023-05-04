package top.bettercode.summer.data.jpa.query

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.util.Assert
import org.springframework.util.StringUtils
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
        private val propertyName: String
) : SpecPredicate<T, M> {
    private var ignoreCase = false
    var isIgnoredPath = false
        private set
    private var trimspec: Trimspec? = null
    private var matcher = PathMatcher.EQ
    private var value: Any? = null

    //--------------------------------------------
    var isSetValue = false
        private set

    //--------------------------------------------
    fun toPath(root: Root<T>): Path<*>? {
        return toPath(root, propertyName)
    }

    @Suppress("UNCHECKED_CAST")
    override fun toPredicate(root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate? {
        if (isIgnoredPath) {
            return null
        }
        val path = this.toPath(root) ?: return null
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
        if (!isSetValue) {
            return null
        }
        var value = value
        if (value != null) {
            when (matcher) {
                PathMatcher.BETWEEN -> {
                    Assert.isTrue(value is BetweenValue<*>,
                            "BETWEEN matcher with wrong value")
                    val betweenValue = value as BetweenValue<*>
                    return criteriaBuilder.between<Comparable<Comparable<*>>>(path as Expression<Comparable<Comparable<*>>>, betweenValue.first as Comparable<Comparable<*>>,
                            betweenValue.second as Comparable<Comparable<*>>)
                }

                PathMatcher.GT -> return criteriaBuilder.greaterThan<Comparable<Comparable<*>>>(path as Expression<Comparable<Comparable<*>>>, value as Comparable<Comparable<*>>)
                PathMatcher.GE -> return criteriaBuilder.greaterThanOrEqualTo<Comparable<Comparable<*>>>(path as Expression<Comparable<Comparable<*>>>, value as Comparable<Comparable<*>>)
                PathMatcher.LT -> return criteriaBuilder.lessThan<Comparable<Comparable<*>>>(path as Expression<Comparable<Comparable<*>>>, value as Comparable<Comparable<*>>)
                PathMatcher.LE -> return criteriaBuilder.lessThanOrEqualTo<Comparable<Comparable<*>>>(path as Expression<Comparable<Comparable<*>>>, value as Comparable<Comparable<*>>)
                else -> {}
            }
        }
        if (pathJavaType == String::class.java) {
            var stringExpression: Expression<String> = path as Expression<String>
            val ignoreCase = ignoreCase
            if (ignoreCase) {
                stringExpression = criteriaBuilder.lower(stringExpression)
                if (value is String) {
                    value = value.toString().lowercase(Locale.getDefault())
                }
            }
            if (trimspec != null && value is String) {
                when (trimspec) {
                    Trimspec.LEADING -> value = StringUtils.trimLeadingWhitespace(value)
                    Trimspec.TRAILING -> value = StringUtils.trimTrailingWhitespace(value)
                    Trimspec.BOTH -> value = StringUtils.trimWhitespace(value)
                    else -> {}
                }
            }
            when (matcher) {
                PathMatcher.EQ -> return criteriaBuilder.equal(stringExpression, value)
                PathMatcher.NE -> return criteriaBuilder.notEqual(stringExpression, value)
                PathMatcher.IN -> {
                    Assert.isTrue(value is Collection<*>, "IN matcher with wrong value")
                    val collect = (value as Collection<*>).stream()
                            .map { s: Any? -> if (ignoreCase) s.toString().lowercase(Locale.getDefault()) else s.toString() }
                            .collect(Collectors.toList())
                    return stringExpression.`in`(collect)
                }

                PathMatcher.NOT_IN -> {
                    Assert.isTrue(value is Collection<*>, "IN matcher with wrong value")
                    val notInCollect = (value as Collection<*>).stream()
                            .map { s: Any? -> if (ignoreCase) s.toString().lowercase(Locale.getDefault()) else s.toString() }
                            .collect(Collectors.toList())
                    return criteriaBuilder.not(stringExpression.`in`(notInCollect))
                }

                else -> {}
            }
            if (value != null) {
                when (matcher) {
                    PathMatcher.LIKE -> return criteriaBuilder.like(stringExpression, value as String?)
                    PathMatcher.NOT_LIKE -> return criteriaBuilder.notLike(stringExpression, value as String?)
                    PathMatcher.STARTING -> return criteriaBuilder.like(stringExpression, starting(value))
                    PathMatcher.NOT_STARTING -> return criteriaBuilder.notLike(stringExpression, starting(value))
                    PathMatcher.ENDING -> return criteriaBuilder.like(stringExpression, ending(value))
                    PathMatcher.NOT_ENDING -> return criteriaBuilder.notLike(stringExpression, ending(value))
                    PathMatcher.CONTAINING -> return criteriaBuilder.like(stringExpression, containing(value))
                    PathMatcher.NOT_CONTAINING -> return criteriaBuilder.notLike(stringExpression, containing(value))
                    else -> {}
                }
            }
        } else {
            when (matcher) {
                PathMatcher.EQ -> return criteriaBuilder.equal(path, value)
                PathMatcher.NE -> return criteriaBuilder.notEqual(path, value)
                PathMatcher.IN -> {
                    Assert.isTrue(value is Collection<*>, "IN matcher with wrong value")
                    val collect = value as Collection<*>?
                    return path.`in`(collect)
                }

                PathMatcher.NOT_IN -> {
                    Assert.isTrue(value is Collection<*>, "IN matcher with wrong value")
                    val notInCollect = value as Collection<*>?
                    return criteriaBuilder.not(path.`in`(notInCollect))
                }

                else -> {}
            }
        }
        return null
    }

    fun setValue(value: Any?): SpecPath<T, M> {
        this.value = value
        isSetValue = true
        return this
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

    fun ignoredPath(): M {
        isIgnoredPath = true
        return specMatcher
    }

    fun withMatcher(matcher: PathMatcher): M {
        this.matcher = matcher
        return specMatcher
    }

    fun withMatcher(value: Any?, matcher: PathMatcher): M {
        this.setValue(value)
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
    fun equal(value: Any?): M {
        return withMatcher(value, PathMatcher.EQ)
    }

    fun notEqual(value: Any?): M {
        return withMatcher(value, PathMatcher.NE)
    }

    fun <Y : Comparable<Y>?> gt(value: Y): M {
        return withMatcher(value, PathMatcher.GT)
    }

    fun <Y : Comparable<Y>?> ge(value: Y): M {
        return withMatcher(value, PathMatcher.GE)
    }

    fun <Y : Comparable<Y>?> lt(value: Y): M {
        return withMatcher(value, PathMatcher.LT)
    }

    fun <Y : Comparable<Y>?> le(value: Y): M {
        return withMatcher(value, PathMatcher.LE)
    }

    fun <Y : Comparable<Y>> between(first: Y, second: Y): M {
        return withMatcher(BetweenValue(first, second), PathMatcher.BETWEEN)
    }

    fun like(value: String?): M {
        return withMatcher(value, PathMatcher.LIKE)
    }

    fun starting(value: String?): M {
        return withMatcher(value, PathMatcher.STARTING)
    }

    fun ending(value: String?): M {
        return withMatcher(value, PathMatcher.ENDING)
    }

    fun containing(value: String?): M {
        return withMatcher(value, PathMatcher.CONTAINING)
    }

    fun notStarting(value: String?): M {
        return withMatcher(value, PathMatcher.NOT_STARTING)
    }

    fun notEnding(value: String?): M {
        return withMatcher(value, PathMatcher.NOT_ENDING)
    }

    fun notContaining(value: String?): M {
        return withMatcher(value, PathMatcher.NOT_CONTAINING)
    }

    fun notLike(value: String?): M {
        return withMatcher(value, PathMatcher.NOT_LIKE)
    }

    @SafeVarargs
    fun <E> `in`(vararg value: E): M {
        return withMatcher(listOf(*value), PathMatcher.IN)
    }

    fun `in`(value: Collection<*>?): M {
        return withMatcher(value, PathMatcher.IN)
    }

    @SafeVarargs
    fun <E> notIn(vararg value: E): M {
        return withMatcher(listOf(*value), PathMatcher.NOT_IN)
    }

    fun notIn(value: Collection<*>?): M {
        return withMatcher(value, PathMatcher.NOT_IN)
    }

    internal class BetweenValue<Y : Comparable<Y>?>(val first: Y, val second: Y)
    companion object {
        private val log = LoggerFactory.getLogger(SpecPath::class.java)

        //--------------------------------------------
        fun containing(value: Any): String {
            return "%$value%"
        }

        fun ending(value: Any): String {
            return "%$value"
        }

        fun starting(value: Any): String {
            return "$value%"
        }

        fun <T> toPath(root: Root<T>, propertyName: String): Path<*>? {
            return try {
                val split = propertyName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var path: Path<*>? = null
                for (s in split) {
                    path = if (path == null) root.get<Any>(s) else path.get<Any>(s)
                }
                path
            } catch (e: IllegalArgumentException) {
                if (log.isDebugEnabled) {
                    log.debug(e.message)
                }
                null
            }
        }
    }
}
