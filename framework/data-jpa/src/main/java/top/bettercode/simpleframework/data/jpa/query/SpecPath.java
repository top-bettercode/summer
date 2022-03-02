package top.bettercode.simpleframework.data.jpa.query;


import java.util.Arrays;
import java.util.Collection;
import org.springframework.data.domain.Sort.Direction;

/**
 * @author Peter Wu
 */
public class SpecPath<T, M extends SpecMatcher<T, M>> {

  private final M specMatcher;
  /**
   * name of the attribute
   */
  private final String propertyName;

  private boolean ignoreCase = false;

  private boolean ignoredPath = false;

  private PathMatcher matcher = PathMatcher.EQ;

  private Object value;

  private Direction direction = null;


  public SpecPath(M specMatcher, String propertyName) {
    this.specMatcher = specMatcher;
    this.propertyName = propertyName;
  }

  public String getPropertyName() {
    return this.propertyName;
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public PathMatcher getMatcher() {
    return matcher;
  }

  public Object getValue() {
    return this.value;
  }

  public boolean isIgnoredPath() {
    return this.ignoredPath;
  }

  public Direction getDirection() {
    return direction;
  }

  public SpecPath<T, M> setValue(Object value) {
    this.value = value;
    return this;
  }

  public M asc() {
    this.direction = Direction.ASC;
    return this.specMatcher;
  }

  public M desc() {
    this.direction = Direction.DESC;
    return this.specMatcher;
  }

  public M withMatcher(PathMatcher matcher) {
    this.matcher = matcher;
    return this.specMatcher;
  }

  public M withMatcher(Object value, PathMatcher matcher) {
    this.value = value;
    this.matcher = matcher;
    return this.specMatcher;
  }

  public M withIgnoreCase() {
    ignoreCase = true;
    return this.specMatcher;
  }


  public M ignoredPath() {
    ignoredPath = true;
    return this.specMatcher;
  }

  public M isTrue() {
    this.matcher = PathMatcher.IS_TRUE;
    return this.specMatcher;
  }

  public M isFalse() {
    this.matcher = PathMatcher.IS_FALSE;
    return this.specMatcher;
  }

  public M isNull() {
    this.matcher = PathMatcher.IS_NULL;
    return this.specMatcher;
  }

  public M isNotNull() {
    this.matcher = PathMatcher.IS_NOT_NULL;
    return this.specMatcher;
  }

  public M equal() {
    this.matcher = PathMatcher.EQ;
    return this.specMatcher;
  }

  public M notEqual() {
    this.matcher = PathMatcher.NE;
    return this.specMatcher;
  }

  public M gt() {
    this.matcher = PathMatcher.GT;
    return this.specMatcher;
  }

  public M ge() {
    this.matcher = PathMatcher.GE;
    return this.specMatcher;
  }

  public M lt() {
    this.matcher = PathMatcher.LT;
    return this.specMatcher;
  }

  public M le() {
    this.matcher = PathMatcher.LE;
    return this.specMatcher;
  }

  public M like() {
    this.matcher = PathMatcher.LIKE;
    return this.specMatcher;
  }

  public M starting() {
    this.matcher = PathMatcher.STARTING;
    return this.specMatcher;
  }

  public M ending() {
    this.matcher = PathMatcher.ENDING;
    return this.specMatcher;
  }

  public M containing() {
    this.matcher = PathMatcher.CONTAINING;
    return this.specMatcher;
  }

  public M notStarting() {
    this.matcher = PathMatcher.NOT_STARTING;
    return this.specMatcher;
  }

  public M notEnding() {
    this.matcher = PathMatcher.NOT_ENDING;
    return this.specMatcher;
  }

  public M notContaining() {
    this.matcher = PathMatcher.NOT_CONTAINING;
    return this.specMatcher;
  }

  public M notLike() {
    this.matcher = PathMatcher.NOT_LIKE;
    return this.specMatcher;
  }

  //--------------------------------------------
  public M equal(Object value) {
    return withMatcher(value, PathMatcher.EQ);
  }

  public M notEqual(Object value) {
    return withMatcher(value, PathMatcher.NE);
  }

  public <Y extends Comparable<? super Y>> M gt(Y value) {
    return withMatcher(value, PathMatcher.GT);
  }

  public <Y extends Comparable<? super Y>> M ge(Y value) {
    return withMatcher(value, PathMatcher.GE);
  }

  public <Y extends Comparable<? super Y>> M lt(Y value) {
    return withMatcher(value, PathMatcher.LT);
  }

  public <Y extends Comparable<? super Y>> M le(Y value) {
    return withMatcher(value, PathMatcher.LE);
  }

  public <Y extends Comparable<? super Y>> M between(Y first, Y second) {
    return withMatcher(new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  public M like(String value) {
    return withMatcher(value, PathMatcher.LIKE);
  }

  public M starting(String value) {
    return withMatcher(value, PathMatcher.STARTING);
  }

  public M ending(String value) {
    return withMatcher(value, PathMatcher.ENDING);
  }

  public M containing(String value) {
    return withMatcher(value, PathMatcher.CONTAINING);
  }

  public M notStarting(String value) {
    return withMatcher(value, PathMatcher.NOT_STARTING);
  }

  public M notEnding(String value) {
    return withMatcher(value, PathMatcher.NOT_ENDING);
  }

  public M notContaining(String value) {
    return withMatcher(value, PathMatcher.NOT_CONTAINING);
  }

  public M notLike(String value) {
    return withMatcher(value, PathMatcher.NOT_LIKE);
  }

  public M in(Object... value) {
    return withMatcher(Arrays.asList(value), PathMatcher.IN);
  }

  public M in(Collection<?> value) {
    return withMatcher(value, PathMatcher.IN);
  }

  public M notIn(Object... value) {
    return withMatcher(Arrays.asList(value), PathMatcher.NOT_IN);
  }

  public M notIn(Collection<?> value) {
    return withMatcher(value, PathMatcher.NOT_IN);
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


