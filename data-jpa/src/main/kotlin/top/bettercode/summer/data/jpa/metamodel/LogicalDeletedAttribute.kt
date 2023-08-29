package top.bettercode.summer.data.jpa.metamodel

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.data.jpa.support.JpaUtil
import javax.persistence.criteria.*
import javax.persistence.metamodel.SingularAttribute

/**
 *
 * @author Peter Wu
 */
class LogicalDeletedAttribute<X, T>(singularAttribute: SingularAttribute<X, T>, annotation: LogicalDelete, jpaExtProperties: JpaExtProperties) : SingularAttributeExt<X, T>(singularAttribute, isLogicalDeleted = true) {

    /**
     * 设置逻辑删除值
     */
    fun setLogicalDeleted(criteriaUpdate: CriteriaUpdate<X>, value: Boolean) {
        criteriaUpdate.set(this, if (value) trueValue else falseValue)
    }

    /**
     * 默认逻辑删除值.
     */
    val trueValue: T by lazy {
        JpaUtil.convert(annotation.trueValue.ifBlank {
            jpaExtProperties.logicalDelete.trueValue
        }, this.javaType)!!
    }

    /**
     * 默认逻辑未删除值.
     */
    val falseValue: T by lazy {
        JpaUtil.convert(annotation.falseValue.ifBlank {
            jpaExtProperties.logicalDelete.falseValue
        }, this.javaType)!!
    }

    val notDeletedSpecification by lazy {
        Specification { root: Root<X>, _: CriteriaQuery<*>?, builder: CriteriaBuilder -> builder.equal(root.get(this), falseValue) }
    }

    val deletedSpecification by lazy {
        Specification { root: Root<X>, _: CriteriaQuery<*>?, builder: CriteriaBuilder ->
            builder.equal(root.get(this), trueValue)
        }
    }

    fun andNotDeleted(spec: Specification<X>?): Specification<X> {
        return if (spec == null) notDeletedSpecification else spec.and(notDeletedSpecification)
    }

    fun andDeleted(spec: Specification<X>?): Specification<X> {
        return if (spec == null) deletedSpecification else spec.and(deletedSpecification)
    }

    fun andNotDeleted(predicate: Predicate?, builder: CriteriaBuilder, root: Root<*>): Predicate {
        var predicate1: Predicate? = predicate
        val predicate2 = builder.equal(root.get<Any>(this.name), this.falseValue)
        predicate1 = if (predicate1 == null) {
            predicate2
        } else
            builder.and(predicate1, predicate2)
        return predicate1
    }

    /**
     * 删除
     */
    fun delete(entity: Any) {
        DirectFieldAccessFallbackBeanWrapper(entity).setPropertyValue(this.name, trueValue)
    }

    /**
     * 恢复
     */
    fun restore(entity: Any) {
        DirectFieldAccessFallbackBeanWrapper(entity).setPropertyValue(this.name, falseValue)
    }

    /**
     * 如果未设置值设置未删除
     */
    fun setFalseIf(entity: Any) {
        val beanWrapper = DirectFieldAccessFallbackBeanWrapper(entity)
        if (beanWrapper.getPropertyValue(this.name) == null) {
            beanWrapper.setPropertyValue(this.name, falseValue)
        }
    }

}