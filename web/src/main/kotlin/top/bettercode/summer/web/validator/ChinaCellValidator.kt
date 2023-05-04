package top.bettercode.summer.web.validator

import top.bettercode.summer.tools.lang.util.CellUtil
import top.bettercode.summer.tools.lang.util.CellUtil.isChinaCell
import top.bettercode.summer.tools.lang.util.CellUtil.isChinaMobile
import top.bettercode.summer.tools.lang.util.CellUtil.isChinaTelecom
import top.bettercode.summer.tools.lang.util.CellUtil.isChinaUnicom
import top.bettercode.summer.tools.lang.util.CellUtil.isChinaVNO
import top.bettercode.summer.tools.lang.util.CellUtil.isSimpleCell
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * `ChinaCell` 验证器
 *
 * @author Peter wu
 */
class ChinaCellValidator : ConstraintValidator<ChinaCell, String?> {
    private var model: CellUtil.Model? = null
    override fun initialize(constraintAnnotation: ChinaCell) {
        model = constraintAnnotation.model
    }

    override fun isValid(charSequence: String?,
                         constraintValidatorContext: ConstraintValidatorContext): Boolean {
        return if (charSequence == null || charSequence.isEmpty()) {
            true
        } else when (model) {
            CellUtil.Model.ALL -> isChinaCell(charSequence)
            CellUtil.Model.MOBILE -> isChinaMobile(charSequence)
            CellUtil.Model.UNICOM -> isChinaUnicom(charSequence)
            CellUtil.Model.TELECOM -> isChinaTelecom(charSequence)
            CellUtil.Model.VNO -> isChinaVNO(charSequence)
            else -> isSimpleCell(charSequence)
        }
    }
}
