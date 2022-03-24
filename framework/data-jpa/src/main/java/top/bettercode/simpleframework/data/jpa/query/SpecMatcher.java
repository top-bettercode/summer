package top.bettercode.simpleframework.data.jpa.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
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

  private final SpecMatcherMode matcherMode;
  private final Map<String, SpecPath<T, M>> specPaths = new LinkedHashMap<>();
  private final List<Sort.Order> orders = new ArrayList<>();
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
    for (SpecPath<T, M> specPath : getSpecPaths()) {
      if (!specPath.isIgnoredPath()) {
        Predicate predicate = specPath.toPredicate(root, cb);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }
    }
    if (!this.orders.isEmpty()) {
      List<Order> orders = this.orders.stream().map(o -> {
        Path<Object> path = root.get(o.getProperty());
        return o.getDirection().isDescending() ? cb.desc(path) : cb.asc(path);
      }).collect(Collectors.toList());
      query.orderBy(orders);
    }

    Predicate[] restrictions = predicates.toArray(new Predicate[0]);
    return getMatchMode().equals(SpecMatcherMode.ALL) ? cb.and(restrictions) : cb.or(restrictions);
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

  //--------------------------------------------

  public SpecMatcherMode getMatchMode() {
    return this.matcherMode;
  }

  public Collection<SpecPath<T, M>> getSpecPaths() {
    return specPaths.values();
  }

  public SpecPath<T, M> specPath(String propertyName) {
    Assert.hasText(propertyName, "propertyName can not be blank.");
    return specPaths.computeIfAbsent(propertyName,
        s -> new SpecPath<>(typed, propertyName));
  }

  //--------------------------------------------

  public M sortBy(Direction direction, String... propertyName) {
    this.orders.addAll(Arrays.stream(propertyName)//
        .map(it -> new Sort.Order(direction, it))//
        .collect(Collectors.toList()));
    return this.typed;
  }

  public M asc(String... propertyName) {
    return this.sortBy(Direction.ASC, propertyName);
  }

  public M desc(String... propertyName) {
    return this.sortBy(Direction.DESC, propertyName);
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
