package cn.bestwu.simpleframework.web.validator;

import cn.bestwu.lang.util.IPAddressUtil;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code IPv6} 验证器
 *
 * @author Peter wu
 */
public class IPv6Validator implements ConstraintValidator<IPv6, String> {

  public IPv6Validator() {
  }

  @Override
  public void initialize(IPv6 constraintAnnotation) {
  }

  public boolean isValid(String charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null || charSequence.length() == 0) {
      return true;
    }
    return IPAddressUtil.isIPv6(charSequence);
  }
}

