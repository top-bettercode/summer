package top.bettercode.simpleframework.data.jpa.query;

import java.util.Collection;

/**
 * @author Peter Wu
 */
public interface SpecMatcher {

  enum SpecMatcherMode {
    ALL, ANY
  }


  /**
   * 创建 SpecMatcher 实例
   *
   * @return 行政区划 SpecMatcher 实例
   */
  static DefaultSpecMatcher<SpecMatcher> defaultMatching() {
    return defaultMatchingAll();
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @return 行政区划 SpecMatcher 实例
   */
  static DefaultSpecMatcher<SpecMatcher> defaultMatchingAll() {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ALL);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @return 行政区划 SpecMatcher 实例
   */
  static DefaultSpecMatcher<SpecMatcher> defaultMatchingAny() {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ANY);
  }

  default <T> MatcherSpecification<T> spec() {
    return new MatcherSpecification<>(this, null);
  }

  default <T> MatcherSpecification<T> spec(T probe) {
    return new MatcherSpecification<>(this, probe);
  }


  SpecMatcherMode getMatchMode();

  Collection<SpecPath<?>> getSpecPaths();

  SpecPath<? extends SpecMatcher> specPath(String propertyName);

  SpecMatcher withMatcher(String propertyName, Object value, PathMatcher matcher);

  //equality:

  /**
   * Create a predicate for testing the arguments for equality.
   *
   * @param propertyName propertyName
   * @param value        object
   * @return this
   */
  SpecMatcher equal(String propertyName, Object value);

  /**
   * Create a predicate for testing the arguments for inequality.
   *
   * @param propertyName propertyName
   * @param value        object
   * @return this
   */
  SpecMatcher notEqual(String propertyName, Object value);

  //comparisons for generic (non-numeric) operands:

  /**
   * Create a predicate for testing whether the first argument is greater than the second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value        value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher gt(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is greater than or equal to the
   * second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value        value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher ge(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is less than the second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value        value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher lt(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is less than or equal to the second.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param value        value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher le(String propertyName, Y value);

  /**
   * Create a predicate for testing whether the first argument is between the second and third
   * arguments in value.
   *
   * @param propertyName propertyName
   * @param <Y>          Y
   * @param first        value
   * @param second       value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher between(String propertyName, Y first, Y second);

  //string functions:

  /**
   * Create a predicate for testing whether the expression satisfies the given pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher like(String propertyName, String value);

  /**
   * Matches string starting with pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher starting(String propertyName, String value);

  /**
   * Matches string ending with pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher ending(String propertyName, String value);

  /**
   * Matches string containing with pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher containing(String propertyName, String value);

  /**
   * Not Matches string starting with pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher notStarting(String propertyName, String value);

  /**
   * Not Matches string ending with pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher notEnding(String propertyName, String value);

  /**
   * Not Matches string containing with pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher notContaining(String propertyName, String value);

  /**
   * Create a predicate for testing whether the expression does not satisfy the given pattern.
   *
   * @param propertyName propertyName
   * @param value        string
   * @return this
   */
  SpecMatcher notLike(String propertyName, String value);

  //in builders:

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param propertyName propertyName
   * @param value        list of values
   * @return this
   */
  SpecMatcher in(String propertyName, Object... value);

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param propertyName propertyName
   * @param value        list of values
   * @return this
   */
  SpecMatcher in(String propertyName, Collection<?> value);

}
