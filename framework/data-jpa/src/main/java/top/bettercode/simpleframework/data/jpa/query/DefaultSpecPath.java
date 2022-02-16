package top.bettercode.simpleframework.data.jpa.query;


import java.util.Arrays;
import java.util.Collection;
import org.springframework.data.domain.Sort.Direction;

/**
 * @author Peter Wu
 */
public class DefaultSpecPath<M extends SpecMatcher> implements SpecPath<M> {

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


  public DefaultSpecPath(M specMatcher, String propertyName) {
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
  public Direction getDirection() {
    return direction;
  }

  @Override
  public SpecPath<M> setValue(Object value) {
    this.value = value;
    return this;
  }

  @Override
  public M asc() {
    this.direction = Direction.ASC;
    return this.specMatcher;
  }

  @Override
  public M desc() {
    this.direction = Direction.DESC;
    return this.specMatcher;
  }

  @Override
  public M withMatcher(PathMatcher matcher) {
    this.matcher = matcher;
    return this.specMatcher;
  }

  @Override
  public M withMatcher(Object value, PathMatcher matcher) {
    this.value = value;
    this.matcher = matcher;
    return this.specMatcher;
  }

  @Override
  public M withIgnoreCase() {
    ignoreCase = true;
    return this.specMatcher;
  }


  @Override
  public M ignoredPath() {
    ignoredPath = true;
    return this.specMatcher;
  }

  @Override
  public M isTrue() {
    this.matcher = PathMatcher.IS_TRUE;
    return this.specMatcher;
  }

  @Override
  public M isFalse() {
    this.matcher = PathMatcher.IS_FALSE;
    return this.specMatcher;
  }

  @Override
  public M isNull() {
    this.matcher = PathMatcher.IS_NULL;
    return this.specMatcher;
  }

  @Override
  public M isNotNull() {
    this.matcher = PathMatcher.IS_NOT_NULL;
    return this.specMatcher;
  }

  @Override
  public M equal() {
    this.matcher = PathMatcher.EQ;
    return this.specMatcher;
  }

  @Override
  public M notEqual() {
    this.matcher = PathMatcher.NE;
    return this.specMatcher;
  }

  @Override
  public M gt() {
    this.matcher = PathMatcher.GT;
    return this.specMatcher;
  }

  @Override
  public M ge() {
    this.matcher = PathMatcher.GE;
    return this.specMatcher;
  }

  @Override
  public M lt() {
    this.matcher = PathMatcher.LT;
    return this.specMatcher;
  }

  @Override
  public M le() {
    this.matcher = PathMatcher.LE;
    return this.specMatcher;
  }

  @Override
  public M like() {
    this.matcher = PathMatcher.LIKE;
    return this.specMatcher;
  }

  @Override
  public M starting() {
    this.matcher = PathMatcher.STARTING;
    return this.specMatcher;
  }

  @Override
  public M ending() {
    this.matcher = PathMatcher.ENDING;
    return this.specMatcher;
  }

  @Override
  public M containing() {
    this.matcher = PathMatcher.CONTAINING;
    return this.specMatcher;
  }

  @Override
  public M notStarting() {
    this.matcher = PathMatcher.NOT_STARTING;
    return this.specMatcher;
  }

  @Override
  public M notEnding() {
    this.matcher = PathMatcher.NOT_ENDING;
    return this.specMatcher;
  }

  @Override
  public M notContaining() {
    this.matcher = PathMatcher.NOT_CONTAINING;
    return this.specMatcher;
  }

  @Override
  public M notLike() {
    this.matcher = PathMatcher.NOT_LIKE;
    return this.specMatcher;
  }

  //--------------------------------------------
  @Override
  public M equal(Object value) {
    return withMatcher(value, PathMatcher.EQ);
  }

  @Override
  public M notEqual(Object value) {
    return withMatcher(value, PathMatcher.NE);
  }

  @Override
  public <Y extends Comparable<? super Y>> M gt(Y value) {
    return withMatcher(value, PathMatcher.GT);
  }

  @Override
  public <Y extends Comparable<? super Y>> M ge(Y value) {
    return withMatcher(value, PathMatcher.GE);
  }

  @Override
  public <Y extends Comparable<? super Y>> M lt(Y value) {
    return withMatcher(value, PathMatcher.LT);
  }

  @Override
  public <Y extends Comparable<? super Y>> M le(Y value) {
    return withMatcher(value, PathMatcher.LE);
  }

  @Override
  public <Y extends Comparable<? super Y>> M between(Y first, Y second) {
    return withMatcher(new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  @Override
  public M like(String value) {
    return withMatcher(value, PathMatcher.LIKE);
  }

  @Override
  public M starting(String value) {
    return withMatcher(value, PathMatcher.STARTING);
  }

  @Override
  public M ending(String value) {
    return withMatcher(value, PathMatcher.ENDING);
  }

  @Override
  public M containing(String value) {
    return withMatcher(value, PathMatcher.CONTAINING);
  }

  @Override
  public M notStarting(String value) {
    return withMatcher(value, PathMatcher.NOT_STARTING);
  }

  @Override
  public M notEnding(String value) {
    return withMatcher(value, PathMatcher.NOT_ENDING);
  }

  @Override
  public M notContaining(String value) {
    return withMatcher(value, PathMatcher.NOT_CONTAINING);
  }

  @Override
  public M notLike(String value) {
    return withMatcher(value, PathMatcher.NOT_LIKE);
  }

  @Override
  public M in(Object... value) {
    return withMatcher(Arrays.asList(value), PathMatcher.IN);
  }

  @Override
  public M in(Collection<?> value) {
    return withMatcher(value, PathMatcher.IN);
  }

  @Override
  public M notIn(Object... value) {
    return withMatcher(Arrays.asList(value), PathMatcher.NOT_IN);
  }

  @Override
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


