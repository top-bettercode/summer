package top.bettercode.simpleframework.data.jpa.query;


import java.util.Collection;

/**
 * @author Peter Wu
 */
public interface SpecPath<T> {

  String getPropertyName();

  boolean isIgnoreCase();

  PathMatcher getMatcher();

  Object getValue();

  SpecMatcher<T> setValue(Object value);

  /**
   * @return return {@literal true} if path was set to be ignored.
   */
  boolean isIgnoredPath();

  SpecMatcher<T> withMatcher(PathMatcher matcher);

  SpecMatcher<T> withMatcher(Object value, PathMatcher matcher);

  /**
   * Create ignoreCase expression for a string.
   *
   * @return this
   */
  SpecMatcher<T> withIgnoreCase();

  /**
   * set path to be ignored.
   *
   * @return this
   */
  SpecMatcher<T> ignoredPath();

  //--------------------------------------------

  //turn Expression<Boolean> into a PredicatePath
  //useful for use with varargs methods

  /**
   * Create a predicate testing for a true value.
   *
   * @return this
   */
  SpecMatcher<T> isTrue();

  /**
   * Create a predicate testing for a false value.
   *
   * @return this
   */
  SpecMatcher<T> isFalse();

  //null tests:

  /**
   * Create a predicate to test whether the expression is null.
   *
   * @return this
   */
  SpecMatcher<T> isNull();

  /**
   * Create a predicate to test whether the expression is not null.
   *
   * @return this
   */
  SpecMatcher<T> isNotNull();

  SpecMatcher<T> equal();

  SpecMatcher<T> notEqual();

  SpecMatcher<T> gt();

  SpecMatcher<T> ge();

  SpecMatcher<T> lt();

  SpecMatcher<T> le();

  SpecMatcher<T> like();

  SpecMatcher<T> starting();

  SpecMatcher<T> ending();

  SpecMatcher<T> containing();

  SpecMatcher<T> notStarting();

  SpecMatcher<T> notEnding();

  SpecMatcher<T> notContaining();

  SpecMatcher<T> notLike();

  //equality:

  /**
   * Create a predicate for testing the arguments for equality.
   *
   * @param value object
   * @return this
   */
  SpecMatcher<T> equal(Object value);

  /**
   * Create a predicate for testing the arguments for inequality.
   *
   * @param value object
   * @return this
   */
  SpecMatcher<T> notEqual(Object value);

  //comparisons for generic (non-numeric) operands:

  /**
   * Create a predicate for testing whether the first argument is greater than the second.
   *
   * @param <Y> Y
   * @param value   value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> gt(Y value);

  /**
   * Create a predicate for testing whether the first argument is greater than or equal to the
   * second.
   *
   * @param <Y> Y
   * @param value   value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> ge(Y value);

  /**
   * Create a predicate for testing whether the first argument is less than the second.
   *
   * @param <Y> Y
   * @param value   value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> lt(Y value);

  /**
   * Create a predicate for testing whether the first argument is less than or equal to the second.
   *
   * @param <Y> Y
   * @param value   value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> le(Y value);

  /**
   * Create a predicate for testing whether the first argument is between the second and third
   * arguments in value.
   *
   * @param <Y> Y
   * @param first   value
   * @param second   value
   * @return this
   */
  <Y extends Comparable<? super Y>> SpecMatcher<T> between(Y first, Y second);

  //string functions:

  /**
   * Create a predicate for testing whether the expression satisfies the given pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> like(String value);

  /**
   * Matches string starting with pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> starting(String value);

  /**
   * Matches string ending with pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> ending(String value);

  /**
   * Matches string containing with pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> containing(String value);

  /**
   * Not Matches string starting with pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> notStarting(String value);

  /**
   * Not Matches string ending with pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> notEnding(String value);

  /**
   * Not Matches string containing with pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> notContaining(String value);

  /**
   * Create a predicate for testing whether the expression does not satisfy the given pattern.
   *
   * @param value string
   * @return this
   */
  SpecMatcher<T> notLike(String value);

  //in builders:

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param value list of values
   * @return this
   */
  SpecMatcher<T> in(Object... value);

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param value list of values
   * @return this
   */
  SpecMatcher<T> in(Collection<?> value);
}


