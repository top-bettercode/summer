package top.bettercode.summer.test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.summer.security.Anonymous;
import top.bettercode.summer.security.AuthenticationHelper;
import top.bettercode.summer.security.ClientAuthorize;
import top.bettercode.summer.security.SecurityParameterNames;
import top.bettercode.summer.security.config.ApiSecurityProperties;
import top.bettercode.summer.test.autodoc.Autodoc;
import top.bettercode.summer.tools.lang.operation.HttpOperation;
import top.bettercode.summer.web.AnnotatedUtils;
import top.bettercode.summer.web.servlet.HandlerMethodContextHolder;

@ConditionalOnClass(Anonymous.class)
@Configuration(proxyBeanMethods = false)
public class AutodocAuthWebMvcConfigurer implements AutoDocRequestHandler {

  private final ApiSecurityProperties securityProperties;

  public AutodocAuthWebMvcConfigurer(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Override
  public void handle(AutoDocHttpServletRequest request) {
    HandlerMethod handler = HandlerMethodContextHolder.getHandler(request);
    if (handler != null) {
      Optional<String> username = AuthenticationHelper.getUsername();
      username.ifPresent(
          u -> request.setAttribute(HttpOperation.REQUEST_LOGGING_USERNAME, username.get()));
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
                  "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
            }
          } else {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.hasText(authorization)) {
              request.header(HttpHeaders.AUTHORIZATION, "bearer xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
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