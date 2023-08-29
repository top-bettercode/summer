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
