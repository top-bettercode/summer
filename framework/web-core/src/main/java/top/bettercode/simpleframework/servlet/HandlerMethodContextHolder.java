package top.bettercode.simpleframework.servlet;

import java.lang.annotation.Annotation;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;
import top.bettercode.simpleframework.AnnotatedUtils;

/**
 * @author Peter Wu
 */
public class HandlerMethodContextHolder {

  private static final Logger log = LoggerFactory.getLogger(HandlerMethodContextHolder.class);
  private static final String HANDLER_METHOD =
      HandlerMethodContextHolder.class.getName() + ".handlerMethod";
  private static RequestMappingHandlerMapping handlerMapping;


  public static void setHandlerMapping(
      RequestMappingHandlerMapping handlerMapping) {
    HandlerMethodContextHolder.handlerMapping = handlerMapping;
  }

  public static HandlerMethod getHandler(HttpServletRequest request) {
    try {
      Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
      if (handler instanceof HandlerMethod && !ErrorController.class.isAssignableFrom(
          ((HandlerMethod) handler).getBeanType())) {
        return (HandlerMethod) handler;
      }
      HandlerMethod handlerMethod;
      handlerMethod = (HandlerMethod) request.getAttribute(
          HandlerMethodContextHolder.HANDLER_METHOD);
      if (handlerMethod != null) {
        return handlerMethod;
      }

      if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
        ServletRequestPathUtils.parseAndCache(request);
      }
      HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
      if (handlerExecutionChain != null) {
        handler = handlerExecutionChain.getHandler();
        if (handler instanceof HandlerMethod && !ErrorController.class.isAssignableFrom(
            ((HandlerMethod) handler).getBeanType())) {
          handlerMethod = (HandlerMethod) handler;
          request.setAttribute(HandlerMethodContextHolder.HANDLER_METHOD, handlerMethod);
          return handlerMethod;
        }
      }
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
    return null;
  }

  public static <A extends Annotation> boolean hasAnnotation(HttpServletRequest request,
      Class<A> annotationType) {
    HandlerMethod handler = getHandler(request);
    return handler != null && AnnotatedUtils.hasAnnotation(handler, annotationType);
  }

  public static <A extends Annotation> A getAnnotation(HttpServletRequest request,
      Class<A> annotationType) {
    HandlerMethod handler = getHandler(request);
    return handler == null ? null : AnnotatedUtils.getAnnotation(handler, annotationType);
  }
}