package top.bettercode.summer.security;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalUserException extends InternalAuthenticationServiceException {

  private static final long serialVersionUID = 1L;

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
