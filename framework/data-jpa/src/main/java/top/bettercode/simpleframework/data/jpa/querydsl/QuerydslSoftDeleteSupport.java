package top.bettercode.simpleframework.data.jpa.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import org.springframework.beans.BeansException;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;
import top.bettercode.simpleframework.data.jpa.support.DefaultExtJpaSupport;

/**
 * @author Peter Wu
 */
public class QuerydslSoftDeleteSupport extends DefaultExtJpaSupport {

  private SimplePath<Object> path;

  public <T> QuerydslSoftDeleteSupport(JpaExtProperties jpaExtProperties, Class<?> domainClass,
      EntityPath<T> entityPath) throws BeansException {
    super(jpaExtProperties, domainClass);
    if (supportSoftDeleted()) {
      if (entityPath != null) {
        this.path = Expressions.path(getSoftDeletedPropertyType(), entityPath, getSoftDeletedPropertyName());
      }
    }
  }

  public Predicate andTruePredicate(Predicate predicate) {
    return getPredicate(predicate, getSoftDeletedTrueValue());
  }

  public Predicate andFalsePredicate(Predicate predicate) {
    return getPredicate(predicate, getSoftDeletedFalseValue());
  }

  protected Predicate getPredicate(Predicate predicate, Object value) {
    if (predicate == null) {
      return path.eq(value);
    } else {
      return path.eq(value).and(predicate);
    }
  }
}
