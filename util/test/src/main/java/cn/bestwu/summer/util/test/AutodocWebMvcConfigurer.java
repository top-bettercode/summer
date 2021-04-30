package cn.bestwu.summer.util.test;

import cn.bestwu.autodoc.gen.AutoDocHttpServletRequest;
import cn.bestwu.autodoc.gen.AutoDocRequestHandler;
import cn.bestwu.autodoc.gen.Autodoc;
import cn.bestwu.simpleframework.security.resource.Anonymous;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.hateoas.server.core.AnnotationMappingDiscoverer;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@ConditionalOnClass(Anonymous.class)
@Configuration
public class AutodocWebMvcConfigurer implements WebMvcConfigurer, AutoDocRequestHandler {

  @Value("${security.url-filter.ignored:}")
  private String[] ignored;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptorAdapter() {

      @Override
      public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
          Object handler) {
        Set<String> requiredHeaders = Autodoc.getRequiredHeaders();
        if (handler instanceof HandlerMethod) {
          AnnotationMappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(
              RequestMapping.class);
          String url = DISCOVERER.getMapping(((HandlerMethod) handler).getMethod());
          if (!hasAnnotation((HandlerMethod) handler, Anonymous.class) && !ignored(url)) {
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
    request.header("Authorization", "bearer xxxxxxx-xxxx-xxxx-xxxx-xxxxxx");
  }

  protected <A extends Annotation> boolean hasAnnotation(HandlerMethod handlerMethod,
      Class<A> annotationType) {
    if (handlerMethod.hasMethodAnnotation(annotationType)) {
      return true;
    } else {
      return AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), annotationType);
    }
  }

  private boolean ignored(String path) {
    if (ignored == null) {
      return false;
    }
    AntPathMatcher antPathMatcher = new AntPathMatcher();
    for (String pattern : ignored) {
      if (antPathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }


}