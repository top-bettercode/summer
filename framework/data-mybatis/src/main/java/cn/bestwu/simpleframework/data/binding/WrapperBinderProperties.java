package cn.bestwu.simpleframework.data.binding;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("app.wrapper-binder")
public class WrapperBinderProperties {

  private String isAscParameter = "sort";
  private String defaultIsAsc = "asc";
  private String orderByParameter = "orderBy";
  private String groupByParameter = "groupBy";
  private String isNullParameter = "isNull";
  private String isNotNullParameter = "isNotNull";

  //--------------------------------------------

  public String getIsAscParameter() {
    return isAscParameter;
  }

  public void setIsAscParameter(String isAscParameter) {
    this.isAscParameter = isAscParameter;
  }

  public String getDefaultIsAsc() {
    return defaultIsAsc;
  }

  public void setDefaultIsAsc(String defaultIsAsc) {
    this.defaultIsAsc = defaultIsAsc;
  }

  public String getOrderByParameter() {
    return orderByParameter;
  }

  public void setOrderByParameter(String orderByParameter) {
    this.orderByParameter = orderByParameter;
  }

  public String getGroupByParameter() {
    return groupByParameter;
  }

  public void setGroupByParameter(String groupByParameter) {
    this.groupByParameter = groupByParameter;
  }

  public String getIsNullParameter() {
    return isNullParameter;
  }

  public void setIsNullParameter(String isNullParameter) {
    this.isNullParameter = isNullParameter;
  }

  public String getIsNotNullParameter() {
    return isNotNullParameter;
  }

  public void setIsNotNullParameter(String isNotNullParameter) {
    this.isNotNullParameter = isNotNullParameter;
  }
}
