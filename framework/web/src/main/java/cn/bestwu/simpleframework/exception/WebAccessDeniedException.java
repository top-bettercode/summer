package cn.bestwu.simpleframework.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class WebAccessDeniedException extends RuntimeException {

  private static final long serialVersionUID = -7941819415782111951L;

  public WebAccessDeniedException() {
    this("access.denied");
  }

  public WebAccessDeniedException(String message) {
    super(message);
  }
}
