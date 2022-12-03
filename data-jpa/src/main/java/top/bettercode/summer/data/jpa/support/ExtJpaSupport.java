package top.bettercode.summer.data.jpa.support;

/**
 * @author Peter Wu
 */
public interface ExtJpaSupport {

  void setSoftDeleted(Object entity);

  void setUnSoftDeleted(Object entity);

  boolean supportSoftDeleted();

  boolean isSoftDeleted(Object entity);

  boolean softDeletedSeted(Object entity);

  Class<?> getSoftDeletedPropertyType();

  String getSoftDeletedPropertyName();

  String getLastModifiedDatePropertyName();

  String getVersionPropertyName();

  Object getLastModifiedDateNowValue();

  Object getVersionIncValue();

  Object getSoftDeletedTrueValue();

  Object getSoftDeletedFalseValue();

}
