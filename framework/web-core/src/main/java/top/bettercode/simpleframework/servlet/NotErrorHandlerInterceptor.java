package top.bettercode.simpleframework.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * 非错误 HandlerMethod Interceptor
 *
 * @author Peter Wu
 */
public interface NotErrorHandlerInterceptor extends AsyncHandlerInterceptor {

  String ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

  @Override
  default boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) throws Exception {
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      if (ErrorController.class.isAssignableFrom((handlerMethod).getBeanType())) {
        return true;
      }
      return preHandlerMethod(request, response, handlerMethod);
    }
    return true;
  }

  default boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler) throws Exception {

    return true;
  }

  @Override
  default void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      if (ErrorController.class.isAssignableFrom((handlerMethod).getBeanType())) {
        return;
      }
      afterCompletionMethod(request, response, handlerMethod, ex);
    }
  }

  default void afterCompletionMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler, Throwable ex) throws Exception {

  }

  default Throwable getError(HttpServletRequest request) {
    Throwable exception = (Throwable) request.getAttribute(ERROR_INTERNAL_ATTRIBUTE);
    if (exception == null) {
      exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    }
    return exception;
  }

}
