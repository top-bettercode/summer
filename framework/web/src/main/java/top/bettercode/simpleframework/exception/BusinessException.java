package top.bettercode.simpleframework.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.OK)
public class BusinessException extends SystemException {

  private static final long serialVersionUID = 1L;

  public BusinessException(String code) {
    super(code);
  }

  public BusinessException(String code, Throwable cause) {
    super(code, cause);
  }

  public BusinessException(String code, Object data) {
    super(code, data);
  }

  public BusinessException(String code, String message) {
    super(code, message);
  }

  public BusinessException(String code, String message, Throwable cause) {
    super(code, message, cause);
  }

  public BusinessException(String code, String message, Object data) {
    super(code, message, data);
  }
}
