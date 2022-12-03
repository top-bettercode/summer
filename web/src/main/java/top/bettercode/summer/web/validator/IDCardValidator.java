package top.bettercode.summer.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IDCardValidator implements ConstraintValidator<IDCard, String> {
	// 每位加权因子
	private static final int[] power = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
			8, 4, 2 };

	// 第18位校检码
	private static final String[] verifyCode = { "1", "0", "X", "9", "8", "7", "6",
			"5", "4", "3", "2" };

	@Override
	public void initialize(IDCard annotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value == null || value.length() == 0 || IDCardUtil.validate(value);
	}

}