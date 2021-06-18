package top.bettercode.simpleframework.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * 响应实体
 *
 * @author Peter Wu
 */
public interface IRespEntity {

  @JsonIgnore
  default Integer getHttpStatusCode() {
    return HttpStatus.OK.value();
  }

  @JsonIgnore
  Map<String, Object> toMap();
}
