package top.bettercode.simpleframework.data.jpa.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.util.Assert;

class ExampleSpecification<T> implements Specification<T> {

  private static final long serialVersionUID = 1L;

  private final Example<T> example;
  private final EscapeCharacter escapeCharacter;

  ExampleSpecification(Example<T> example, EscapeCharacter escapeCharacter) {

    Assert.notNull(example, "Example must not be null!");
    Assert.notNull(escapeCharacter, "EscapeCharacter must not be null!");

    this.example = example;
    this.escapeCharacter = escapeCharacter;
  }


  @Override
  public Predicate toPredicate(
      @NotNull Root<T> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder cb) {
    return QueryByExamplePredicateBuilder.getPredicate(root, cb, example, escapeCharacter);
  }
}
