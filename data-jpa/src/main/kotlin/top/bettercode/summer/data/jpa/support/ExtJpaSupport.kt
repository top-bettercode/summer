package top.bettercode.summer.data.jpa.support

import top.bettercode.summer.data.jpa.metamodel.LastModifiedByAttribute
import top.bettercode.summer.data.jpa.metamodel.LastModifiedDateAttribute
import top.bettercode.summer.data.jpa.metamodel.LogicalDeletedAttribute
import top.bettercode.summer.data.jpa.metamodel.VersionAttribute
import javax.persistence.metamodel.SingularAttribute

/**
 * @author Peter Wu
 */
interface ExtJpaSupport<T> {

    val idAttribute: SingularAttribute<T, *>?

    val logicalDeletedSupported: Boolean

    val logicalDeletedAttribute: LogicalDeletedAttribute<T, *>?

    val lastModifiedDateAttribute: LastModifiedDateAttribute<T, *>?

    val lastModifiedByAttribute: LastModifiedByAttribute<T, *>?

    val versionAttribute: VersionAttribute<T, *>?
}
