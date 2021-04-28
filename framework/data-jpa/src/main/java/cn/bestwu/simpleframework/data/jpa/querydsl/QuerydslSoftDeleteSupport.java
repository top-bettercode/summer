package cn.bestwu.simpleframework.data.jpa.querydsl;

import cn.bestwu.simpleframework.data.jpa.config.JpaExtProperties;
import cn.bestwu.simpleframework.data.jpa.support.DefaultSoftDeleteSupport;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import org.springframework.beans.BeansException;

/**
 * @author Peter Wu
 */
public class QuerydslSoftDeleteSupport extends DefaultSoftDeleteSupport {

  private SimplePath<Object> path;

  public <T> QuerydslSoftDeleteSupport(JpaExtProperties jpaExtProperties, Class<?> domainClass,
      EntityPath<T> entityPath) throws BeansException {
    super(jpaExtProperties, domainClass);
    if (support()) {
      if (entityPath != null) {
        this.path = Expressions.path(getPropertyType(), entityPath, getPropertyName());
      }
    }
  }

  public Predicate andTruePredicate(Predicate predicate) {
    return getPredicate(predicate, getTrueValue());
  }

  public Predicate andFalsePredicate(Predicate predicate) {
    return getPredicate(predicate, getFalseValue());
  }

  protected Predicate getPredicate(Predicate predicate, Object value) {
    if (predicate == null) {
      return path.eq(value);
    } else {
      return path.eq(value).and(predicate);
    }
  }
}
