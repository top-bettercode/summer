package top.bettercode.simpleframework.web.validator;

import top.bettercode.lang.util.CharUtil;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code ChinaCell} 验证器
 *
 * @author Peter wu
 */
public class CNStringValidator implements ConstraintValidator<CNString, String> {

  public CNStringValidator() {
  }

  @Override
  public void initialize(CNString constraintAnnotation) {
  }

  public boolean isValid(String charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null || charSequence.length() == 0) {
      return true;
    }
    for (char c : charSequence.toCharArray()) {
      if (!CharUtil.isCNChar(c)) {
        return false;
      }
    }
    return true;
  }
}

