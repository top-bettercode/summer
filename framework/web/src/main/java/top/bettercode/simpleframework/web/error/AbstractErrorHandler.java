package top.bettercode.simpleframework.web.error;

import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.validator.NoPropertyPath;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

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


  public String getProperty(ConstraintViolation<?> constraintViolation) {
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

  protected void constraintViolationException(ConstraintViolationException error,
      RespEntity<?> respEntity, Map<String, String> errors,
      String separator) {
    String message;
    respEntity.setHttpStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    Set<ConstraintViolation<?>> constraintViolations = error.getConstraintViolations();
    for (ConstraintViolation<?> constraintViolation : constraintViolations) {
      String property = getProperty(constraintViolation);
      String msg;
      if (constraintViolation.getConstraintDescriptor().getPayload()
          .contains(NoPropertyPath.class)) {
        msg = constraintViolation.getMessage();
      } else {
        msg = getText(property) + separator + constraintViolation.getMessage();
      }
      errors.put(property, msg);
    }
    message = errors.values().iterator().next();
    if (!StringUtils.hasText(message)) {
      message = "data.valid.failed";
    }
    respEntity.setMessage(message);
  }

}
