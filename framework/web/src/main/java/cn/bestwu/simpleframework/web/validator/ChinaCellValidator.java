package cn.bestwu.simpleframework.web.validator;

import cn.bestwu.lang.util.CellUtil;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code ChinaCell} 验证器
 *
 * @author Peter wu
 */
public class ChinaCellValidator implements ConstraintValidator<ChinaCell, String> {

  public ChinaCellValidator() {
  }

  @Override
  public void initialize(ChinaCell constraintAnnotation) {
  }

  public boolean isValid(String charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null || charSequence.length() == 0) {
      return true;
    }
    return CellUtil.isChinaCell(charSequence);
  }
}

