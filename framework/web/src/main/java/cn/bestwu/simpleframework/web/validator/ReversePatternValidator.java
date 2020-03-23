package cn.bestwu.simpleframework.web.validator;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * {@code ReversePattern} 的验证器
 *
 * @author Peter Wu
 */
public class ReversePatternValidator implements ConstraintValidator<ReversePattern, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private java.util.regex.Pattern pattern;

	public void initialize(ReversePattern parameters) {
		ReversePattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for (ReversePattern.Flag flag : flags) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			pattern = java.util.regex.Pattern.compile(parameters.regexp(), intFlag);
		} catch (PatternSyntaxException e) {
			throw LOG.getInvalidRegularExpressionException(e);
		}
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null|| value.length() == 0) {
			return true;
		}
		Matcher m = pattern.matcher(value);
		return !m.matches();
	}
}
