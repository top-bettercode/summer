package cn.bestwu.summer.util.test;

import cn.bestwu.autodoc.gen.Autodoc;
import cn.bestwu.logging.AnnotatedUtils;
import cn.bestwu.simpleframework.security.resource.Anonymous;
import cn.bestwu.simpleframework.security.resource.SecurityProperties;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnClass(Anonymous.class)
@Configuration
public class AutodocWebMvcConfigurer implements WebMvcConfigurer, AutoDocRequestHandler {


  private final SecurityProperties securityProperties;

  public AutodocWebMvcConfigurer(
      SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AsyncHandlerInterceptor() {

      @Override
      public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
          Object handler) {
        Set<String> requiredHeaders = Autodoc.getRequiredHeaders();
        if (handler instanceof HandlerMethod) {
          String url = request.getServletPath();
          if (!AnnotatedUtils.hasAnnotation((HandlerMethod) handler, Anonymous.class) && !securityProperties
              .ignored(url)) {
            requiredHeaders = new HashSet<>(requiredHeaders);
            requiredHeaders.add("Authorization");
            Autodoc.requiredHeaders(requiredHeaders.toArray(new String[0]));
          }
        }
        return true;
      }

    });
  }

  @Override
  public void handle(AutoDocHttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (!StringUtils.hasText(authorization)) {
      request.header("Authorization", "bearer xxxxxxx-xxxx-xxxx-xxxx-xxxxxx");
    }
  }

  @Override
  public boolean support(@NotNull AutoDocHttpServletRequest request) {
    return request.isMock();
  }
}