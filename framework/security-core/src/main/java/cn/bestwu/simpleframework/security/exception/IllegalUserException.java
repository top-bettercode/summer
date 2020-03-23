package cn.bestwu.simpleframework.security.exception;

import java.util.Map;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Peter Wu
 */
public class IllegalUserException extends AuthenticationException {

  private static final long serialVersionUID = 4634232939775284312L;

  private Map<String, String> errors;

  public IllegalUserException(String msg) {
    super(msg);
  }

  public IllegalUserException(String msg, Throwable t) {
    super(msg, t);
  }

  public Map<String, String> getErrors() {
    return errors;
  }

  public void setErrors(Map<String, String> errors) {
    this.errors = errors;
  }
}
