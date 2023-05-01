package top.bettercode.summer.web.validator

import top.bettercode.summer.tools.lang.util.CellUtil
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import kotlin.reflect.KClass

/**
 * 中国内地手机号验证
 *
 * @author Peter wu
 */
@MustBeDocumented
@Constraint(validatedBy = [ChinaCellValidator::class])
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@ReportAsSingleViolation
annotation class ChinaCell(
        /**
         * @return 检查方式
         */
        val model: CellUtil.Model = CellUtil.Model.SIMPLE, val message: String = "{chinaCell.notValid}", val groups: Array<KClass<*>> = [], val payload: Array<KClass<out Payload>> = []) {
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    annotation class List(vararg val value: ChinaCell)
}
