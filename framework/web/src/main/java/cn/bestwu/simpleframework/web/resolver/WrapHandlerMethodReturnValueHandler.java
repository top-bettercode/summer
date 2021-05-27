package cn.bestwu.simpleframework.web.resolver;

import cn.bestwu.simpleframework.web.IRespEntity;
import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.ErrorAttributes;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author Peter Wu
 */
public class WrapHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {


  private final HandlerMethodReturnValueHandler delegate;
  private final Boolean okEnable;
  private final Boolean wrapEnable;
  private final ErrorAttributes errorAttributes;

  public WrapHandlerMethodReturnValueHandler(
      HandlerMethodReturnValueHandler delegate, Boolean okEnable, Boolean wrapEnable,
      ErrorAttributes errorAttributes) {
    this.delegate = delegate;
    this.okEnable = okEnable;
    this.wrapEnable = wrapEnable;
    this.errorAttributes = errorAttributes;
  }


  @Override
  public boolean supportsReturnType(MethodParameter returnType) {
    return delegate.supportsReturnType(returnType);
  }

  @Override
  public void handleReturnValue(Object returnValue, MethodParameter returnType,
      ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

    if (okEnable) {
      webRequest.getNativeResponse(HttpServletResponse.class).setStatus(HttpStatus.OK.value());
      if (returnValue instanceof ResponseEntity) {
        int statusCode = ((ResponseEntity<?>) returnValue).getStatusCode().value();
        if (statusCode != 404 && statusCode != 405) {
          returnValue = ResponseEntity.ok().headers(((ResponseEntity<?>) returnValue).getHeaders())
              .body(((ResponseEntity<?>) returnValue).getBody());
        }
      }
    }

    //异常信息处理
    Object body =
        returnValue instanceof ResponseEntity ? ((ResponseEntity<?>) returnValue).getBody()
            : returnValue;
    if (body instanceof Throwable) {
      body = errorAttributes.getErrorAttributes((Throwable) body, webRequest);

      returnValue =
          returnValue instanceof ResponseEntity ? ResponseEntity
              .status(((ResponseEntity<?>) returnValue).getStatusCode())
              .headers(((ResponseEntity<?>) returnValue).getHeaders()).body(body)
              : body;
    } else if (wrapEnable && (!(returnValue instanceof IRespEntity))
        && supportsRewrapType(returnType)) {
      Object value = returnValue;

      if (returnValue instanceof HttpEntity) {
        value = ((HttpEntity<?>) returnValue).getBody();
        returnValue = new HttpEntity<>(rewrapResult(value),
            ((HttpEntity<?>) returnValue).getHeaders());
      } else {
        returnValue = rewrapResult(value);
      }
    }
    delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
  }

  public boolean supportsRewrapType(MethodParameter returnType) {
    Class<?> typeContainingClass = returnType.getContainingClass();
    Class<?> parameterType = returnType.getParameterType();
    boolean support = !void.class.equals(parameterType) && !AnnotatedElementUtils
        .hasAnnotation(parameterType, NoWrapResp.class) && !AnnotatedElementUtils
        .hasAnnotation(typeContainingClass, NoWrapResp.class) &&
        !returnType.hasMethodAnnotation(NoWrapResp.class) && (
        AnnotatedElementUtils.hasAnnotation(typeContainingClass, ResponseBody.class) ||
            returnType.hasMethodAnnotation(ResponseBody.class)
            || HttpEntity.class.isAssignableFrom(parameterType) &&
            !RequestEntity.class.isAssignableFrom(parameterType));
    if (support) {
      return !Objects.equals(returnType.getExecutable().getDeclaringClass().getPackage().getName(),
          "org.springframework.boot.actuate.endpoint.web.servlet");
    }
    return support;
  }

  private Object rewrapResult(Object originalValue) {
    return new RespEntity<>(originalValue);
  }

}
