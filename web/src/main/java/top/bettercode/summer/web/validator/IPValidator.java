package top.bettercode.summer.web.validator;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code IP} 验证器
 *
 * @author Peter wu
 */
public class IPValidator implements ConstraintValidator<IP, String> {

  public IPValidator() {
  }

  @Override
  public void initialize(IP constraintAnnotation) {
  }

  public boolean isValid(String charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null || charSequence.length() == 0) {
      return true;
    }
    return isValidInet4Address(charSequence) || isValidInet6Address(charSequence);
  }

  public static boolean isValidInet4Address(String ip) {
    try {
      return Inet4Address.getByName(ip).getHostAddress().equals(ip);
    } catch (UnknownHostException ex) {
      return false;
    }
  }

  public static boolean isValidInet6Address(String ip) {
    try {
      return Inet6Address.getByName(ip).getHostAddress().equals(ip);
    } catch (UnknownHostException ex) {
      return false;
    }
  }
}

