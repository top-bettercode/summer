package top.bettercode.summer.util.test;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.lang.AnnotatedUtils;
import top.bettercode.logging.trace.TraceHttpServletRequestWrapper;
import top.bettercode.simpleframework.security.Anonymous;
import top.bettercode.simpleframework.security.ClientAuthorize;
import top.bettercode.simpleframework.security.SecurityParameterNames;
import top.bettercode.simpleframework.security.URLFilterInvocationSecurityMetadataSource;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.lang.servlet.NotErrorHandlerInterceptor;

@ConditionalOnClass(Anonymous.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ApiSecurityProperties.class)
public class AutodocWebMvcConfigurer implements WebMvcConfigurer, AutoDocRequestHandler {


  private final ApiSecurityProperties securityProperties;

  public AutodocWebMvcConfigurer(
      ApiSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new NotErrorHandlerInterceptor() {

      @Override
      public boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
          HandlerMethod handler) {
        Set<String> requiredHeaders = Autodoc.getRequiredHeaders();
        String url = request.getServletPath();
        //set required
        if (!AnnotatedUtils.hasAnnotation(handler, Anonymous.class)
            && !securityProperties.ignored(url) || AnnotatedUtils.hasAnnotation(
            handler, ClientAuthorize.class)) {
          requiredHeaders = new HashSet<>(requiredHeaders);
          if (securityProperties.getCompatibleAccessToken()) {
            requiredHeaders.add(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
          } else {
            requiredHeaders.add(HttpHeaders.AUTHORIZATION);
          }
          Autodoc.requiredHeaders(requiredHeaders.toArray(new String[0]));
          //set required end
        } else if (request instanceof TraceHttpServletRequestWrapper
            && ((TraceHttpServletRequestWrapper) request).getRequest() instanceof AutoDocHttpServletRequest) {
          AutoDocHttpServletRequest autoRequest = (AutoDocHttpServletRequest) ((TraceHttpServletRequestWrapper) request).getRequest();
          if (!requiredHeaders.contains(HttpHeaders.AUTHORIZATION) && !requiredHeaders.contains(
              HttpHeaders.AUTHORIZATION.toLowerCase()
          )
          ) {
            autoRequest.getExtHeaders().remove(HttpHeaders.AUTHORIZATION);
          }
          if (!requiredHeaders.contains(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
              && !requiredHeaders.contains(
              SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN.toLowerCase())) {
            autoRequest.getExtHeaders().remove(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
          }
        }
        return true;
      }

    });
  }

  @Override
  public void handle(AutoDocHttpServletRequest request) {
    if (URLFilterInvocationSecurityMetadataSource.matchClientAuthorize(request)) {
      request.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString(
          (securityProperties.getClientId() + ":"
              + securityProperties.getClientSecret()).getBytes()));
    } else {
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
  }

  @Override
  public boolean support(@NotNull AutoDocHttpServletRequest request) {
    return request.isMock();
  }
}