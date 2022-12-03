package top.bettercode.summer.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import top.bettercode.summer.web.exception.SystemException;

/**
 * @author Peter Wu
 */
public class RespEntity<T> implements IRespEntity, Serializable {

  private static final long serialVersionUID = 1L;
  public static final String KEY_STATUS = "status";
  public static final String KEY_MESSAGE = "message";
  public static final String KEY_DATA = "data";
  public static final String KEY_TRACE = "trace";
  public static final String KEY_ERRORS = "errors";

  Integer httpStatusCode;

  @JsonView(Object.class)
  @JsonProperty(KEY_STATUS)
  private String status;

  @JsonProperty(KEY_MESSAGE)
  @JsonView(Object.class)
  private String message;

  @JsonProperty(KEY_TRACE)
  @JsonView(Object.class)
  private String trace;

  @JsonProperty(KEY_ERRORS)
  @JsonView(Object.class)
  private Object errors;

  @JsonProperty(KEY_DATA)
  @JsonView(Object.class)
  private T data;

  public RespEntity() {
  }

  public RespEntity(String status, String message) {
    this.status = status;
    this.message = message;
  }

  public RespEntity(T data) {
    this.status = String.valueOf(HttpStatus.OK.value());
    this.message = "";
    this.data = data;
  }

  @Override
  public Integer getHttpStatusCode() {
    return httpStatusCode;
  }

  public void setHttpStatusCode(Integer httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
    this.status = String.valueOf(httpStatusCode);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getTrace() {
    return trace;
  }

  public void setTrace(String trace) {
    this.trace = trace;
  }

  public Object getErrors() {
    return errors;
  }

  public void setErrors(Object errors) {
    this.errors = errors;
  }

  //--------------------------------------------

  @JsonIgnore
  public boolean isOk() {
    return String.valueOf(HttpStatus.OK.value()).equals(status);
  }

  public static void assertOk(RespEntity<?> respEntity) {
    if (!respEntity.isOk()) {
      throw new SystemException(respEntity.getStatus(), respEntity.getMessage());
    }
  }

  public static void assertOk(RespEntity<?> respEntity, String message) {
    if (!respEntity.isOk()) {
      throw new SystemException(respEntity.getStatus(), message);
    }
  }

  @JsonIgnore
  public static <T> RespEntity<T> of(String status, String message) {
    return new RespEntity<>(status, message);
  }

  @JsonIgnore
  public static <T> RespEntity<T> ok() {
    return new RespEntity<>(null);
  }

  @JsonIgnore
  public static <T> RespEntity<T> ok(T data) {
    return new RespEntity<>(data);
  }

  @JsonIgnore
  public static <T> RespEntity<T> fail() {
    return fail("");
  }

  @JsonIgnore
  public static <T> RespEntity<T> fail(String message) {
    return fail(message, null);
  }

  @JsonIgnore
  public static <T> RespEntity<T> fail(String message, Object errors) {
    return fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), message, errors);
  }

  @JsonIgnore
  public static <T> RespEntity<T> fail(String status, String message, Object errors) {
    RespEntity<T> respEntity = new RespEntity<>(status, message);
    respEntity.setErrors(errors);
    return respEntity;
  }


  @Override
  public Map<String, Object> toMap() {
    RespEntityMap map = new RespEntityMap();
    map.put(KEY_STATUS, status);
    map.put(KEY_MESSAGE, message);
    map.put(KEY_DATA, data);
    map.put(KEY_TRACE, trace);
    map.put(KEY_ERRORS, errors);
    return map;
  }

  public static class RespEntityMap extends HashMap<String, Object> implements IRespEntity {

    private static final long serialVersionUID = 1L;

    @Override
    public Integer getHttpStatusCode() {
      try {
        return HttpStatus.valueOf(Integer.parseInt((String) this.get(KEY_STATUS))).value();
      } catch (Exception e) {
        return HttpStatus.OK.value();
      }
    }

    @Override
    public Map<String, Object> toMap() {
      return this;
    }
  }
}
