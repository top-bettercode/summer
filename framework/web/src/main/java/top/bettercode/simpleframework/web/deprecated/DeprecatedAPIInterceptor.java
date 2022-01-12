package top.bettercode.simpleframework.web.deprecated;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.bettercode.logging.AnnotatedUtils;

/**
 * 已弃用的接口检查
 *
 * @author Peter Wu
 */
public class DeprecatedAPIInterceptor implements AsyncHandlerInterceptor {

  private final MessageSource messageSource;

  public DeprecatedAPIInterceptor(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    if (handler instanceof HandlerMethod && ErrorController.class
        .isAssignableFrom(((HandlerMethod) handler).getBeanType())) {
      return true;
    }
    DeprecatedAPI annotation = AnnotatedUtils.getAnnotation((HandlerMethod) handler,
        DeprecatedAPI.class);
    if (annotation != null) {
      throw new IllegalStateException(getText(request, annotation.message()));
    }

    return true;
  }

  private String getText(HttpServletRequest request, Object code, Object... args) {
    String codeString = String.valueOf(code);
    return messageSource.getMessage(codeString, args, codeString,
        request == null ? Locale.CHINA : request.getLocale());
  }

}
