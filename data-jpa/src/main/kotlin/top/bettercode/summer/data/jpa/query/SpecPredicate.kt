package top.bettercode.summer.data.jpa.query

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

/**
 * @author Peter Wu
 */
interface SpecPredicate<T : Any?, M : SpecMatcher<T, M>> {
    fun toPredicate(root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate?
}
