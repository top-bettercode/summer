package top.bettercode.simpleframework.data.jpa.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.data.jpa.query.DefaultSpecPath.BetweenValue;

/**
 * @author Peter Wu
 */
public class DefaultSpecMatcher<M extends SpecMatcher> implements SpecMatcher {

  private final SpecMatcherMode matcherMode;
  private final Map<String, SpecPath<? extends SpecMatcher>> specPaths = new HashMap<>();
  private M typed;


  @SuppressWarnings("unchecked")
  protected DefaultSpecMatcher(SpecMatcherMode matcherMode) {
    this.matcherMode = matcherMode;
    this.typed = (M) this;
  }

  public M setTyped(M typed) {
    this.typed = typed;
    return typed;
  }

  @Override
  public SpecMatcherMode getMatchMode() {
    return this.matcherMode;
  }

  @Override
  public Collection<SpecPath<?>> getSpecPaths() {
    return specPaths.values();
  }

  @SuppressWarnings("unchecked")
  @Override
  public SpecPath<M> specPath(String propertyName) {
    Assert.hasText(propertyName, "propertyName can not be blank.");
    return (SpecPath<M>) specPaths.computeIfAbsent(propertyName,
        s -> new DefaultSpecPath<>(typed, propertyName));
  }

  @Override
  public M asc(String propertyName) {
    return specPath(propertyName).asc();
  }

  @Override
  public M desc(String propertyName) {
    return specPath(propertyName).desc();
  }

  @Override
  public M withMatcher(String propertyName, Object value, PathMatcher matcher) {
    return specPath(propertyName).setValue(value).withMatcher(matcher);
  }

  @Override
  public M equal(String propertyName, Object value) {
    return withMatcher(propertyName, value, PathMatcher.EQ);
  }

  @Override
  public M notEqual(String propertyName, Object value) {
    return withMatcher(propertyName, value, PathMatcher.NE);
  }

  @Override
  public <Y extends Comparable<? super Y>> M gt(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.GT);
  }

  @Override
  public <Y extends Comparable<? super Y>> M ge(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.GE);
  }

  @Override
  public <Y extends Comparable<? super Y>> M lt(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.LT);
  }

  @Override
  public <Y extends Comparable<? super Y>> M le(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.LE);
  }

  @Override
  public <Y extends Comparable<? super Y>> M between(String propertyName, Y first,
      Y second) {
    return withMatcher(propertyName, new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  @Override
  public M like(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.LIKE);
  }

  @Override
  public M starting(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.STARTING);
  }

  @Override
  public M ending(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.ENDING);
  }

  @Override
  public M containing(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.CONTAINING);
  }

  @Override
  public M notStarting(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_STARTING);
  }

  @Override
  public M notEnding(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_ENDING);
  }

  @Override
  public M notContaining(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_CONTAINING);
  }

  @Override
  public M notLike(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_LIKE);
  }

  @Override
  public M in(String propertyName, Object... value) {
    return withMatcher(propertyName, Arrays.asList(value), PathMatcher.IN);
  }

  @Override
  public M in(String propertyName, Collection<?> value) {
    return withMatcher(propertyName, value, PathMatcher.IN);
  }


  @Override
  public M notIn(String propertyName, Object... value) {
    return withMatcher(propertyName, Arrays.asList(value), PathMatcher.NOT_IN);
  }

  @Override
  public M notIn(String propertyName, Collection<?> value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_IN);
  }

}
