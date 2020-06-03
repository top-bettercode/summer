package cn.bestwu.simpleframework.web.validator;

import cn.bestwu.lang.util.IPAddressUtil;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code IPv4} 验证器
 *
 * @author Peter wu
 */
public class IPv4Validator implements ConstraintValidator<IPv4, String> {

  public IPv4Validator() {
  }

  @Override
  public void initialize(IPv4 constraintAnnotation) {
  }

  public boolean isValid(String charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null || charSequence.length() == 0) {
      return true;
    }
    return IPAddressUtil.isIPv4(charSequence);
  }
}

