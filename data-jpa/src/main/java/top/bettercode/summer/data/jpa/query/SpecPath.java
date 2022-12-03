package top.bettercode.summer.data.jpa.query;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class SpecPath<T, M extends SpecMatcher<T, M>> implements SpecPredicate<T, M> {

  private static final Logger log = LoggerFactory.getLogger(SpecPath.class);

  private final M specMatcher;
  /**
   * name of the attribute
   */
  private final String propertyName;

  private boolean ignoreCase = false;

  private boolean ignoredPath = false;

  private Trimspec trimspec = null;

  private PathMatcher matcher = PathMatcher.EQ;

  private Object value;

  private boolean setValue = false;

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
  @Override
  public Predicate toPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
    if (this.isIgnoredPath()) {
      return null;
    }
    Path path = this.toPath(root);
    if (path == null) {
      return null;
    }
    PathMatcher matcher = this.matcher;
    Class pathJavaType = path.getJavaType();
    switch (matcher) {
      case IS_TRUE:
        return criteriaBuilder.isTrue(path);
      case IS_FALSE:
        return criteriaBuilder.isFalse(path);
      case IS_NULL:
        return criteriaBuilder.isNull(path);
      case IS_NOT_NULL:
        return criteriaBuilder.isNotNull(path);
    }
    if (pathJavaType.equals(String.class)) {
      switch (matcher) {
        case IS_EMPTY:
          return criteriaBuilder.equal(path, "");
        case IS_NOT_EMPTY:
          return criteriaBuilder.notEqual(path, "");
        case IS_NULL_OR_EMPTY:
          return criteriaBuilder.or(criteriaBuilder.isNull(path),
              criteriaBuilder.equal(path, ""));
        case IS_NOT_NULL_OR_EMPTY:
          return criteriaBuilder.and(criteriaBuilder.isNotNull(path),
              criteriaBuilder.notEqual(path, ""));
      }
    }
    if (!setValue) {
      return null;
    }
    Object value = this.value;
    if (value != null) {
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
    }

    if (pathJavaType.equals(String.class)) {
      Expression<String> stringExpression = path;
      boolean ignoreCase = this.ignoreCase;
      if (ignoreCase) {
        stringExpression = criteriaBuilder.lower(stringExpression);
        if (value instanceof String) {
          value = value.toString().toLowerCase();
        }
      }
      if (trimspec != null && value instanceof String) {
        switch (trimspec) {
          case LEADING:
            value = StringUtils.trimLeadingWhitespace((String) value);
            break;
          case TRAILING:
            value = StringUtils.trimTrailingWhitespace((String) value);
            break;
          case BOTH:
            value = StringUtils.trimWhitespace((String) value);
            break;
        }
      }

      switch (matcher) {
        case EQ:
          return criteriaBuilder.equal(stringExpression, value);
        case NE:
          return criteriaBuilder.notEqual(stringExpression, value);
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
      if (value != null) {
        switch (matcher) {
          case LIKE:
            return criteriaBuilder.like(stringExpression, (String) value);
          case NOT_LIKE:
            return criteriaBuilder.notLike(stringExpression, (String) value);
          case STARTING:
            return criteriaBuilder.like(stringExpression, starting(value));
          case NOT_STARTING:
            return criteriaBuilder.notLike(stringExpression, starting(value));
          case ENDING:
            return criteriaBuilder.like(stringExpression, ending(value));
          case NOT_ENDING:
            return criteriaBuilder.notLike(stringExpression, ending(value));
          case CONTAINING:
            return criteriaBuilder.like(stringExpression, containing(value));
          case NOT_CONTAINING:
            return criteriaBuilder.notLike(stringExpression, containing(value));
        }
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


  public boolean isSetValue() {
    return setValue;
  }

  public boolean isIgnoredPath() {
    return this.ignoredPath;
  }

  public SpecPath<T, M> setValue(Object value) {
    this.value = value;
    this.setValue = true;
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

  public M ignoreCase() {
    ignoreCase = true;
    return this.specMatcher;
  }

  public M trim(Trimspec trimspec) {
    this.trimspec = trimspec;
    return this.specMatcher;
  }

  public M trim() {
    return this.trim(Trimspec.BOTH);
  }

  public M ignoredPath() {
    ignoredPath = true;
    return this.specMatcher;
  }

  public M withMatcher(PathMatcher matcher) {
    this.matcher = matcher;
    return this.specMatcher;
  }

  public M withMatcher(Object value, PathMatcher matcher) {
    this.setValue(value);
    return this.withMatcher(matcher);
  }

  public M isTrue() {
    return this.withMatcher(PathMatcher.IS_TRUE);
  }

  public M isFalse() {
    return this.withMatcher(PathMatcher.IS_FALSE);
  }

  public M isNull() {
    return this.withMatcher(PathMatcher.IS_NULL);
  }

  public M isNotNull() {
    return this.withMatcher(PathMatcher.IS_NOT_NULL);
  }

  public M isEmpty() {
    return this.withMatcher(PathMatcher.IS_EMPTY);
  }

  public M isNotEmpty() {
    return this.withMatcher(PathMatcher.IS_NOT_EMPTY);
  }

  public M isNullOrEmpty() {
    return this.withMatcher(PathMatcher.IS_NULL_OR_EMPTY);
  }

  public M isNotNullOrEmpty() {
    return this.withMatcher(PathMatcher.IS_NOT_NULL_OR_EMPTY);
  }

  public M equal() {
    return this.withMatcher(PathMatcher.EQ);
  }

  public M notEqual() {
    return this.withMatcher(PathMatcher.NE);
  }

  public M gt() {
    return this.withMatcher(PathMatcher.GT);
  }

  public M ge() {
    return this.withMatcher(PathMatcher.GE);
  }

  public M lt() {
    return this.withMatcher(PathMatcher.LT);
  }

  public M le() {
    return this.withMatcher(PathMatcher.LE);
  }

  public M like() {
    return this.withMatcher(PathMatcher.LIKE);
  }

  public M starting() {
    return this.withMatcher(PathMatcher.STARTING);
  }

  public M ending() {
    return this.withMatcher(PathMatcher.ENDING);
  }

  public M containing() {
    return this.withMatcher(PathMatcher.CONTAINING);
  }

  public M notStarting() {
    return this.withMatcher(PathMatcher.NOT_STARTING);
  }

  public M notEnding() {
    return this.withMatcher(PathMatcher.NOT_ENDING);
  }

  public M notContaining() {
    return this.withMatcher(PathMatcher.NOT_CONTAINING);
  }

  public M notLike() {
    return this.withMatcher(PathMatcher.NOT_LIKE);
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

  @SafeVarargs
  public final <E> M in(E... value) {
    return withMatcher(Arrays.asList(value), PathMatcher.IN);
  }

  public M in(Collection<?> value) {
    return withMatcher(value, PathMatcher.IN);
  }

  @SafeVarargs
  public final <E> M notIn(E... value) {
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


