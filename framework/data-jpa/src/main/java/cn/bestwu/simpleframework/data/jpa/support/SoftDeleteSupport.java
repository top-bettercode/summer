package cn.bestwu.simpleframework.data.jpa.support;

import com.querydsl.core.types.Predicate;

/**
 * @author Peter Wu
 */
public interface SoftDeleteSupport {

  void setSoftDeleted(Object entity);

  void setUnSoftDeleted(Object entity);

  boolean support();

  boolean isSoftDeleted(Object entity);

  String getPropertyName();

  Object getTrueValue();

  Object getFalseValue();

  Predicate andTruePredicate(Predicate predicate);

  Predicate andFalsePredicate(Predicate predicate);
}
