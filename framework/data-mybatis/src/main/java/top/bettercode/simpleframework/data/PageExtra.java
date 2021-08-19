package top.bettercode.simpleframework.data;

import com.baomidou.mybatisplus.plugins.Page;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class PageExtra<T> extends Page<T> {

  private static final long serialVersionUID = -8674517568275448549L;

  @JsonAnyGetter
  private Map<String, Object> extra = new HashMap<>();

  @JsonView(Object.class)
  @JsonUnwrapped
  private Object extraPOJO;

  public PageExtra<T> extra(String key, Object value) {
    this.extra.put(key, value);
    return this;
  }

  public PageExtra<T> extraPOJO(Object POJO) {
    this.extraPOJO = POJO;
    return this;
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, Object> extra) {
    this.extra = extra;
  }

  public Object getExtraPOJO() {
    return extraPOJO;
  }

  public void setExtraPOJO(Object extraPOJO) {
    this.extraPOJO = extraPOJO;
  }
}
