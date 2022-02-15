package top.bettercode.simpleframework.data.jpa.query;

import java.util.Collection;

/**
 * @author Peter Wu
 */
public interface SpecMatcher<T> {

  SpecMatcherMode getMatchMode();

  Collection<SpecPath<T>> getSpecPaths();

  SpecPath<T> specPath(String propertyName);

  SpecMatcher<T> withMatcher(String propertyName, Object value, PathMatcher matcher);

  //equality:

  /**
   * Create a predicate for testing the arguments for equality.
   *
   * @param propertyName propertyName
   * @param value            object
   * @return this
   */
  SpecMatcher<T> equal(String propertyName, Object value);

  /**
   * Create a predicate for testing the arguments for inequality.
   *
   * @param propertyName propertyName
   * @param value            object
   * @return this
   */
  SpecMatcher<T> notEqual(String propertyName, Object value);

  //comparisons for generic (non-numeric) operands:

  /**
   * Create a predicate for testing whether the first argument is greater than the second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value            value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> gt(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is greater than or equal to the
   * second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value            value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> ge(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is less than the second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value            value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> lt(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is less than or equal to the second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value            value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> le(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is between the second and third
   * arguments in value.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param first            value
   * @param second            value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> between(String propertyName, Y first, Y second);

  //string functions:

  /**
   * Create a predicate for testing whether the expression satisfies the given pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> like(String propertyName, String value);

  /**
   * Matches string starting with pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> starting(String propertyName, String value);

  /**
   * Matches string ending with pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> ending(String propertyName, String value);

  /**
   * Matches string containing with pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> containing(String propertyName, String value);

  /**
   * Not Matches string starting with pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> notStarting(String propertyName, String value);

  /**
   * Not Matches string ending with pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> notEnding(String propertyName, String value);

  /**
   * Not Matches string containing with pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> notContaining(String propertyName, String value);

  /**
   * Create a predicate for testing whether the expression does not satisfy the given pattern.
   *
   * @param propertyName propertyName
   * @param value      string
   * @return this
   */
  SpecMatcher<T> notLike(String propertyName, String value);

  //in builders:

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param propertyName propertyName
   * @param value            list of values
   * @return this
   */
  SpecMatcher<T> in(String propertyName, Object... value);

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param propertyName propertyName
   * @param value            list of values
   * @return this
   */
  SpecMatcher<T> in(String propertyName, Collection<?> value);

  default MatcherSpecification<T> spec() {
    return new MatcherSpecification<>(this, null);
  }

  default MatcherSpecification<T> spec(T probe) {
    return new MatcherSpecification<>(this, probe);
  }

  enum SpecMatcherMode {
    ALL, ANY
  }

}
