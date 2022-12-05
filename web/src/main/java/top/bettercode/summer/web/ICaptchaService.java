package top.bettercode.summer.web;

/**
 * @author Peter Wu
 */
public interface ICaptchaService {

  /**
   * @param loginId 客户端ID
   * @param text    客户端验证码
   */
  void save(String loginId, String text);

  /**
   * @param loginId 客户端ID
   * @param text    客户端验证码
   * @return 是否匹配
   */
  boolean match(String loginId, String text);
}
