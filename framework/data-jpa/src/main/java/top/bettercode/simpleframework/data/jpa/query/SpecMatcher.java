package top.bettercode.simpleframework.data.jpa.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.data.jpa.query.SpecPath.BetweenValue;

/**
 * @author Peter Wu
 */
public class SpecMatcher<T, M extends SpecMatcher<T, M>> implements Specification<T> {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(SpecMatcher.class);
  private final SpecMatcherMode matcherMode;
  //  private final List<String> select = new ArrayList<>();
  private final Map<String, SpecPath<T, M>> specPaths = new HashMap<>();
  private final M typed;
  private final T probe;
  private static final Set<PersistentAttributeType> ASSOCIATION_TYPES;


  //--------------------------------------------
  @SuppressWarnings("unchecked")
  protected SpecMatcher(SpecMatcherMode matcherMode, T probe) {
    this.matcherMode = matcherMode;
    this.probe = probe;
    this.typed = (M) this;
  }

  //--------------------------------------------

  static {
    ASSOCIATION_TYPES = EnumSet.of(PersistentAttributeType.MANY_TO_MANY, //
        PersistentAttributeType.MANY_TO_ONE, //
        PersistentAttributeType.ONE_TO_MANY, //
        PersistentAttributeType.ONE_TO_ONE);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    if (probe != null) {
      setSpecPathDefaultValue("", root, root.getModel(), probe, probe.getClass(),
          new PathNode("root", null, probe));
    }
    List<Predicate> predicates = new ArrayList<>();
    List<Order> orders = new ArrayList<>();
    for (SpecPath<T, M> specPath : getSpecPaths()) {
      Direction direction = specPath.getDirection();
      if (direction != null) {
        Path<?> path = getPath(specPath.getPropertyName(), root);
        if (path != null) {
          Order order = Direction.DESC.equals(direction) ? cb.desc(path) : cb.asc(path);
          orders.add(order);
        }
      }
      if (!specPath.isIgnoredPath()) {
        Predicate predicate = toPredicate(specPath, root, cb);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }
    }
//   findAll not support multiselect
//    List<String> select = specMatcher.getSelect();
//    if (!select.isEmpty()) {
//      query.multiselect(select.stream().map(root::get).collect(Collectors.toList()));
//    }
    if (!orders.isEmpty()) {
      query.orderBy(orders);
    }

    Predicate[] restrictions = predicates.toArray(new Predicate[0]);
    return getMatchMode().equals(SpecMatcherMode.ALL) ? cb.and(
        restrictions)
        : cb.or(restrictions);
  }

  @SuppressWarnings("rawtypes")
  public void setSpecPathDefaultValue(String path, Path<?> from,
      ManagedType<?> type, Object value, Class<?> probeType, PathNode currentNode) {
    DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(
        value);
    for (SingularAttribute attribute : type.getSingularAttributes()) {
      String currentPath =
          !StringUtils.hasText(path) ? attribute.getName() : path + "." + attribute.getName();

      SpecPath<T, M> specPath = specPath(currentPath);
      if (specPath.isIgnoredPath()) {
        continue;
      }

      Object attributeValue = beanWrapper.getPropertyValue(attribute.getName());

      if (attributeValue == null || "".equals(attributeValue)) {
        continue;
      }

      if (attribute.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)
          || (isAssociation(attribute) && !(from instanceof From))) {
        setSpecPathDefaultValue(currentPath, from.get(attribute.getName()),
            (ManagedType<?>) attribute.getType(), attributeValue, probeType, currentNode);
        continue;
      }

      if (isAssociation(attribute)) {
        PathNode node = currentNode.add(attribute.getName(), attributeValue);
        if (node.spansCycle()) {
          throw new InvalidDataAccessApiUsageException(
              String.format(
                  "Path '%s' from root %s must not span a cyclic property reference!\r\n%s",
                  currentPath,
                  ClassUtils.getShortName(probeType), node));
        }

        setSpecPathDefaultValue(currentPath, ((From<?, ?>) from).join(attribute.getName()),
            (ManagedType<?>) attribute.getType(), attributeValue, probeType, node);
        continue;
      }
      if (specPath.getValue() == null) {
        specPath.setValue(attributeValue);
      }
    }
  }

  private static boolean isAssociation(Attribute<?, ?> attribute) {
    return ASSOCIATION_TYPES.contains(attribute.getPersistentAttributeType());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Predicate toPredicate(SpecPath specPath, Root<?> root,
      CriteriaBuilder criteriaBuilder) {
    if (specPath.isIgnoredPath()) {
      return null;
    }
    String propertyName = specPath.getPropertyName();
    PathMatcher matcher = specPath.getMatcher();
    switch (matcher) {
      case IS_TRUE:
        Path path = getPath(propertyName, root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isTrue(path);
      case IS_FALSE:
        path = getPath(propertyName, root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isFalse(path);
      case IS_NULL:
        path = getPath(propertyName, root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isNull(path);
      case IS_NOT_NULL:
        path = getPath(propertyName, root);
        if (path == null) {
          return null;
        }
        return criteriaBuilder.isNotNull(path);
    }
    Object value = specPath.getValue();
    if (value == null || "".equals(value)) {
      return null;
    }
    Path path = getPath(propertyName, root);
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
      boolean ignoreCase = specPath.isIgnoreCase();
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
          return criteriaBuilder.like(stringExpression, value + "%");
        case ENDING:
          return criteriaBuilder.like(stringExpression, "%" + value);
        case CONTAINING:
          return criteriaBuilder.like(stringExpression, "%" + value + "%");
        case NOT_STARTING:
          return criteriaBuilder.notLike(stringExpression, value + "%");
        case NOT_ENDING:
          return criteriaBuilder.notLike(stringExpression, "%" + value);
        case NOT_CONTAINING:
          return criteriaBuilder.notLike(stringExpression, "%" + value + "%");
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

  @SuppressWarnings({"rawtypes"})
  private Path getPath(String propertyName, Root<?> root) {
    try {
      String[] split = propertyName.split("\\.");
      Path path = null;
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

  public SpecMatcherMode getMatchMode() {
    return this.matcherMode;
  }

//  public List<String> getSelect() {
//    return select;
//  }

  public Collection<SpecPath<T, M>> getSpecPaths() {
    return specPaths.values();
  }

  public SpecPath<T, M> specPath(String propertyName) {
    Assert.hasText(propertyName, "propertyName can not be blank.");
    return specPaths.computeIfAbsent(propertyName,
        s -> new SpecPath<>(typed, propertyName));
  }

//  public M select(String propertyName) {
//    select.add(propertyName);
//    return typed;
//  }

  //--------------------------------------------

  public M asc(String propertyName) {
    return specPath(propertyName).asc();
  }

  public M desc(String propertyName) {
    return specPath(propertyName).desc();
  }

  public M withMatcher(String propertyName, Object value, PathMatcher matcher) {
    return specPath(propertyName).setValue(value).withMatcher(matcher);
  }

  public M equal(String propertyName, Object value) {
    return withMatcher(propertyName, value, PathMatcher.EQ);
  }

  public M notEqual(String propertyName, Object value) {
    return withMatcher(propertyName, value, PathMatcher.NE);
  }

  public <Y extends Comparable<? super Y>> M gt(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.GT);
  }

  public <Y extends Comparable<? super Y>> M ge(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.GE);
  }

  public <Y extends Comparable<? super Y>> M lt(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.LT);
  }

  public <Y extends Comparable<? super Y>> M le(String propertyName, Y value) {
    return withMatcher(propertyName, value, PathMatcher.LE);
  }

  public <Y extends Comparable<? super Y>> M between(String propertyName, Y first,
      Y second) {
    return withMatcher(propertyName, new BetweenValue<>(first, second), PathMatcher.BETWEEN);
  }

  public M like(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.LIKE);
  }

  public M starting(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.STARTING);
  }

  public M ending(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.ENDING);
  }

  public M containing(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.CONTAINING);
  }

  public M notStarting(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_STARTING);
  }

  public M notEnding(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_ENDING);
  }

  public M notContaining(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_CONTAINING);
  }

  public M notLike(String propertyName, String value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_LIKE);
  }

  public M in(String propertyName, Object... value) {
    return withMatcher(propertyName, Arrays.asList(value), PathMatcher.IN);
  }

  public M in(String propertyName, Collection<?> value) {
    return withMatcher(propertyName, value, PathMatcher.IN);
  }


  public M notIn(String propertyName, Object... value) {
    return withMatcher(propertyName, Arrays.asList(value), PathMatcher.NOT_IN);
  }

  public M notIn(String propertyName, Collection<?> value) {
    return withMatcher(propertyName, value, PathMatcher.NOT_IN);
  }

}
