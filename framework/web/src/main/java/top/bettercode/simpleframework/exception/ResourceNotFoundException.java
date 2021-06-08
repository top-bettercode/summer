package top.bettercode.simpleframework.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 找不到资源
 *
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 4871067433010822786L;

  public ResourceNotFoundException() {
    this("resource.not.found");
  }

  public ResourceNotFoundException(String message) {
    this(message, null);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}