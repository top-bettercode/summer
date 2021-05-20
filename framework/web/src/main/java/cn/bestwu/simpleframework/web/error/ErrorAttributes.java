package cn.bestwu.simpleframework.web.error;

import cn.bestwu.lang.property.PropertiesSource;
import cn.bestwu.simpleframework.exception.BusinessException;
import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.validator.NoPropertyPath;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

/**
 * ErrorAttributes 错误属性
 *
 * @author Peter Wu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorAttributes extends DefaultErrorAttributes {

  private static final Logger log = LoggerFactory.getLogger(ErrorAttributes.class);

  public static final String IS_PLAIN_TEXT_ERROR = ErrorAttributes.class.getName() + ".plainText";
  private final MessageSource messageSource;
  private final List<IErrorHandler> errorHandlers;
  private final IErrorRespEntityHandler errorRespEntityHandler;
  private final String separator;
  private static final PropertiesSource propertiesSource = PropertiesSource
      .of("default-exception-handle", "exception-handle");

  public ErrorAttributes(List<IErrorHandler> errorHandlers,
      IErrorRespEntityHandler errorRespEntityHandler,
      MessageSource messageSource, String separator) {
    this.errorHandlers = errorHandlers;
    this.errorRespEntityHandler = errorRespEntityHandler;
    this.messageSource = messageSource;
    this.separator = separator;
  }

  @Override
  public Map<String, Object> getErrorAttributes(WebRequest webRequest,
      boolean includeStackTrace) {
    String statusCode = null;
    Integer httpStatusCode = null;
    String message;
    Throwable error = getError(webRequest);
    RespEntity<Object> respEntity = new RespEntity<>();
    Map<String, String> errors = new HashMap<>();

    if (error != null) {
      if (errorHandlers != null) {
        for (IErrorHandler errorHandler : errorHandlers) {
          errorHandler.handlerException(error, respEntity, errors, separator);
        }
      }

      statusCode = respEntity.getStatus();
      httpStatusCode = respEntity.getHttpStatusCode();
      message = respEntity.getMessage();

      if (includeStackTrace) {
        addStackTrace(respEntity, error);
      }

      if (error instanceof BindException) {//参数错误
        BindException er = (BindException) error;
        List<FieldError> fieldErrors = er.getFieldErrors();
        message = handleFieldError(webRequest, errors, fieldErrors);
      } else if (error instanceof MethodArgumentNotValidException) {
        BindingResult bindingResult = ((MethodArgumentNotValidException) error).getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        message = handleFieldError(webRequest, errors, fieldErrors);
      } else if (error instanceof ConversionFailedException) {
        message = getText(webRequest, "typeMismatch",
            ((ConversionFailedException) error).getValue(),
            ((ConversionFailedException) error).getTargetType());
      } else if (error instanceof ConstraintViolationException) {//数据验证
        ConstraintViolationException er = (ConstraintViolationException) error;
        Set<ConstraintViolation<?>> constraintViolations = er.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
          String property = getProperty(constraintViolation);
          String msg;
          if (constraintViolation.getConstraintDescriptor().getPayload()
              .contains(NoPropertyPath.class)) {
            msg = constraintViolation.getMessage();
          } else {
            msg = getText(webRequest, property) + separator + constraintViolation.getMessage();
          }
          errors.put(property, msg);
        }
        message = errors.values().iterator().next();
      } else if (error instanceof HttpMediaTypeNotAcceptableException) {
        message =
            "MediaType not Acceptable!Must ACCEPT:" + ((HttpMediaTypeNotAcceptableException) error)
                .getSupportedMediaTypes();
      } else if (error instanceof HttpMessageNotWritableException) {
        message = error.getMessage();
        if (message != null && message.contains("Session is closed")) {
          httpStatusCode = HttpStatus.REQUEST_TIMEOUT.value();
          message = "request.timeout";
        }
      } else if (error instanceof BusinessException) {
        statusCode = ((BusinessException) error).getCode();
        respEntity.setErrors(((BusinessException) error).getData());
      }
    } else {
      message = getMessage(webRequest);
    }

    if (!StringUtils.hasText(message) && error != null) {
      message = handleMessage(error.getClass());
      if (StringUtils.hasText(error.getMessage()) && (!StringUtils.hasText(message) || !error
          .getMessage()
          .contains("Exception"))) {
        message = error.getMessage();
      }
    }

    if (httpStatusCode == null) {
      if (error != null) {
        Class<? extends Throwable> errorClass = error.getClass();
        httpStatusCode = handleHttpStatusCode(errorClass);

        ResponseStatus responseStatus = AnnotatedElementUtils
            .findMergedAnnotation(errorClass, ResponseStatus.class);
        if (responseStatus != null) {
          if (httpStatusCode == null) {
            httpStatusCode = responseStatus.code().value();
          }
          String reason = responseStatus.reason();
          if (!StringUtils.hasText(message) && StringUtils.hasText(reason)) {
            message = reason;
          }
        }
      }
      if (httpStatusCode == null) {
        httpStatusCode = getStatus(webRequest).value();
      }
    }

    statusCode = statusCode == null ? String.valueOf(httpStatusCode) : statusCode;
    if (!StringUtils.hasText(message)) {
      if (httpStatusCode == 404) {
        message = "Page not found";
      } else {
        message = "";
      }
    }
    message = getText(webRequest, message);

    setErrorInfo(webRequest, httpStatusCode, statusCode, message, error);

    respEntity.setStatus(statusCode);
    respEntity.setMessage(message);
    if (!errors.isEmpty()) {
      respEntity.setErrors(errors);
    }
    if (errorRespEntityHandler != null) {
      return errorRespEntityHandler.handle(webRequest, respEntity).toMap();
    } else {
      return respEntity.toMap();
    }
  }

  private Integer handleHttpStatusCode(Class<? extends Throwable> throwableClass) {
    String key = throwableClass.getName() + ".code";
    String value = propertiesSource.getString(key);
    if (StringUtils.hasText(value)) {
      return Integer.parseInt(value);
    } else {
      return null;
    }
  }

  private String handleMessage(Class<? extends Throwable> throwableClass) {
    String key = throwableClass.getName() + ".message";
    return propertiesSource.getString(key);
  }


  @NotNull
  private String handleFieldError(WebRequest webRequest, Map<String, String> errors,
      List<FieldError> fieldErrors) {
    String message;
    for (FieldError fieldError : fieldErrors) {
      String defaultMessage = fieldError.getDefaultMessage();
      if (defaultMessage.contains("required type")) {
        defaultMessage = getText(webRequest, fieldError.getCode());
      }
      String regrex = "^.*threw exception; nested exception is .*: (.*)$";
      if (defaultMessage.matches(regrex)) {
        defaultMessage = defaultMessage.replaceAll(regrex, "$1");
        defaultMessage = getText(webRequest, defaultMessage);
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
          msg = getText(webRequest, field.substring(field.lastIndexOf('.') + 1)) + separator
              + defaultMessage;
        } else {
          msg = getText(webRequest, field) + separator + defaultMessage;
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

  @NotNull
  public static String getProperty(ConstraintViolation<?> constraintViolation) {
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

  public static void setErrorInfo(WebRequest request, Integer httpStatusCode,
      String statusCode,
      String message,
      Throwable error) {
    request.setAttribute("javax.servlet.error.status_code", httpStatusCode,
        RequestAttributes.SCOPE_REQUEST);
    request.setAttribute(DefaultErrorAttributes.class.getName() + ".ERROR", error,
        RequestAttributes.SCOPE_REQUEST);
    request
        .setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, message, RequestAttributes.SCOPE_REQUEST);
  }

  /**
   * 增加StackTrace
   *
   * @param respEntity respEntity
   * @param error      error
   */
  private void addStackTrace(RespEntity<Object> respEntity, Throwable error) {
    StringWriter stackTrace = new StringWriter();
    error.printStackTrace(new PrintWriter(stackTrace));
    stackTrace.flush();
    respEntity.setTrace(stackTrace.toString());
  }

  public static HttpStatus getStatus(RequestAttributes requestAttributes) {
    Integer statusCode = getAttribute(requestAttributes, WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
    if (statusCode != null) {
      try {
        return HttpStatus.valueOf(statusCode);
      } catch (Exception ignored) {
      }
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public static String getMessage(RequestAttributes requestAttributes) {
    return getAttribute(requestAttributes, WebUtils.ERROR_MESSAGE_ATTRIBUTE);
  }

  @SuppressWarnings("unchecked")
  private static <T> T getAttribute(RequestAttributes requestAttributes, String name) {
    return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
  }

  /**
   * 得到国际化信息 未找到时返回代码 code
   *
   * @param webRequest webRequest
   * @param code       模板
   * @param args       参数
   * @return 信息
   */
  private String getText(WebRequest webRequest, Object code, Object... args) {
    String codeString = String.valueOf(code);
    return messageSource.getMessage(codeString, args, codeString,
        webRequest == null ? Locale.CHINA : webRequest.getLocale());
  }
}
