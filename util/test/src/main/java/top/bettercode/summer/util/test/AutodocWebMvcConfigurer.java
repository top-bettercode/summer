package top.bettercode.summer.util.test;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.logging.AnnotatedUtils;
import top.bettercode.simpleframework.security.Anonymous;
import top.bettercode.simpleframework.security.ApiTokenService;
import top.bettercode.simpleframework.security.SecurityParameterNames;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;

@ConditionalOnClass(Anonymous.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ApiSecurityProperties.class)
public class AutodocWebMvcConfigurer implements WebMvcConfigurer, AutoDocRequestHandler {


  private final ApiSecurityProperties securityProperties;

  public AutodocWebMvcConfigurer(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public ApiTokenService apiTokenService(ApiSecurityProperties securityProperties,
      ApiAuthorizationService apiAuthorizationService,
      UserDetailsService userDetailsService) {
    return new ApiTokenService(securityProperties, apiAuthorizationService, userDetailsService);
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
          if (!AnnotatedUtils.hasAnnotation((HandlerMethod) handler, Anonymous.class)
              && !securityProperties
              .ignored(url)) {
            requiredHeaders = new HashSet<>(requiredHeaders);
            if (securityProperties.getCompatibleAccessToken()) {
              requiredHeaders.add(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
            } else {
              requiredHeaders.add(HttpHeaders.AUTHORIZATION);
            }
            Autodoc.requiredHeaders(requiredHeaders.toArray(new String[0]));
          }
        }
        return true;
      }

    });
  }

  @Override
  public void handle(AutoDocHttpServletRequest request) {
    if (securityProperties.getCompatibleAccessToken()) {
      String authorization = request.getHeader(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
      if (!StringUtils.hasText(authorization)) {
        request.header(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN,
            "xxxxxxx-xxxx-xxxx-xxxx-xxxxxx");
      }
    } else {
      String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (!StringUtils.hasText(authorization)) {
        request.header(HttpHeaders.AUTHORIZATION, "bearer xxxxxxx-xxxx-xxxx-xxxx-xxxxxx");
      }
    }

  }

  @Override
  public boolean support(@NotNull AutoDocHttpServletRequest request) {
    return request.isMock();
  }
}