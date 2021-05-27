package cn.bestwu.simpleframework.exception;

import java.util.Map;

/**
 * @author Peter Wu
 */
public class IllegalUserException extends RuntimeException {

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
