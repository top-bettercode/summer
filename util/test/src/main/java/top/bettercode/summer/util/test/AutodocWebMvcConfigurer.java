package top.bettercode.summer.util.test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.logging.RequestLoggingFilter;
import top.bettercode.simpleframework.AnnotatedUtils;
import top.bettercode.simpleframework.security.Anonymous;
import top.bettercode.simpleframework.security.AuthenticationHelper;
import top.bettercode.simpleframework.security.ClientAuthorize;
import top.bettercode.simpleframework.security.SecurityParameterNames;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.simpleframework.servlet.HandlerMethodContextHolder;

@ConditionalOnClass(Anonymous.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ApiSecurityProperties.class)
public class AutodocWebMvcConfigurer implements AutoDocRequestHandler {


  private final ApiSecurityProperties securityProperties;

  public AutodocWebMvcConfigurer(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Override
  public void handle(AutoDocHttpServletRequest request) {
    HandlerMethod handler = HandlerMethodContextHolder.getHandler(request);
    if (handler != null) {
      Optional<String> username = AuthenticationHelper.getUsername();
      username.ifPresent(
          u -> request.setAttribute(RequestLoggingFilter.REQUEST_LOGGING_USERNAME, username.get()));
      Set<String> requiredHeaders = Autodoc.getRequiredHeaders();
      String url = request.getRequestURI();
      boolean needAuth = false;
      //set required
      boolean isClientAuth = AnnotatedUtils.hasAnnotation(handler, ClientAuthorize.class);
      if (!AnnotatedUtils.hasAnnotation(handler, Anonymous.class)
          && !securityProperties.ignored(url) || isClientAuth) {
        requiredHeaders = new HashSet<>(requiredHeaders);
        if (securityProperties.getCompatibleAccessToken()) {
          requiredHeaders.add(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
        } else {
          requiredHeaders.add(HttpHeaders.AUTHORIZATION);
        }
        needAuth = true;
        Autodoc.requiredHeaders(requiredHeaders.toArray(new String[0]));
        //set required end
      }

      if (needAuth) {
        if (isClientAuth) {
          request.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString(
              (securityProperties.getClientId() + ":"
                  + securityProperties.getClientSecret()).getBytes()));
        } else {
          if (securityProperties.getCompatibleAccessToken()) {
            String authorization = request.getHeader(
                SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
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
      }
    }
  }

  @Override
  public boolean support(@NotNull AutoDocHttpServletRequest request) {
    return request.isMock();
  }
}