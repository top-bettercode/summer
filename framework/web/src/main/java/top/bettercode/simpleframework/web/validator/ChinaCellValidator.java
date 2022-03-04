package top.bettercode.simpleframework.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import top.bettercode.lang.util.CellUtil;

/**
 * {@code ChinaCell} 验证器
 *
 * @author Peter wu
 */
public class ChinaCellValidator implements ConstraintValidator<ChinaCell, String> {

  private CellUtil.Model model;

  public ChinaCellValidator() {
  }

  @Override
  public void initialize(ChinaCell constraintAnnotation) {
    model = constraintAnnotation.model();
  }

  public boolean isValid(String charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null || charSequence.length() == 0) {
      return true;
    }
    switch (model) {
      case ALL:
        return CellUtil.isChinaCell(charSequence);
      case MOBILE:
        return CellUtil.isChinaMobile(charSequence);
      case UNICOM:
        return CellUtil.isChinaUnicom(charSequence);
      case TELECOM:
        return CellUtil.isChinaTelecom(charSequence);
      case VNO:
        return CellUtil.isChinaVNO(charSequence);
      default:
        return CellUtil.isSimpleCell(charSequence);
    }
  }
}

