package top.bettercode.simpleframework.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.bettercode.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

  private static final long serialVersionUID = -7941819415782111951L;
  /**
   * 业务错误码
   */
  private final String code;
  private final Object data;
  private static final PropertiesSource propertiesSource = PropertiesSource
      .of("error-code", "properties.error-code");

  public BusinessException(String code) {
    super(propertiesSource.getOrDefault(code, code));
    this.code = code;
    this.data = null;
  }

  public BusinessException(String code, Throwable cause) {
    super(propertiesSource.getOrDefault(code, code), cause);
    this.code = code;
    this.data = null;
  }

  public BusinessException(String code, Object data) {
    super(propertiesSource.getOrDefault(code, code));
    this.code = code;
    this.data = data;
  }

  public BusinessException(String code, String message) {
    super(message);
    this.code = code;
    this.data = null;
  }

  public BusinessException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.data = null;
  }

  public BusinessException(String code, String message, Object data) {
    super(message);
    this.code = code;
    this.data = data;
  }

  public String getCode() {
    return code;
  }

  public Object getData() {
    return data;
  }
}
