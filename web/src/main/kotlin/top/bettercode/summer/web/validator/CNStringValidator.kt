package top.bettercode.summer.web.validator

import top.bettercode.summer.tools.lang.util.CharUtil.isCNChar
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * `ChinaCell` 验证器
 *
 * @author Peter wu
 */
class CNStringValidator : ConstraintValidator<CNString?, String?> {
    override fun initialize(constraintAnnotation: CNString?) {}
    override fun isValid(charSequence: String?,
                         constraintValidatorContext: ConstraintValidatorContext): Boolean {
        if (charSequence.isNullOrEmpty()) {
            return true
        }
        for (c in charSequence.toCharArray()) {
            if (!isCNChar(c)) {
                return false
            }
        }
        return true
    }
}
