package top.bettercode.simpleframework.web;

import com.google.code.kaptcha.Constants;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import top.bettercode.lang.util.TimeUtil;
import top.bettercode.simpleframework.web.kaptcha.KaptchaProperties;

/**
 * @author Peter Wu
 */
public class DefaultCaptchaServiceImpl implements ICaptchaService {

  protected final HttpSession httpSession;
  private final KaptchaProperties kaptchaProperties;

  public DefaultCaptchaServiceImpl(@Autowired(required = false) HttpSession httpSession,
      KaptchaProperties kaptchaProperties) {
    this.kaptchaProperties = kaptchaProperties;
    this.httpSession = httpSession;
  }

  @Override
  public void save(String loginId, String captcha) {
    httpSession.setAttribute(Constants.KAPTCHA_SESSION_KEY, captcha);
    httpSession.setAttribute(Constants.KAPTCHA_SESSION_DATE, new Date());
  }

  @Override
  public boolean match(String loginId, String captcha) {
    Assert.hasText(loginId,"验证码错误");
    Assert.hasText(captcha,"验证码错误");
    String kaptcha = (String) httpSession.getAttribute(Constants.KAPTCHA_SESSION_KEY);
    Date date = (Date) httpSession.getAttribute(Constants.KAPTCHA_SESSION_DATE);
    return date != null && TimeUtil.of(date).toLocalDateTime().plus(kaptchaProperties.getExpireSeconds(),
        ChronoUnit.SECONDS).isAfter(TimeUtil.now().toLocalDateTime()) && captcha.equalsIgnoreCase(kaptcha);
  }

}
