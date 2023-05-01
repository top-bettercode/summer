package top.bettercode.summer.web.validator

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class IDCardValidator : ConstraintValidator<IDCard?, String?> {
    override fun initialize(annotation: IDCard?) {}
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value == null || value.length == 0 || IDCardUtil.validate(value)
    }

    companion object {
        // 每位加权因子
        private val power = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
                8, 4, 2)

        // 第18位校检码
        private val verifyCode = arrayOf("1", "0", "X", "9", "8", "7", "6",
                "5", "4", "3", "2")
    }
}