package top.bettercode.summer.util.test;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.logging.AnnotatedUtils;
import top.bettercode.simpleframework.security.resource.Anonymous;
import top.bettercode.simpleframework.security.resource.SecurityProperties;

@ConditionalOnClass(Anonymous.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityProperties.class)
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
        if (handler instanceof HandlerMethod && ErrorController.class
            .isAssignableFrom(((HandlerMethod) handler).getBeanType())) {
          return true;
        }

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