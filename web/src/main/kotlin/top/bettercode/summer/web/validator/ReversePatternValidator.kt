package top.bettercode.summer.web.validator

import org.hibernate.validator.internal.util.logging.LoggerFactory
import java.lang.invoke.MethodHandles
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * `ReversePattern` 的验证器
 *
 * @author Peter Wu
 */
class ReversePatternValidator : ConstraintValidator<ReversePattern, CharSequence?> {
    private var pattern: Pattern? = null
    override fun initialize(parameters: ReversePattern) {
        val flags = parameters.flags
        var intFlag = 0
        for (flag in flags) {
            intFlag = intFlag or flag.value
        }
        pattern = try {
            Pattern.compile(parameters.regexp, intFlag)
        } catch (e: PatternSyntaxException) {
            throw LOG.getInvalidRegularExpressionException(e)
        }
    }

    override fun isValid(value: CharSequence?,
                         constraintValidatorContext: ConstraintValidatorContext): Boolean {
        if (value == null || value.length == 0) {
            return true
        }
        val m = pattern!!.matcher(value)
        return !m.matches()
    }

    companion object {
        private val LOG = LoggerFactory.make(MethodHandles.lookup())
    }
}
