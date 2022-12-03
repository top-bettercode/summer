package top.bettercode.summer.data.jpa.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author Peter Wu
 */
public interface SpecPredicate<T, M extends SpecMatcher<T, M>> {

  Predicate toPredicate(Root<T> root, CriteriaBuilder criteriaBuilder);
}
