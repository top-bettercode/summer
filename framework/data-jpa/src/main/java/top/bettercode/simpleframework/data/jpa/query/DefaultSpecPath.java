package top.bettercode.simpleframework.data.jpa.query;


import java.util.Arrays;
import java.util.Collection;

/**
 * @author Peter Wu
 */
public class DefaultSpecPath<T> implements SpecPath<T> {

  private final SpecMatcher<T> specMatcher;
  /**
   * name of the attribute
   */
  private final String propertyName;

  private boolean ignoreCase = false;

  private boolean ignoredPath = false;

  private PathMatcher matcher = PathMatcher.EQ;

  private Object value;


  public DefaultSpecPath(SpecMatcher<T> specMatcher, String propertyName) {
    this.specMatcher = specMatcher;
    this.propertyName = propertyName;
  }

  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  @Override
  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  @Override
  public PathMatcher getMatcher() {
    return matcher;
  }

  @Override
  public Object getValue() {
    return this.value;
  }

  @Override
  public SpecMatcher<T> setValue(Object value) {
    this.value = value;
    return this.specMatcher;
  }

  @Override
  public boolean isIgnoredPath() {
    return this.ignoredPath;
  }

  @Override
  public SpecMatcher<T> withMatcher(PathMatcher matcher) {
    this.matcher = matcher;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> withMatcher(Object value, PathMatcher matcher) {
    this.value = value;
    this.matcher = matcher;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> withIgnoreCase() {
    ignoreCase = true;
    return this.specMatcher;
  }


  @Override
  public SpecMatcher<T> ignoredPath() {
    ignoredPath = true;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> isTrue() {
    this.matcher = PathMatcher.IS_TRUE;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> isFalse() {
    this.matcher = PathMatcher.IS_FALSE;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> isNull() {
    this.matcher = PathMatcher.IS_NULL;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> isNotNull() {
    this.matcher = PathMatcher.IS_NOT_NULL;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> equal() {
    this.matcher = PathMatcher.EQ;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> notEqual() {
    this.matcher = PathMatcher.NE;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> gt() {
    this.matcher = PathMatcher.GT;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> ge() {
    this.matcher = PathMatcher.GE;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> lt() {
    this.matcher = PathMatcher.LT;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> le() {
    this.matcher = PathMatcher.LE;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> like() {
    this.matcher = PathMatcher.LIKE;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> starting() {
    this.matcher = PathMatcher.STARTING;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> ending() {
    this.matcher = PathMatcher.ENDING;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> containing() {
    this.matcher = PathMatcher.CONTAINING;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> notStarting() {
    this.matcher = PathMatcher.NOT_STARTING;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> notEnding() {
    this.matcher = PathMatcher.NOT_ENDING;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> notContaining() {
    this.matcher = PathMatcher.NOT_CONTAINING;
    return this.specMatcher;
  }

  @Override
  public SpecMatcher<T> notLike() {
    this.matcher = PathMatcher.NOT_LIKE;
    return this.specMatcher;
  }

  //--------------------------------------------
  @Override
  public SpecMatcher<T> equal(Object value) {
    return withMatcher(value, PathMatcher.EQ);
  }

  @Override
  public SpecMatcher<T> notEqual(Object value) {
    return withMatcher(value, PathMatcher.NE);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> gt(Y value) {
    return withMatcher(value, PathMatcher.GT);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> ge(Y value) {
    return withMatcher(value, PathMatcher.GE);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> lt(Y value) {
    return withMatcher(value, PathMatcher.LT);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> le(Y value) {
    return withMatcher(value, PathMatcher.LE);
  }

  @Override
  public <Y extends Comparable<? super Y>> SpecMatcher<T> between(Y first, Y second) {
    return withMatcher(new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  @Override
  public SpecMatcher<T> like(String value) {
    return withMatcher(value, PathMatcher.LIKE);
  }

  @Override
  public SpecMatcher<T> starting(String value) {
    return withMatcher(value, PathMatcher.STARTING);
  }

  @Override
  public SpecMatcher<T> ending(String value) {
    return withMatcher(value, PathMatcher.ENDING);
  }

  @Override
  public SpecMatcher<T> containing(String value) {
    return withMatcher(value, PathMatcher.CONTAINING);
  }

  @Override
  public SpecMatcher<T> notStarting(String value) {
    return withMatcher(value, PathMatcher.NOT_STARTING);
  }

  @Override
  public SpecMatcher<T> notEnding(String value) {
    return withMatcher(value, PathMatcher.NOT_ENDING);
  }

  @Override
  public SpecMatcher<T> notContaining(String value) {
    return withMatcher(value, PathMatcher.NOT_CONTAINING);
  }

  @Override
  public SpecMatcher<T> notLike(String value) {
    return withMatcher(value, PathMatcher.NOT_LIKE);
  }

  @Override
  public SpecMatcher<T> in(Object... value) {
    return in(Arrays.asList(value));
  }

  @Override
  public SpecMatcher<T> in(Collection<?> value) {
    return withMatcher(value, PathMatcher.IN);
  }

  static class BetweenValue<Y extends Comparable<? super Y>> {

    private final Y first;
    private final Y second;

    public BetweenValue(Y first, Y second) {
      this.first = first;
      this.second = second;
    }

    public Y getFirst() {
      return first;
    }

    public Y getSecond() {
      return second;
    }
  }
}


