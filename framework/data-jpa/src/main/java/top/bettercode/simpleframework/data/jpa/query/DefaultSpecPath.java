package top.bettercode.simpleframework.data.jpa.query;


import java.util.Arrays;
import java.util.Collection;

/**
 * @author Peter Wu
 */
public class DefaultSpecPath<E extends SpecMatcher> implements SpecPath<E> {

  private final E specMatcher;
  /**
   * name of the attribute
   */
  private final String propertyName;

  private boolean ignoreCase = false;

  private boolean ignoredPath = false;

  private PathMatcher matcher = PathMatcher.EQ;

  private Object value;


  public DefaultSpecPath(E specMatcher, String propertyName) {
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
  public boolean isIgnoredPath() {
    return this.ignoredPath;
  }

  @Override
  public SpecPath<E> setValue(Object value) {
    this.value = value;
    return this;
  }

  @Override
  public E withMatcher(PathMatcher matcher) {
    this.matcher = matcher;
    return this.specMatcher;
  }

  @Override
  public E withMatcher(Object value, PathMatcher matcher) {
    this.value = value;
    this.matcher = matcher;
    return this.specMatcher;
  }

  @Override
  public E withIgnoreCase() {
    ignoreCase = true;
    return this.specMatcher;
  }


  @Override
  public E ignoredPath() {
    ignoredPath = true;
    return this.specMatcher;
  }

  @Override
  public E isTrue() {
    this.matcher = PathMatcher.IS_TRUE;
    return this.specMatcher;
  }

  @Override
  public E isFalse() {
    this.matcher = PathMatcher.IS_FALSE;
    return this.specMatcher;
  }

  @Override
  public E isNull() {
    this.matcher = PathMatcher.IS_NULL;
    return this.specMatcher;
  }

  @Override
  public E isNotNull() {
    this.matcher = PathMatcher.IS_NOT_NULL;
    return this.specMatcher;
  }

  @Override
  public E equal() {
    this.matcher = PathMatcher.EQ;
    return this.specMatcher;
  }

  @Override
  public E notEqual() {
    this.matcher = PathMatcher.NE;
    return this.specMatcher;
  }

  @Override
  public E gt() {
    this.matcher = PathMatcher.GT;
    return this.specMatcher;
  }

  @Override
  public E ge() {
    this.matcher = PathMatcher.GE;
    return this.specMatcher;
  }

  @Override
  public E lt() {
    this.matcher = PathMatcher.LT;
    return this.specMatcher;
  }

  @Override
  public E le() {
    this.matcher = PathMatcher.LE;
    return this.specMatcher;
  }

  @Override
  public E like() {
    this.matcher = PathMatcher.LIKE;
    return this.specMatcher;
  }

  @Override
  public E starting() {
    this.matcher = PathMatcher.STARTING;
    return this.specMatcher;
  }

  @Override
  public E ending() {
    this.matcher = PathMatcher.ENDING;
    return this.specMatcher;
  }

  @Override
  public E containing() {
    this.matcher = PathMatcher.CONTAINING;
    return this.specMatcher;
  }

  @Override
  public E notStarting() {
    this.matcher = PathMatcher.NOT_STARTING;
    return this.specMatcher;
  }

  @Override
  public E notEnding() {
    this.matcher = PathMatcher.NOT_ENDING;
    return this.specMatcher;
  }

  @Override
  public E notContaining() {
    this.matcher = PathMatcher.NOT_CONTAINING;
    return this.specMatcher;
  }

  @Override
  public E notLike() {
    this.matcher = PathMatcher.NOT_LIKE;
    return this.specMatcher;
  }

  //--------------------------------------------
  @Override
  public E equal(Object value) {
    return withMatcher(value, PathMatcher.EQ);
  }

  @Override
  public E notEqual(Object value) {
    return withMatcher(value, PathMatcher.NE);
  }

  @Override
  public <Y extends Comparable<? super Y>> E gt(Y value) {
    return withMatcher(value, PathMatcher.GT);
  }

  @Override
  public <Y extends Comparable<? super Y>> E ge(Y value) {
    return withMatcher(value, PathMatcher.GE);
  }

  @Override
  public <Y extends Comparable<? super Y>> E lt(Y value) {
    return withMatcher(value, PathMatcher.LT);
  }

  @Override
  public <Y extends Comparable<? super Y>> E le(Y value) {
    return withMatcher(value, PathMatcher.LE);
  }

  @Override
  public <Y extends Comparable<? super Y>> E between(Y first, Y second) {
    return withMatcher(new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  @Override
  public E like(String value) {
    return withMatcher(value, PathMatcher.LIKE);
  }

  @Override
  public E starting(String value) {
    return withMatcher(value, PathMatcher.STARTING);
  }

  @Override
  public E ending(String value) {
    return withMatcher(value, PathMatcher.ENDING);
  }

  @Override
  public E containing(String value) {
    return withMatcher(value, PathMatcher.CONTAINING);
  }

  @Override
  public E notStarting(String value) {
    return withMatcher(value, PathMatcher.NOT_STARTING);
  }

  @Override
  public E notEnding(String value) {
    return withMatcher(value, PathMatcher.NOT_ENDING);
  }

  @Override
  public E notContaining(String value) {
    return withMatcher(value, PathMatcher.NOT_CONTAINING);
  }

  @Override
  public E notLike(String value) {
    return withMatcher(value, PathMatcher.NOT_LIKE);
  }

  @Override
  public E in(Object... value) {
    return in(Arrays.asList(value));
  }

  @Override
  public E in(Collection<?> value) {
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


