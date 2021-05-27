package cn.bestwu.simpleframework.web.error;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.context.MessageSource;

/**
 * @author Peter Wu
 */
public abstract class AbstractErrorHandler implements IErrorHandler {

  private final MessageSource messageSource;
  private final HttpServletRequest request;

  public AbstractErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    this.messageSource = messageSource;
    this.request = request;
  }

  @Override
  public String getText(Object code, Object... args) {
    String codeString = String.valueOf(code);
    return messageSource.getMessage(codeString, args, codeString,
        request == null ? Locale.CHINA : request.getLocale());
  }


  public  String getProperty(ConstraintViolation<?> constraintViolation) {
    Path propertyPath = constraintViolation.getPropertyPath();
    String property = propertyPath.toString();
    if (propertyPath instanceof PathImpl) {
      property = ((PathImpl) propertyPath).getLeafNode().getName();
    }
    if (property.contains(".")) {
      property = property.substring(property.lastIndexOf('.') + 1);
    }
    return property;
  }

}
