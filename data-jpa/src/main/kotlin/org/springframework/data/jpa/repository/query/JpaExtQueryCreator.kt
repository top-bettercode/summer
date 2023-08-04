package org.springframework.data.jpa.repository.query

import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.repository.query.parser.PartTree
import top.bettercode.summer.data.jpa.support.ExtJpaSupport
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * @author Peter Wu
 */
internal class JpaExtQueryCreator
/**
 * Create a new [JpaQueryCreator].
 *
 * @param tree              must not be null.
 * @param type              must not be null.
 * @param builder           must not be null.
 * @param provider          must not be null.
 * @param softDeleteSupport softDeleteSupport
 */(
        tree: PartTree,
        type: ReturnedType,
        builder: CriteriaBuilder,
        provider: ParameterMetadataProvider,
        private val softDeleteSupport: ExtJpaSupport
) : JpaQueryCreator(tree, type, builder, provider) {
    override fun complete(predicate: Predicate?, sort: Sort, query: CriteriaQuery<out Any>, builder: CriteriaBuilder, root: Root<*>): CriteriaQuery<out Any> {
        var predicate1: Predicate? = predicate
        if (predicate1 != null && softDeleteSupport.supportSoftDeleted()) {
            val deletedPath = root.get<Boolean>(softDeleteSupport.softDeletedPropertyName)
            predicate1 = builder.and(predicate1, builder.isFalse(deletedPath))
        }
        return super.complete(predicate1, sort, query, builder, root)
    }
}
