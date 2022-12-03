package top.bettercode.summer.tools.sms;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SmsSysException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public SmsSysException(String message) {
    super("短信平台：" + message);
  }
}
