package top.bettercode.simpleframework.data.jpa.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.data.jpa.query.DefaultSpecPath.BetweenValue;
import top.bettercode.simpleframework.data.jpa.query.SpecMatcher.SpecMatcherMode;

/**
 * @author Peter Wu
 */
public class MatcherSpecification<T> implements Specification<T> {

  private static final long serialVersionUID = 1L;
  private final SpecMatcher specMatcher;
  private final T probe;
  private static final Set<PersistentAttributeType> ASSOCIATION_TYPES;

  static {
    ASSOCIATION_TYPES = EnumSet.of(PersistentAttributeType.MANY_TO_MANY, //
        PersistentAttributeType.MANY_TO_ONE, //
        PersistentAttributeType.ONE_TO_MANY, //
        PersistentAttributeType.ONE_TO_ONE);
  }

  public MatcherSpecification(SpecMatcher specMatcher) {
    this.specMatcher = specMatcher;
    this.probe = null;
  }

  public MatcherSpecification(SpecMatcher specMatcher, T probe) {
    this.specMatcher = specMatcher;
    this.probe = probe;
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder) {
    if (probe != null) {
      setSpecPathDefaultValue("", root, root.getModel(), probe, probe.getClass(),
          new PathNode("root", null, probe));
    }
    List<Predicate> predicates = new ArrayList<>();
    List<Order> orders = new ArrayList<>();
    for (SpecPath<?> specPath : specMatcher.getSpecPaths()) {
      Direction direction = specPath.getDirection();
      if (direction != null) {
        Order order = Direction.DESC.equals(direction) ? criteriaBuilder.desc(
            root.get(specPath.getPropertyName()))
            : criteriaBuilder.asc(root.get(specPath.getPropertyName()));
        orders.add(order);
      }
      if (!specPath.isIgnoredPath()) {
        Predicate predicate = toPredicate(specPath, root, criteriaBuilder);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }
    }
    query.orderBy(orders);
    Predicate[] restrictions = predicates.toArray(new Predicate[0]);
    return specMatcher.getMatchMode().equals(SpecMatcherMode.ALL) ? criteriaBuilder.and(
        restrictions)
        : criteriaBuilder.or(restrictions);
  }

  @SuppressWarnings("rawtypes")
  public void setSpecPathDefaultValue(String path, Path<?> from,
      ManagedType<?> type, Object value, Class<?> probeType, PathNode currentNode) {
    DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(
        value);
    for (SingularAttribute attribute : type.getSingularAttributes()) {
      String currentPath =
          !StringUtils.hasText(path) ? attribute.getName() : path + "." + attribute.getName();

      SpecPath<?> specPath = specMatcher.specPath(currentPath);
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
    SingularAttribute attribute = root.getModel().getSingularAttribute(specPath.getPropertyName());
    PathMatcher matcher = specPath.getMatcher();
    Path path = root.get(attribute);
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
    Object value = specPath.getValue();
    if (value == null || "".equals(value)) {
      return null;
    }
    switch (matcher) {
      case BETWEEN:
        Assert.isTrue(value instanceof DefaultSpecPath.BetweenValue,
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
    if (attribute.getJavaType().equals(String.class)) {
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

  private static class PathNode {

    String name;
    @Nullable
    PathNode parent;
    List<PathNode> siblings = new ArrayList<>();
    @Nullable
    Object value;

    PathNode(String edge, @Nullable PathNode parent, @Nullable Object value) {

      this.name = edge;
      this.parent = parent;
      this.value = value;
    }

    PathNode add(String attribute, @Nullable Object value) {

      PathNode node = new PathNode(attribute, this, value);
      siblings.add(node);
      return node;
    }

    boolean spansCycle() {

      if (value == null) {
        return false;
      }

      String identityHex = ObjectUtils.getIdentityHexString(value);
      PathNode current = parent;

      while (current != null) {

        if (current.value != null && ObjectUtils.getIdentityHexString(current.value)
            .equals(identityHex)) {
          return true;
        }
        current = current.parent;
      }

      return false;
    }

    @Override
    public String toString() {

      StringBuilder sb = new StringBuilder();
      if (parent != null) {
        sb.append(parent);
        sb.append(" -");
        sb.append(name);
        sb.append("-> ");
      }

      sb.append("[{ ");
      sb.append(ObjectUtils.nullSafeToString(value));
      sb.append(" }]");
      return sb.toString();
    }
  }

}
