package cn.bestwu.simpleframework.security.server;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalCaptchaException extends InternalAuthenticationServiceException {

  private static final long serialVersionUID = 4634232939775284312L;

  public IllegalCaptchaException(String msg) {
    super(msg);
  }
}
