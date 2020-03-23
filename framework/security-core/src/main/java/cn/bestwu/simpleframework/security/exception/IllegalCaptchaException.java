package cn.bestwu.simpleframework.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author Peter Wu
 */
public class IllegalCaptchaException extends AuthenticationException {

  private static final long serialVersionUID = 4634232939775284312L;

  public IllegalCaptchaException(String msg) {
    super(msg);
  }
}
