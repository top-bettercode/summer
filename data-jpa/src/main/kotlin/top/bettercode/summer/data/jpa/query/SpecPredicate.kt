package top.bettercode.summer.data.jpa.query

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * @author Peter Wu
 */
interface SpecPredicate<T, M : SpecMatcher<T, M>> {
    fun toPredicate(root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate?
}
