package cn.bestwu.simpleframework.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

/**
 * 响应实体
 *
 * @author Peter Wu
 */
public interface IRespEntity {

  @JsonIgnore
  Map<String, Object> toMap();
}
