package cn.bestwu.simpleframework.web.validator;

import com.google.common.net.InetAddresses;
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
    return InetAddresses.isInetAddress(charSequence);
  }
}

