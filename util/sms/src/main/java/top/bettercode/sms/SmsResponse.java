package top.bettercode.sms;

/**
 * @author Peter Wu
 */
public interface SmsResponse {

  /**
   * @return 是否成功
   */
  boolean isOk();

  /**
   * @return 响应消息
   */
  String getMessage();
}
