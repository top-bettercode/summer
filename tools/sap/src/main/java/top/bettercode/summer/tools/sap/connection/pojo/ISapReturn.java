package top.bettercode.summer.tools.sap.connection.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Peter Wu
 */
public interface ISapReturn {

  @JsonIgnore
  default boolean isOk() {
    return isSuccess();
  }

  @JsonIgnore
  boolean isSuccess();

  @JsonIgnore
  String getMessage();
}
