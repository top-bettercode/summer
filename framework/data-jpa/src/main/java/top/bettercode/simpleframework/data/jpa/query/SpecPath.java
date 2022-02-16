package top.bettercode.simpleframework.data.jpa.query;


import java.util.Collection;
import org.springframework.data.domain.Sort.Direction;

/**
 * @author Peter Wu
 */
public interface SpecPath<E extends SpecMatcher> {

  String getPropertyName();

  boolean isIgnoreCase();

  PathMatcher getMatcher();

  Object getValue();

  /**
   * @return return {@literal true} if path was set to be ignored.
   */
  boolean isIgnoredPath();

  Direction getDirection();

  SpecPath<E> setValue(Object value);

  E asc();

  E desc();

  E withMatcher(PathMatcher matcher);

  E withMatcher(Object value, PathMatcher matcher);

  /**
   * Create ignoreCase expression for a string.
   *
   * @return this
   */
  E withIgnoreCase();

  /**
   * set path to be ignored.
   *
   * @return this
   */
  E ignoredPath();

  //--------------------------------------------

  //turn Expression<Boolean> into a PredicatePath
  //useful for use with varargs methods

  /**
   * Create a predicate testing for a true value.
   *
   * @return this
   */
  E isTrue();

  /**
   * Create a predicate testing for a false value.
   *
   * @return this
   */
  E isFalse();

  //null tests:

  /**
   * Create a predicate to test whether the expression is null.
   *
   * @return this
   */
  E isNull();

  /**
   * Create a predicate to test whether the expression is not null.
   *
   * @return this
   */
  E isNotNull();

  E equal();

  E notEqual();

  E gt();

  E ge();

  E lt();

  E le();

  E like();

  E starting();

  E ending();

  E containing();

  E notStarting();

  E notEnding();

  E notContaining();

  E notLike();

  //equality:

  /**
   * Create a predicate for testing the arguments for equality.
   *
   * @param value object
   * @return this
   */
  E equal(Object value);

  /**
   * Create a predicate for testing the arguments for inequality.
   *
   * @param value object
   * @return this
   */
  E notEqual(Object value);

  //comparisons for generic (non-numeric) operands:

  /**
   * Create a predicate for testing whether the first argument is greater than the second.
   *
   * @param <Y>   Y
   * @param value value
   * @return this
   */
  <Y extends Comparable<? super Y>> E gt(Y value);

  /**
   * Create a predicate for testing whether the first argument is greater than or equal to the
   * second.
   *
   * @param <Y>   Y
   * @param value value
   * @return this
   */
  <Y extends Comparable<? super Y>> E ge(Y value);

  /**
   * Create a predicate for testing whether the first argument is less than the second.
   *
   * @param <Y>   Y
   * @param value value
   * @return this
   */
  <Y extends Comparable<? super Y>> E lt(Y value);

  /**
   * Create a predicate for testing whether the first argument is less than or equal to the second.
   *
   * @param <Y>   Y
   * @param value value
   * @return this
   */
  <Y extends Comparable<? super Y>> E le(Y value);

  /**
   * Create a predicate for testing whether the first argument is between the second and third
   * arguments in value.
   *
   * @param <Y>    Y
   * @param first  value
   * @param second value
   * @return this
   */
  <Y extends Comparable<? super Y>> E between(Y first, Y second);

  //string functions:

  /**
   * Create a predicate for testing whether the expression satisfies the given pattern.
   *
   * @param value string
   * @return this
   */
  E like(String value);

  /**
   * Matches string starting with pattern.
   *
   * @param value string
   * @return this
   */
  E starting(String value);

  /**
   * Matches string ending with pattern.
   *
   * @param value string
   * @return this
   */
  E ending(String value);

  /**
   * Matches string containing with pattern.
   *
   * @param value string
   * @return this
   */
  E containing(String value);

  /**
   * Not Matches string starting with pattern.
   *
   * @param value string
   * @return this
   */
  E notStarting(String value);

  /**
   * Not Matches string ending with pattern.
   *
   * @param value string
   * @return this
   */
  E notEnding(String value);

  /**
   * Not Matches string containing with pattern.
   *
   * @param value string
   * @return this
   */
  E notContaining(String value);

  /**
   * Create a predicate for testing whether the expression does not satisfy the given pattern.
   *
   * @param value string
   * @return this
   */
  E notLike(String value);

  //in builders:

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param value list of values
   * @return this
   */
  E in(Object... value);

  /**
   * Create predicate to test whether given expression is contained in a list of values.
   *
   * @param value list of values
   * @return this
   */
  E in(Collection<?> value);

  E notIn(Object... value);

  E notIn(Collection<?> value);
}


