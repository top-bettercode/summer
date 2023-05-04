package top.bettercode.summer.data.jpa.support

import org.springframework.beans.BeanUtils
import org.springframework.data.annotation.LastModifiedDate
import top.bettercode.summer.data.jpa.SoftDelete
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.time.temporal.TemporalAccessor
import java.util.*
import javax.persistence.MappedSuperclass
import javax.persistence.Version

/**
 * @author Peter Wu
 */
open class DefaultExtJpaSupport(jpaExtProperties: JpaExtProperties, domainClass: Class<*>) : ExtJpaSupport {
    /**
     * 是否支持逻辑删除
     */
    private var supportSoftDeleted = false
    final override var softDeletedPropertyType: Class<*>? = null
    private var lastModifiedDatePropertyType: Class<*>? = null
    private var versionPropertyType: Class<*>? = null

    /**
     * 逻辑删除字段属性名.
     */
    final override var softDeletedPropertyName: String? = null
    final override var lastModifiedDatePropertyName: String? = null
    final override var versionPropertyName: String? = null

    /**
     * 默认逻辑删除值.
     */
    final override var softDeletedTrueValue: Any? = null

    /**
     * 默认逻辑未删除值.
     */
    final override var softDeletedFalseValue: Any? = null
    private var softDeletedReadMethod: Method? = null
    private var softDeletedWriteMethod: Method? = null

    init {
        var trueValue: String? = null
        var falseValue: String? = null
        var finish = 0
        for (declaredField in domainClass.declaredFields) {
            if (softDeletedPropertyName == null) {
                val annotation = declaredField.getAnnotation(SoftDelete::class.java)
                if (annotation != null) {
                    trueValue = annotation.trueValue
                    falseValue = annotation.falseValue
                    softDeletedPropertyName = declaredField.name
                    softDeletedPropertyType = declaredField.type
                    finish++
                }
            }
            if (lastModifiedDatePropertyName == null
                    && declaredField.getAnnotation<LastModifiedDate>(LastModifiedDate::class.java) != null) {
                lastModifiedDatePropertyName = declaredField.name
                lastModifiedDatePropertyType = declaredField.type
                finish++
            }
            if (versionPropertyName == null && declaredField.getAnnotation<Version>(Version::class.java) != null) {
                versionPropertyName = declaredField.name
                versionPropertyType = declaredField.type
                finish++
            }
            if (finish == 3) {
                break
            }
        }
        if (finish < 3 && domainClass.superclass.isAnnotationPresent(MappedSuperclass::class.java)) {
            val descriptClass = domainClass.superclass
            for (declaredField in descriptClass.declaredFields) {
                if (softDeletedPropertyName == null) {
                    val annotation = declaredField.getAnnotation(SoftDelete::class.java)
                    if (annotation != null) {
                        trueValue = annotation.trueValue
                        falseValue = annotation.falseValue
                        softDeletedPropertyName = declaredField.name
                        softDeletedPropertyType = declaredField.type
                        finish++
                    }
                }
                if (lastModifiedDatePropertyName == null
                        && declaredField.getAnnotation(LastModifiedDate::class.java) != null) {
                    lastModifiedDatePropertyName = declaredField.name
                    lastModifiedDatePropertyType = declaredField.type
                    finish++
                }
                if (versionPropertyName == null && declaredField.getAnnotation(Version::class.java) != null) {
                    versionPropertyName = declaredField.name
                    versionPropertyType = declaredField.type
                    finish++
                }
                if (finish == 3) {
                    break
                }
            }
        }
        if (finish < 3) {
            val propertyDescriptors = BeanUtils.getPropertyDescriptors(domainClass)
            for (propertyDescriptor in propertyDescriptors) {
                if ("class" == propertyDescriptor.name) {
                    continue
                }
                if (softDeletedPropertyName == null) {
                    val annotation = propertyDescriptor.readMethod
                            .getAnnotation(SoftDelete::class.java)
                    if (annotation != null) {
                        trueValue = annotation.trueValue
                        falseValue = annotation.falseValue
                        softDeletedPropertyName = propertyDescriptor.name
                        softDeletedPropertyType = propertyDescriptor.propertyType
                        finish++
                    }
                }
                if (lastModifiedDatePropertyName == null
                        && propertyDescriptor.readMethod.getAnnotation(LastModifiedDate::class.java) != null) {
                    lastModifiedDatePropertyName = propertyDescriptor.name
                    lastModifiedDatePropertyType = propertyDescriptor.propertyType
                    finish++
                }
                if (versionPropertyName == null
                        && propertyDescriptor.readMethod.getAnnotation(Version::class.java) != null) {
                    versionPropertyName = propertyDescriptor.name
                    versionPropertyType = propertyDescriptor.propertyType
                    finish++
                }
                if (finish == 3) {
                    break
                }
            }
        }
        if (softDeletedPropertyName != null) {
            supportSoftDeleted = true
            val softDeleteProperties = jpaExtProperties.softDelete
            val propertyDescriptor = BeanUtils
                    .getPropertyDescriptor(domainClass, softDeletedPropertyName!!)!!
            softDeletedWriteMethod = Objects.requireNonNull(propertyDescriptor).writeMethod
            softDeletedReadMethod = propertyDescriptor.readMethod
            softDeletedTrueValue = if ("" != trueValue) {
                trueValue
            } else {
                softDeleteProperties.trueValue
            }
            softDeletedTrueValue = JpaUtil.convert(softDeletedTrueValue,
                    softDeletedPropertyType)
            softDeletedFalseValue = if ("" != falseValue) {
                falseValue
            } else {
                softDeleteProperties.falseValue
            }
            softDeletedFalseValue = JpaUtil.convert(softDeletedFalseValue,
                    softDeletedPropertyType)
        }
    }

    override fun setSoftDeleted(entity: Any) {
        try {
            softDeletedWriteMethod!!.invoke(entity, softDeletedTrueValue)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun setUnSoftDeleted(entity: Any) {
        try {
            softDeletedWriteMethod!!.invoke(entity, softDeletedFalseValue)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun isSoftDeleted(entity: Any): Boolean {
        return try {
            softDeletedTrueValue == softDeletedReadMethod!!.invoke(entity)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun softDeletedSeted(entity: Any): Boolean {
        return try {
            softDeletedReadMethod!!.invoke(entity) != null
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun supportSoftDeleted(): Boolean {
        return supportSoftDeleted
    }

    override val lastModifiedDateNowValue: Any?
        get() = JpaUtil.convert(LocalDateTime.now(), lastModifiedDatePropertyType)
    override val versionIncValue: Any?
        get() = if (versionPropertyType != null && (Date::class.java.isAssignableFrom(versionPropertyType!!)
                        || TemporalAccessor::class.java.isAssignableFrom(versionPropertyType!!))) {
            JpaUtil.convert(LocalDateTime.now(), versionPropertyType)
        } else {
            JpaUtil.convert(1, versionPropertyType)
        }
}
