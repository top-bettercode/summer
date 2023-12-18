package org.springframework.data.jpa.repository.query

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.repository.query.parser.PartTree
import top.bettercode.summer.data.jpa.support.ExtJpaSupport

/**
 * @author Peter Wu
 */
internal class JpaExtCountQueryCreator(
        tree: PartTree,
        type: ReturnedType,
        builder: CriteriaBuilder,
        provider: ParameterMetadataProvider,
        private val logicalDeleteSupport: ExtJpaSupport<*>
) : JpaCountQueryCreator(tree, type, builder, provider) {
    override fun complete(predicate: Predicate?, sort: Sort, query: CriteriaQuery<out Any>, builder: CriteriaBuilder, root: Root<*>): CriteriaQuery<out Any> {
        val predicate1: Predicate? =
                logicalDeleteSupport.logicalDeletedAttribute?.andNotDeleted(predicate, builder, root)
                        ?: predicate
        return super.complete(predicate1, sort, query, builder, root)
    }
}
