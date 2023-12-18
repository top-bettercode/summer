package top.bettercode.summer.data.jpa.metamodel

import jakarta.persistence.metamodel.SingularAttribute


/**
 *
 * @author Peter Wu
 */
open class SingularAttributeExt<X, T>(
        val singularAttribute: SingularAttribute<X, T>,
        val isLogicalDeleted: Boolean = false,
        val isLastModifiedDate: Boolean = false,
        val isLastModifiedBy: Boolean = false,
) : SingularAttribute<X, T> by singularAttribute