package top.bettercode.summer.web.exception;

import top.bettercode.summer.tools.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
public class SystemException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  /**
   * 业务错误码
   */
  private final String code;
  private final Object data;
  private static final PropertiesSource propertiesSource = PropertiesSource
      .of("error-code", "properties.error-code");

  public SystemException(String code) {
    super(propertiesSource.getOrDefault(code, code));
    this.code = code;
    this.data = null;
  }

  public SystemException(String code, Throwable cause) {
    super(propertiesSource.getOrDefault(code, code), cause);
    this.code = code;
    this.data = null;
  }

  public SystemException(String code, Object data) {
    super(propertiesSource.getOrDefault(code, code));
    this.code = code;
    this.data = data;
  }

  public SystemException(String code, String message) {
    super(message);
    this.code = code;
    this.data = null;
  }

  public SystemException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.data = null;
  }

  public SystemException(String code, String message, Object data) {
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
