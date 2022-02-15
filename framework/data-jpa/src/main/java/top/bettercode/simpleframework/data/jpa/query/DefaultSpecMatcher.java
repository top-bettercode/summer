package top.bettercode.simpleframework.data.jpa.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import top.bettercode.simpleframework.data.jpa.query.DefaultSpecPath.BetweenValue;

/**
 * @author Peter Wu
 */
public class DefaultSpecMatcher<T> implements SpecMatcher<T> {

  private final SpecMatcherMode mode;
  private final Map<String, SpecPath<T>> specPaths = new HashMap<>();

  /**
   * 创建 SpecMatcher 实例
   *
   * @param <T> T
   * @return 行政区划 SpecMatcher 实例
   */
  public static <T> DefaultSpecMatcher<T> matching() {
    return matchingAll();
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @param <T> T
   * @return 行政区划 SpecMatcher 实例
   */
  public static <T> DefaultSpecMatcher<T> matchingAll() {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ALL);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @param <T> T
   * @return 行政区划 SpecMatcher 实例
   */
  public static <T> DefaultSpecMatcher<T> matchingAny() {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ANY);
  }

  protected DefaultSpecMatcher(SpecMatcherMode mode) {
    this.mode = mode;
  }

  @Override
  public SpecMatcherMode getMatchMode() {
    return this.mode;
  }

  @Override
  public Collection<SpecPath<T>> getSpecPaths() {
    return specPaths.values();
  }

  @Override
  public SpecPath<T> specPath(String propertyName) {
    return specPaths.computeIfAbsent(propertyName, s -> new DefaultSpecPath<>(this, propertyName));
  }

  @Override
  public SpecMatcher<T> withMatcher(String propertyName, Object value, PathMatcher matcher) {
    SpecPath<T> specPath = specPath(propertyName);
    specPath.setValue(value);
    specPath.withMatcher(matcher);
    return this;
  }

  @Override
  public SpecMatcher<T> equal(String propertyName, Object value) {
    return withMatcher(propertyName, value, PathMatcher.EQ);
  }

  @Override
  public SpecMatcher<T> notEqual(String propertyName, Object value) {
    return withMatcher(propertyName, value, PathMatcher.NE);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> gt(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.GT);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> ge(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.GE);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> lt(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.LT);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> le(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.LE);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> between(String propertyName, Y first,
      Y second) {
    return withMatcher(propertyName, new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  @Override
  public SpecMatcher<T> like(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.LIKE);
  }

  @Override
  public SpecMatcher<T> starting(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.STARTING);
  }

  @Override
  public SpecMatcher<T> ending(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.ENDING);
  }

  @Override
  public SpecMatcher<T> containing(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.CONTAINING);
  }

  @Override
  public SpecMatcher<T> notStarting(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_STARTING);
  }

  @Override
  public SpecMatcher<T> notEnding(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_ENDING);
  }

  @Override
  public SpecMatcher<T> notContaining(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_CONTAINING);
  }

  @Override
  public SpecMatcher<T> notLike(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_LIKE);
  }

  @Override
  public SpecMatcher<T> in(String propertyName, Object... value) {
    return in(propertyName, Arrays.asList(value));
  }

  @Override
  public SpecMatcher<T> in(String propertyName, Collection<?> value) {
    return withMatcher(propertyName, value, PathMatcher.IN);
  }


}
