package top.bettercode.summer.data.jpa.support

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Example
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.util.Assert

internal class ExampleSpecification<T>(example: Example<T>, escapeCharacter: EscapeCharacter) : Specification<T> {
    private val example: Example<T>
    private val escapeCharacter: EscapeCharacter

    init {
        Assert.notNull(example, "Example must not be null!")
        Assert.notNull(escapeCharacter, "EscapeCharacter must not be null!")
        this.example = example
        this.escapeCharacter = escapeCharacter
    }

    override fun toPredicate(
            root: Root<T>, query: CriteriaQuery<*>, cb: CriteriaBuilder
    ): Predicate? {
        return QueryByExamplePredicateBuilder.getPredicate(root, cb, example, escapeCharacter)
    }

}
