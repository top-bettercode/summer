package top.bettercode.simpleframework.web.error;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import top.bettercode.simpleframework.exception.BusinessException;
import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.validator.NoPropertyPath;

/**
 * @author Peter Wu
 */
public class DefaultErrorHandler extends AbstractErrorHandler {


  public DefaultErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    String message = null;
    if (error instanceof BindException) {//参数错误
      BindException er = (BindException) error;
      List<FieldError> fieldErrors = er.getFieldErrors();
      message = handleFieldError(errors, fieldErrors, separator);
    } else if (error instanceof MethodArgumentNotValidException) {
      BindingResult bindingResult = ((MethodArgumentNotValidException) error).getBindingResult();
      List<FieldError> fieldErrors = bindingResult.getFieldErrors();
      message = handleFieldError(errors, fieldErrors, separator);
    } else if (error instanceof ConversionFailedException) {
      message = getText("typeMismatch",
          ((ConversionFailedException) error).getValue(),
          ((ConversionFailedException) error).getTargetType());
    } else if (error instanceof ConstraintViolationException) {//数据验证
      constraintViolationException((ConstraintViolationException) error, respEntity, errors,
          separator);
    } else if (error instanceof HttpMediaTypeNotAcceptableException) {
      message =
          "MediaType not Acceptable!Must ACCEPT:" + ((HttpMediaTypeNotAcceptableException) error)
              .getSupportedMediaTypes();
    } else if (error instanceof HttpMessageNotWritableException) {
      message = error.getMessage();
      if (message != null && message.contains("Session is closed")) {
        respEntity.setHttpStatusCode(HttpStatus.REQUEST_TIMEOUT.value());
        message = "request.timeout";
      }
    } else if (error instanceof BusinessException) {
      respEntity.setHttpStatusCode(HttpStatus.OK.value());
      respEntity.setStatus(((BusinessException) error).getCode());
      respEntity.setErrors(((BusinessException) error).getData());
    }

    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }


  private String handleFieldError(Map<String, String> errors,
      List<FieldError> fieldErrors, String separator) {
    String message;
    for (FieldError fieldError : fieldErrors) {
      String defaultMessage = fieldError.getDefaultMessage();
      if (defaultMessage.contains("required type")) {
        defaultMessage = getText(fieldError.getCode());
      }
      String regrex = "^.*threw exception; nested exception is .*: (.*)$";
      if (defaultMessage.matches(regrex)) {
        defaultMessage = defaultMessage.replaceAll(regrex, "$1");
        defaultMessage = getText(defaultMessage);
      }
      String field = fieldError.getField();
      String msg = null;
      if (fieldError.contains(ConstraintViolation.class)) {
        ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
        if (violation.getConstraintDescriptor().getPayload().contains(NoPropertyPath.class)) {
          msg = violation.getMessage();
        }
      }
      if (msg == null) {
        if (field.contains(".")) {
          msg = getText(field.substring(field.lastIndexOf('.') + 1)) + separator
              + defaultMessage;
        } else {
          msg = getText(field) + separator + defaultMessage;
        }
      }
      errors.put(field, msg);
    }
    message = errors.values().iterator().next();

    if (!StringUtils.hasText(message)) {
      message = "data.valid.failed";
    }
    return message;
  }

}
