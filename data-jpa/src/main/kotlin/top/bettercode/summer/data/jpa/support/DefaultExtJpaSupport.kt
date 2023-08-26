package top.bettercode.summer.data.jpa.support

import org.springframework.beans.BeanUtils
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import top.bettercode.summer.data.jpa.LogicalDelete
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
    private var supportLogicalDeleted = false
    final override var logicalDeletedPropertyType: Class<*>? = null
    private var lastModifiedDatePropertyType: Class<*>? = null
    private var lastModifiedByPropertyType: Class<*>? = null
    private var versionPropertyType: Class<*>? = null

    /**
     * 逻辑删除字段属性名.
     */
    final override var logicalDeletedPropertyName: String? = null
    final override var lastModifiedDatePropertyName: String? = null
    final override var lastModifiedByPropertyName: String? = null
    final override var versionPropertyName: String? = null

    /**
     * 默认逻辑删除值.
     */
    final override var logicalDeletedTrueValue: Any? = null

    /**
     * 默认逻辑未删除值.
     */
    final override var logicalDeletedFalseValue: Any? = null
    private var logicalDeletedReadMethod: Method? = null
    private var logicalDeletedWriteMethod: Method? = null

    init {
        var trueValue: String? = null
        var falseValue: String? = null
        var finish = 0
        val total = 4
        for (declaredField in domainClass.declaredFields) {
            if (logicalDeletedPropertyName == null) {
                val annotation = declaredField.getAnnotation(LogicalDelete::class.java)
                if (annotation != null) {
                    trueValue = annotation.trueValue
                    falseValue = annotation.falseValue
                    logicalDeletedPropertyName = declaredField.name
                    logicalDeletedPropertyType = declaredField.type
                    finish++
                }
            }
            if (lastModifiedDatePropertyName == null
                    && declaredField.getAnnotation(LastModifiedDate::class.java) != null) {
                lastModifiedDatePropertyName = declaredField.name
                lastModifiedDatePropertyType = declaredField.type
                finish++
            }
            if (lastModifiedByPropertyName == null
                    && declaredField.getAnnotation(LastModifiedBy::class.java) != null) {
                lastModifiedByPropertyName = declaredField.name
                lastModifiedByPropertyType = declaredField.type
                finish++
            }
            if (versionPropertyName == null && declaredField.getAnnotation(Version::class.java) != null) {
                versionPropertyName = declaredField.name
                versionPropertyType = declaredField.type
                finish++
            }
            if (finish == total) {
                break
            }
        }
        if (finish < total && domainClass.superclass.isAnnotationPresent(MappedSuperclass::class.java)) {
            val descriptClass = domainClass.superclass
            for (declaredField in descriptClass.declaredFields) {
                if (logicalDeletedPropertyName == null) {
                    val annotation = declaredField.getAnnotation(LogicalDelete::class.java)
                    if (annotation != null) {
                        trueValue = annotation.trueValue
                        falseValue = annotation.falseValue
                        logicalDeletedPropertyName = declaredField.name
                        logicalDeletedPropertyType = declaredField.type
                        finish++
                    }
                }
                if (lastModifiedDatePropertyName == null
                        && declaredField.getAnnotation(LastModifiedDate::class.java) != null) {
                    lastModifiedDatePropertyName = declaredField.name
                    lastModifiedDatePropertyType = declaredField.type
                    finish++
                }
                if (lastModifiedByPropertyName == null
                        && declaredField.getAnnotation(LastModifiedBy::class.java) != null) {
                    lastModifiedByPropertyName = declaredField.name
                    lastModifiedByPropertyType = declaredField.type
                    finish++
                }
                if (versionPropertyName == null && declaredField.getAnnotation(Version::class.java) != null) {
                    versionPropertyName = declaredField.name
                    versionPropertyType = declaredField.type
                    finish++
                }
                if (finish == total) {
                    break
                }
            }
        }
        if (finish < total) {
            val propertyDescriptors = BeanUtils.getPropertyDescriptors(domainClass)
            for (propertyDescriptor in propertyDescriptors) {
                if ("class" == propertyDescriptor.name) {
                    continue
                }
                if (logicalDeletedPropertyName == null) {
                    val annotation = propertyDescriptor.readMethod
                            .getAnnotation(LogicalDelete::class.java)
                    if (annotation != null) {
                        trueValue = annotation.trueValue
                        falseValue = annotation.falseValue
                        logicalDeletedPropertyName = propertyDescriptor.name
                        logicalDeletedPropertyType = propertyDescriptor.propertyType
                        finish++
                    }
                }
                if (lastModifiedDatePropertyName == null
                        && propertyDescriptor.readMethod.getAnnotation(LastModifiedDate::class.java) != null) {
                    lastModifiedDatePropertyName = propertyDescriptor.name
                    lastModifiedDatePropertyType = propertyDescriptor.propertyType
                    finish++
                }
                if (lastModifiedByPropertyName == null
                        && propertyDescriptor.readMethod.getAnnotation(LastModifiedBy::class.java) != null) {
                    lastModifiedByPropertyName = propertyDescriptor.name
                    lastModifiedByPropertyType = propertyDescriptor.propertyType
                    finish++
                }
                if (versionPropertyName == null
                        && propertyDescriptor.readMethod.getAnnotation(Version::class.java) != null) {
                    versionPropertyName = propertyDescriptor.name
                    versionPropertyType = propertyDescriptor.propertyType
                    finish++
                }
                if (finish == total) {
                    break
                }
            }
        }
        if (logicalDeletedPropertyName != null) {
            supportLogicalDeleted = true
            val logicalDeleteProperties = jpaExtProperties.logicalDelete
            val propertyDescriptor = BeanUtils
                    .getPropertyDescriptor(domainClass, logicalDeletedPropertyName!!)!!
            logicalDeletedWriteMethod = propertyDescriptor.writeMethod
            logicalDeletedReadMethod = propertyDescriptor.readMethod
            logicalDeletedTrueValue = if ("" != trueValue) {
                trueValue
            } else {
                logicalDeleteProperties.trueValue
            }
            logicalDeletedTrueValue = JpaUtil.convert(logicalDeletedTrueValue,
                    logicalDeletedPropertyType)
            logicalDeletedFalseValue = if ("" != falseValue) {
                falseValue
            } else {
                logicalDeleteProperties.falseValue
            }
            logicalDeletedFalseValue = JpaUtil.convert(logicalDeletedFalseValue,
                    logicalDeletedPropertyType)
        }
    }

    override fun setLogicalDeleted(entity: Any) {
        try {
            logicalDeletedWriteMethod!!.invoke(entity, logicalDeletedTrueValue)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun setUnLogicalDeleted(entity: Any) {
        try {
            logicalDeletedWriteMethod!!.invoke(entity, logicalDeletedFalseValue)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun isLogicalDeleted(entity: Any): Boolean {
        return try {
            logicalDeletedTrueValue == logicalDeletedReadMethod!!.invoke(entity)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    override fun logicalDeletedSeted(entity: Any): Boolean {
        return try {
            logicalDeletedReadMethod!!.invoke(entity) != null
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e.message, e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.message, e)
        }
    }

    final override fun supportLogicalDeleted(): Boolean {
        return supportLogicalDeleted
    }

    override fun lastModifiedBy(auditor: Any?): Any? {
        return if (lastModifiedByPropertyType == null) auditor else JpaUtil.convert(auditor, lastModifiedByPropertyType)
    }

    override val lastModifiedDateNowValue: Any?
        get() = if (lastModifiedDatePropertyType == null) LocalDateTime.now() else JpaUtil.convert(LocalDateTime.now(), lastModifiedDatePropertyType)

    override val versionIncValue: Any?
        get() = if (versionPropertyType != null && (Date::class.java.isAssignableFrom(versionPropertyType!!)
                        || TemporalAccessor::class.java.isAssignableFrom(versionPropertyType!!))) {
            JpaUtil.convert(LocalDateTime.now(), versionPropertyType)
        } else {
            JpaUtil.convert(1, versionPropertyType)
        }
}
