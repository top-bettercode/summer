package top.bettercode.summer.tools.sms;

/**
 * @author Peter Wu
 */
public class SmsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SmsException() {
    this("短信平台请求失败");
  }

  public SmsException(String message) {
    super(message);
  }

  public SmsException(String message, Throwable cause) {
    super(message, cause);
  }

  public SmsException(Throwable cause) {
    this("短信平台请求失败", cause);
  }

  public SmsException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
