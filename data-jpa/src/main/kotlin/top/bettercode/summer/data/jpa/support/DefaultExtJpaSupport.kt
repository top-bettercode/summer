package top.bettercode.summer.data.jpa.support

import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.AuditorAware
import top.bettercode.summer.data.jpa.LogicalDelete
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.data.jpa.metamodel.LastModifiedByAttribute
import top.bettercode.summer.data.jpa.metamodel.LastModifiedDateAttribute
import top.bettercode.summer.data.jpa.metamodel.LogicalDeletedAttribute
import top.bettercode.summer.data.jpa.metamodel.VersionAttribute
import java.lang.reflect.AnnotatedElement
import javax.persistence.EntityManager
import javax.persistence.Version
import javax.persistence.metamodel.SingularAttribute

/**
 * @author Peter Wu
 */
open class DefaultExtJpaSupport<T>(jpaExtProperties: JpaExtProperties, entityManager: EntityManager, auditorAware: AuditorAware<*>?, domainClass: Class<*>) : ExtJpaSupport<T> {

    final override val logicalDeletedSupported: Boolean
    final override val logicalDeletedAttribute: LogicalDeletedAttribute<T, *>?
    final override val lastModifiedDateAttribute: LastModifiedDateAttribute<T, *>?
    final override val lastModifiedByAttribute: LastModifiedByAttribute<T, *>?
    final override val versionAttribute: VersionAttribute<T, *>?

    init {
        var logicalDeletedAttribute: LogicalDeletedAttribute<T, *>? = null
        var lastModifiedDateAttribute: LastModifiedDateAttribute<T, *>? = null
        var lastModifiedByAttribute: LastModifiedByAttribute<T, *>? = null
        var versionAttribute: VersionAttribute<T, *>? = null
        val attributes = entityManager.metamodel.entity(domainClass).singularAttributes
        attributes.forEach {
            @Suppress("UNCHECKED_CAST")
            it as SingularAttribute<T, *>
            val member = it.javaMember
            if (member is AnnotatedElement) {
                val annotation = member.getAnnotation(LogicalDelete::class.java)
                if (annotation != null) {
                    logicalDeletedAttribute = LogicalDeletedAttribute(it, annotation, jpaExtProperties)
                }
                if (member.isAnnotationPresent(LastModifiedDate::class.java)) {
                    lastModifiedDateAttribute = LastModifiedDateAttribute(it)
                }
                if (member.isAnnotationPresent(LastModifiedBy::class.java)) {
                    lastModifiedByAttribute = LastModifiedByAttribute(it, auditorAware)
                }
                if (member.isAnnotationPresent(Version::class.java)) {
                    versionAttribute = VersionAttribute(it)
                }
            }
        }
        this.logicalDeletedSupported = logicalDeletedAttribute != null
        this.logicalDeletedAttribute = logicalDeletedAttribute
        this.lastModifiedDateAttribute = lastModifiedDateAttribute
        this.lastModifiedByAttribute = lastModifiedByAttribute
        this.versionAttribute = versionAttribute
    }
}
