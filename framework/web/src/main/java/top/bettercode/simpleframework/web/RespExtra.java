package top.bettercode.simpleframework.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class RespExtra<T> {

  private static final long serialVersionUID = -8674517568275448549L;

  private final T content;

  private Map<String, Object> extra = new HashMap<>();

  private Object extraPOJO;

  public RespExtra(T content) {
    this.content = content;
  }

  @JsonView(Object.class)
  @JsonUnwrapped
  public T getContent() {
    return content;
  }

  public RespExtra<T> extra(String key, Object value) {
    this.extra.put(key, value);
    return this;
  }

  public RespExtra<T> extraPOJO(Object POJO) {
    this.extraPOJO = POJO;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, Object> extra) {
    this.extra = extra;
  }

  @JsonView(Object.class)
  @JsonUnwrapped
  public Object getExtraPOJO() {
    return extraPOJO;
  }

  public void setExtraPOJO(Object extraPOJO) {
    this.extraPOJO = extraPOJO;
  }
}
