package top.bettercode.summer.data.jpa.metamodel

import jakarta.persistence.metamodel.SingularAttribute


/**
 *
 * @author Peter Wu
 */
class SingularAttributeValue<X, T>(singularAttribute: SingularAttribute<X, T>, val value: Any) : SingularAttribute<X, T> by singularAttribute