package top.bettercode.summer.web.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * 与`Pattern`相反正则表达式的内容检查注解
 *
 * @author Peter Wu
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ReversePatternValidator::class])
annotation class ReversePattern(
        /**
         * 不match 此正则表达式的字符通过检查
         *
         * @return the regular expression to no match
         */
        val regexp: String,
        /**
         * @return array of `Flag`s considered when resolving the regular expression
         */
        val flags: Array<Flag> = [],
        /**
         * @return the error message template
         */
        val message: String = "{jakarta.validation.constraints.Pattern.message}",
        /**
         * @return the groups the constraint belongs to
         */
        val groups: Array<KClass<*>> = [],
        /**
         * @return the payload associated to the constraint
         */
        val payload: Array<KClass<out Payload>> = []) {
    /**
     * Possible Regexp flags.
     */
    enum class Flag(
            /**
             * @return flag value as defined in [java.util.regex.Pattern]
             */
            //JDK flag value
            val value: Int) {
        /**
         * Enables Unix lines mode.
         *
         * @see java.util.regex.Pattern.UNIX_LINES
         */
        UNIX_LINES(Pattern.UNIX_LINES),

        /**
         * Enables case-insensitive matching.
         *
         * @see java.util.regex.Pattern.CASE_INSENSITIVE
         */
        CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),

        /**
         * Permits whitespace and comments in pattern.
         *
         * @see java.util.regex.Pattern.COMMENTS
         */
        COMMENTS(Pattern.COMMENTS),

        /**
         * Enables multiline mode.
         *
         * @see java.util.regex.Pattern.MULTILINE
         */
        MULTILINE(Pattern.MULTILINE),

        /**
         * Enables dotall mode.
         *
         * @see java.util.regex.Pattern.DOTALL
         */
        DOTALL(Pattern.DOTALL),

        /**
         * Enables Unicode-aware case folding.
         *
         * @see java.util.regex.Pattern.UNICODE_CASE
         */
        UNICODE_CASE(Pattern.UNICODE_CASE),

        /**
         * Enables canonical equivalence.
         *
         * @see java.util.regex.Pattern.CANON_EQ
         */
        CANON_EQ(Pattern.CANON_EQ)

    }

    /**
     * Defines several [ReversePattern] annotations on the same element.
     *
     * @see ReversePattern
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    annotation class List(vararg val value: ReversePattern)
}