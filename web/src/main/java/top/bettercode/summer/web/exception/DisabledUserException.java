package top.bettercode.summer.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 找不到资源
 *
 * @author Peter Wu
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "disabledUser")
public class DisabledUserException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public DisabledUserException() {
    this("disabledUser");
  }

  public DisabledUserException(String message) {
    this(message, null);
  }

  public DisabledUserException(String message, Throwable cause) {
    super(message, cause);
  }
}