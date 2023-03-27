package top.bettercode.summer.web.deprecated;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.summer.tools.lang.util.AnnotatedUtils;
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor;

/**
 * 已弃用的接口检查
 *
 * @author Peter Wu
 */
public class DeprecatedAPIInterceptor implements NotErrorHandlerInterceptor {

  private final MessageSource messageSource;

  public DeprecatedAPIInterceptor(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler) {
    DeprecatedAPI annotation = AnnotatedUtils.getAnnotation(handler,
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
