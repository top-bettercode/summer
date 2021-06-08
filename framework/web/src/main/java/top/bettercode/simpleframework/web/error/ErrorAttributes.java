package top.bettercode.simpleframework.web.error;

import top.bettercode.lang.property.PropertiesSource;
import top.bettercode.simpleframework.config.WebProperties;
import top.bettercode.simpleframework.web.IRespEntity;
import top.bettercode.simpleframework.web.RespEntity;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
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
  private final ErrorProperties errorProperties;
  private final MessageSource messageSource;
  private final List<IErrorHandler> errorHandlers;
  private final IErrorRespEntityHandler errorRespEntityHandler;
  private final WebProperties webProperties;
  private static final PropertiesSource propertiesSource = PropertiesSource
      .of("default-exception-handle", "exception-handle");

  public ErrorAttributes(ErrorProperties errorProperties,
      List<IErrorHandler> errorHandlers,
      IErrorRespEntityHandler errorRespEntityHandler,
      MessageSource messageSource, WebProperties webProperties) {
    this.errorProperties = errorProperties;
    this.errorHandlers = errorHandlers;
    this.errorRespEntityHandler = errorRespEntityHandler;
    this.messageSource = messageSource;
    this.webProperties = webProperties;
  }

  @Override
  public Map<String, Object> getErrorAttributes(WebRequest webRequest,
      ErrorAttributeOptions options) {
    Throwable error = getError(webRequest);
    return getErrorAttributes(error, webRequest, options.isIncluded(
        Include.STACK_TRACE)).toMap();
  }

  public IRespEntity getErrorAttributes(Throwable error, WebRequest webRequest) {
    return getErrorAttributes(error, webRequest, isIncludeStackTrace(webRequest, MediaType.ALL));
  }

  public IRespEntity getErrorAttributes(Throwable error, WebRequest webRequest,
      boolean includeStackTrace) {
    String statusCode = null;
    Integer httpStatusCode = null;
    String message;
    RespEntity<Object> respEntity = new RespEntity<>();
    Map<String, String> errors = new HashMap<>();

    if (error != null) {
      if (errorHandlers != null) {
        for (IErrorHandler errorHandler : errorHandlers) {
          errorHandler.handlerException(error, respEntity, errors,
              webProperties.getConstraintViolationSeparator());
        }
      }

      statusCode = respEntity.getStatus();
      httpStatusCode = respEntity.getHttpStatusCode();
      message = respEntity.getMessage();

      if (includeStackTrace) {
        addStackTrace(respEntity, error);
      }

      if (!StringUtils.hasText(message)) {
        message = handleMessage(error.getClass());
        if (StringUtils.hasText(error.getMessage()) && (!StringUtils.hasText(message) || !error
            .getMessage()
            .contains("Exception"))) {
          message = error.getMessage();
        }
      }

      if (httpStatusCode == null) {
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

    } else {
      message = getMessage(webRequest);
    }

    if (httpStatusCode == null) {
      httpStatusCode = getStatus(webRequest).value();
    }

    statusCode = statusCode == null ? String.valueOf(httpStatusCode) : statusCode;
    if (!StringUtils.hasText(message)) {
      if (httpStatusCode == 404) {
        message = "resource.not.found";
      } else {
        message = "";
      }
    }
    message = getText(webRequest, message);

    setErrorInfo(webRequest, httpStatusCode, message, error);

    respEntity.setStatus(statusCode);
    respEntity.setMessage(message);
    if (!errors.isEmpty()) {
      respEntity.setErrors(errors);
    }
    if (errorRespEntityHandler != null) {
      return errorRespEntityHandler.handle(webRequest, respEntity);
    } else {
      return respEntity;
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

  private void setErrorInfo(WebRequest request, Integer httpStatusCode,
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

  private HttpStatus getStatus(RequestAttributes requestAttributes) {
    Integer statusCode = getAttribute(requestAttributes, WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
    if (statusCode != null) {
      try {
        return HttpStatus.valueOf(statusCode);
      } catch (Exception ignored) {
      }
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  private String getMessage(RequestAttributes requestAttributes) {
    return getAttribute(requestAttributes, WebUtils.ERROR_MESSAGE_ATTRIBUTE);
  }

  @SuppressWarnings("unchecked")
  private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
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


  /**
   * Determine if the stacktrace attribute should be included.
   *
   * @param request  the source request
   * @param produces the media type produced (or {@code MediaType.ALL})
   * @return if the stacktrace attribute should be included
   */
  protected boolean isIncludeStackTrace(WebRequest request, MediaType produces) {
    switch (errorProperties.getIncludeStacktrace()) {
      case ALWAYS:
        return true;
      case ON_PARAM:
        return getTraceParameter(request);
      default:
        return false;
    }
  }


  private boolean getTraceParameter(WebRequest request) {
    String parameter = request.getParameter("trace");
    if (parameter == null) {
      return false;
    }
    return !"false".equalsIgnoreCase(parameter);
  }


}
