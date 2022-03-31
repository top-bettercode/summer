package top.bettercode.simpleframework.data.jpa.query;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.Assert;

/**
 * @author Peter Wu
 */
public class SpecPath<T, M extends SpecMatcher<T, M>> {

  private static final Logger log = LoggerFactory.getLogger(SpecPath.class);

  private final M specMatcher;
  /**
   * name of the attribute
   */
  private final String propertyName;

  private boolean ignoreCase = false;

  private boolean ignoredPath = false;

  private PathMatcher matcher = PathMatcher.EQ;

  private Object value;

  public SpecPath(M specMatcher, String propertyName) {
    this.specMatcher = specMatcher;
    this.propertyName = propertyName;
  }

  //--------------------------------------------

  public static String containing(Object value) {
    return "%" + value + "%";
  }

  public static String ending(Object value) {
    return "%" + value;
  }

  public static String starting(Object value) {
    return value + "%";
  }

  public static <T> Path<?> toPath(Root<T> root, String propertyName) {
    try {
      String[] split = propertyName.split("\\.");
      Path<?> path = null;
      for (String s : split) {
        path = path == null ? root.get(s) : path.get(s);
      }
      return path;
    } catch (IllegalArgumentException e) {
      if (log.isDebugEnabled()) {
        log.debug(e.getMessage());
      }
      return null;
    }
  }
  //--------------------------------------------

  public Path<?> toPath(Root<T> root) {
    return toPath(root, this.propertyName);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public Predicate toPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
    if (this.isIgnoredPath()) {
      return null;
    }
    PathMatcher matcher = this.getMatcher();
    switch (matcher) {
      case IS_TRUE:
        Path path = this.toPath(root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isTrue(path);
      case IS_FALSE:
        path = this.toPath(root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isFalse(path);
      case IS_NULL:
        path = this.toPath(root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isNull(path);
      case IS_NOT_NULL:
        path = this.toPath(root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isNotNull(path);
      case IS_EMPTY:
        path = this.toPath(root);
        if (path == null || !path.getJavaType().equals(String.class)) {
          return null;
        }
        Expression<String> stringExpression = path;
        return criteriaBuilder.equal(stringExpression, "");
      case IS_NOT_EMPTY:
        path = this.toPath(root);
        if (path == null || !path.getJavaType().equals(String.class)) {
          return null;
        }
        stringExpression = path;
        return criteriaBuilder.notEqual(stringExpression, "");
      case IS_NULL_OR_EMPTY:
        path = this.toPath(root);
        if (path == null || !path.getJavaType().equals(String.class)) {
          return null;
        }
        stringExpression = path;
        return criteriaBuilder.or(criteriaBuilder.isNull(path),
            criteriaBuilder.equal(stringExpression, ""));
      case IS_NOT_NULL_OR_EMPTY:
        path = this.toPath(root);
        if (path == null || !path.getJavaType().equals(String.class)) {
          return null;
        }
        stringExpression = path;
        return criteriaBuilder.and(criteriaBuilder.isNotNull(path),
            criteriaBuilder.notEqual(stringExpression, ""));
    }
    Object value = this.getValue();
    if (value == null || "".equals(value)) {
      return null;
    }
    Path path = this.toPath(root);
    if (path == null) {
      return null;
    }
    switch (matcher) {
      case BETWEEN:
        Assert.isTrue(value instanceof SpecPath.BetweenValue,
            "BETWEEN matcher with wrong value");
        BetweenValue betweenValue = (BetweenValue) value;
        return criteriaBuilder.between(path, betweenValue.getFirst(),
            betweenValue.getSecond());
      case GT:
        return criteriaBuilder.greaterThan(path, (Comparable) value);
      case GE:
        return criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) value);
      case LT:
        return criteriaBuilder.lessThan(path, (Comparable) value);
      case LE:
        return criteriaBuilder.lessThanOrEqualTo(path, (Comparable) value);
    }
    if (path.getJavaType().equals(String.class)) {
      Expression<String> stringExpression = path;
      boolean ignoreCase = this.isIgnoreCase();
      if (ignoreCase) {
        stringExpression = criteriaBuilder.lower(stringExpression);
        if (value instanceof String) {
          value = value.toString().toLowerCase();
        }
      }
      switch (matcher) {
        case EQ:
          return criteriaBuilder.equal(stringExpression, value);
        case NE:
          return criteriaBuilder.notEqual(stringExpression, value);
        case LIKE:
          return criteriaBuilder.like(stringExpression, (String) value);
        case STARTING:
          return criteriaBuilder.like(stringExpression, starting(value));
        case ENDING:
          return criteriaBuilder.like(stringExpression, ending(value));
        case CONTAINING:
          return criteriaBuilder.like(stringExpression, containing(value));
        case NOT_STARTING:
          return criteriaBuilder.notLike(stringExpression, starting(value));
        case NOT_ENDING:
          return criteriaBuilder.notLike(stringExpression, ending(value));
        case NOT_CONTAINING:
          return criteriaBuilder.notLike(stringExpression, containing(value));
        case NOT_LIKE:
          return criteriaBuilder.notLike(stringExpression, (String) value);
        case IN:
          Assert.isTrue(value instanceof Collection, "IN matcher with wrong value");
          List<String> collect = ((Collection<?>) value).stream()
              .map(s -> ignoreCase ? s.toString().toLowerCase() : s.toString())
              .collect(Collectors.toList());
          return stringExpression.in(collect);
        case NOT_IN:
          Assert.isTrue(value instanceof Collection, "IN matcher with wrong value");
          List<String> notInCollect = ((Collection<?>) value).stream()
              .map(s -> ignoreCase ? s.toString().toLowerCase() : s.toString())
              .collect(Collectors.toList());
          return criteriaBuilder.not(stringExpression.in(notInCollect));
      }
    } else {
      switch (matcher) {
        case EQ:
          return criteriaBuilder.equal(path, value);
        case NE:
          return criteriaBuilder.notEqual(path, value);
        case IN:
          Assert.isTrue(value instanceof Collection, "IN matcher with wrong value");
          Collection<?> collect = ((Collection<?>) value);
          return path.in(collect);
        case NOT_IN:
          Assert.isTrue(value instanceof Collection, "IN matcher with wrong value");
          Collection<?> notInCollect = ((Collection<?>) value);
          return criteriaBuilder.not(path.in(notInCollect));
      }
    }
    return null;
  }

  //--------------------------------------------

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

  public SpecPath<T, M> setValue(Object value) {
    this.value = value;
    return this;
  }

  public M sortBy(Direction direction) {
    this.specMatcher.sortBy(direction, this.propertyName);
    return this.specMatcher;
  }

  public M asc() {
    return this.sortBy(Direction.ASC);
  }

  public M desc() {
    return this.sortBy(Direction.DESC);
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

  public M isEmpty() {
    this.matcher = PathMatcher.IS_EMPTY;
    return this.specMatcher;
  }

  public M isNotEmpty() {
    this.matcher = PathMatcher.IS_NOT_EMPTY;
    return this.specMatcher;
  }

  public M isNullOrEmpty() {
    this.matcher = PathMatcher.IS_NULL_OR_EMPTY;
    return this.specMatcher;
  }

  public M isNotNullOrEmpty() {
    this.matcher = PathMatcher.IS_NOT_NULL_OR_EMPTY;
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


