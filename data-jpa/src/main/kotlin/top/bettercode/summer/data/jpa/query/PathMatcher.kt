package top.bettercode.summer.data.jpa.query

/**
 * @author Peter Wu
 */
enum class PathMatcher {
    //turn Expression<Boolean> into a
    //useful for use with varargs methods
    /**
     * Create a predicate testing for a true value.
     */
    IS_TRUE,

    /**
     * Create a predicate testing for a false value.
     */
    IS_FALSE,
    //null tests:
    /**
     * Create a predicate to test whether the expression is null.
     */
    IS_NULL,

    /**
     * Create a predicate to test whether the expression is not null.
     */
    IS_NOT_NULL,
    IS_EMPTY,
    IS_NOT_EMPTY,
    IS_NULL_OR_EMPTY,
    IS_NOT_NULL_OR_EMPTY,
    //equality:
    /**
     * Create a predicate for testing the arguments for equality.
     */
    EQ,

    /**
     * Create a predicate for testing the arguments for inequality.
     */
    NE,

    /**
     * Create a predicate for testing whether the first argument is between the second and third
     * arguments in value.
     */
    BETWEEN,
    //comparisons for numeric operands:
    /**
     * Create a predicate for testing whether the first argument is greater than the second.
     */
    GT,

    /**
     * Create a predicate for testing whether the first argument is greater than or equal to the
     * second.
     */
    GE,

    /**
     * Create a predicate for testing whether the first argument is less than the second.
     */
    LT,

    /**
     * Create a predicate for testing whether the first argument is less than or equal to the second.
     */
    LE,
    //string functions:
    /**
     * Create a predicate for testing whether the expression satisfies the given pattern.
     */
    LIKE,

    /**
     * Create a predicate for testing whether the expression does not satisfy the given pattern.
     */
    NOT_LIKE,

    /**
     * Matches string starting with pattern
     */
    STARTING,

    /**
     * Matches string ending with pattern
     */
    ENDING,

    /**
     * Matches string containing pattern
     */
    CONTAINING,

    /**
     * Not Matches string starting with pattern
     */
    NOT_STARTING,

    /**
     * Not Matches string ending with pattern
     */
    NOT_ENDING,

    /**
     * Not Matches string containing pattern
     */
    NOT_CONTAINING,
    //in builders:
    /**
     * Create predicate to test whether given expression is contained in a list of values.
     */
    IN,

    /**
     * Create predicate to test whether given expression is not contained in a list of values.
     */
    NOT_IN
}
