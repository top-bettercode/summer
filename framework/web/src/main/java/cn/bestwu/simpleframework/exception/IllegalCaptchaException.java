package cn.bestwu.simpleframework.exception;

/**
 * @author Peter Wu
 */
public class IllegalCaptchaException extends RuntimeException {

  private static final long serialVersionUID = 4634232939775284312L;

  public IllegalCaptchaException(String msg) {
    super(msg);
  }
}
