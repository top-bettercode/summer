package cn.bestwu.simpleframework.web;

import cn.bestwu.simpleframework.exception.BusinessException;
import cn.bestwu.simpleframework.exception.ResourceNotFoundException;
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
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.util.WebUtils;

/**
 * ErrorAttributes 错误属性
 *
 * @author Peter Wu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorAttributes extends DefaultErrorAttributes {

  @Autowired
  private MessageSource messageSource;
  private List<IErrorHandler> errorHandlers;

  public ErrorAttributes(List<IErrorHandler> errorHandlers) {
    this.errorHandlers = errorHandlers;
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
      message = error.getMessage();
      if (errorHandlers != null) {
        for (IErrorHandler errorHandler : errorHandlers) {
          errorHandler.handlerException(error, respEntity, errors);
        }
      }

      httpStatusCode = respEntity.getHttpStatusCode();
      statusCode = respEntity.getStatus();
      if (StringUtils.hasText(respEntity.getMessage())) {
        message = respEntity.getMessage();
      }

      if (includeStackTrace) {
        addStackTrace(respEntity, error);
      }

      if (error instanceof ResourceNotFoundException) {
        httpStatusCode = HttpStatus.NOT_FOUND.value();
        if (!StringUtils.hasText(message)) {
          message = "resource.not.found";
        }
      } else if (error instanceof HttpRequestMethodNotSupportedException) {
        httpStatusCode = HttpStatus.METHOD_NOT_ALLOWED.value();
        if (!StringUtils.hasText(message)) {
          message = "method.not.allowed";
        }
      } else if (error instanceof BindException) {//参数错误
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
        BindException er = (BindException) error;
        List<FieldError> fieldErrors = er.getFieldErrors();
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
          String msg;
          if (field.contains(".")) {
            msg = getText(webRequest, field.substring(field.lastIndexOf('.') + 1)) + ": "
                + defaultMessage;
          } else {
            msg = getText(webRequest, field) + ": " + defaultMessage;
          }
          errors.put(field, msg);

        }
        message = errors.values().iterator().next();

        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (error instanceof IllegalArgumentException) {
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (error instanceof MethodArgumentTypeMismatchException) {
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
        message = "typeMismatch";
      } else if (error instanceof MissingServletRequestParameterException) {
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
      } else if (error instanceof ConversionFailedException) {
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
        message = getText(webRequest, "typeMismatch",
            ((ConversionFailedException) error).getValue(),
            ((ConversionFailedException) error).getTargetType());
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (error instanceof ConstraintViolationException) {//数据验证
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();

        ConstraintViolationException er = (ConstraintViolationException) error;
        Set<ConstraintViolation<?>> constraintViolations = er.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
          String property = getProperty(constraintViolation);
          String msg;
          if (constraintViolation.getConstraintDescriptor().getPayload()
              .contains(NoPropertyPath.class)) {
            msg = constraintViolation.getMessage();
          } else {
            msg = getText(webRequest, property) + ": " + constraintViolation.getMessage();
          }
          errors.put(property, msg);
        }
        message = errors.values().iterator().next();

        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (error instanceof HttpMediaTypeNotAcceptableException) {
        httpStatusCode = HttpStatus.NOT_ACCEPTABLE.value();
        message =
            "MediaType not Acceptable!Must ACCEPT:" + ((HttpMediaTypeNotAcceptableException) error)
                .getSupportedMediaTypes();
      } else if (error instanceof IllegalStateException) {
        httpStatusCode = HttpStatus.CONFLICT.value();
      } else if (error instanceof MultipartException) {
        httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
        message = "upload.fail";
      } else if (error instanceof NullPointerException) {
        message = "NullPointerException";
      } else if (error instanceof HttpMessageNotWritableException) {
        if (message.contains("Session is closed")) {
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

    if (httpStatusCode == null) {
      if (error != null) {
        ResponseStatus responseStatus = AnnotatedElementUtils
            .findMergedAnnotation(error.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
          httpStatusCode = responseStatus.code().value();
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

    return respEntity.toMap();
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
  private void addStackTrace(RespEntity respEntity, Throwable error) {
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
