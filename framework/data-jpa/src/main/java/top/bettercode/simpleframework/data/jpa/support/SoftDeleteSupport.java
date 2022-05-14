package top.bettercode.simpleframework.data.jpa.support;

/**
 * @author Peter Wu
 */
public interface SoftDeleteSupport {

  void setSoftDeleted(Object entity);

  void setUnSoftDeleted(Object entity);

  boolean support();

  boolean isSoftDeleted(Object entity);

  boolean softDeletedSeted(Object entity);

  Class<?> getPropertyType();

  String getPropertyName();

  Object getTrueValue();

  Object getFalseValue();

}
