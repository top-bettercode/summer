package top.bettercode.simpleframework.web.resolver;

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
import top.bettercode.simpleframework.config.SummerWebProperties;
import top.bettercode.simpleframework.web.IRespEntity;
import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.error.ErrorAttributes;

/**
 * @author Peter Wu
 */
public class ApiHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {


  private final HandlerMethodReturnValueHandler delegate;
  private final SummerWebProperties summerWebProperties;
  private final ErrorAttributes errorAttributes;

  public ApiHandlerMethodReturnValueHandler(
      HandlerMethodReturnValueHandler delegate,
      SummerWebProperties summerWebProperties, ErrorAttributes errorAttributes) {
    this.delegate = delegate;
    this.summerWebProperties = summerWebProperties;
    this.errorAttributes = errorAttributes;
  }


  @Override
  public boolean supportsReturnType(MethodParameter returnType) {
    return delegate.supportsReturnType(returnType);
  }

  @Override
  public void handleReturnValue(Object returnValue, MethodParameter returnType,
      ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

    Class<?> typeContainingClass = returnType.getContainingClass();
    Class<?> parameterType = returnType.getParameterType();
    if (!void.class.equals(parameterType)
        && (AnnotatedElementUtils.hasAnnotation(typeContainingClass, ResponseBody.class)
        || returnType.hasMethodAnnotation(ResponseBody.class)
        || HttpEntity.class.isAssignableFrom(parameterType)
        && !RequestEntity.class.isAssignableFrom(parameterType))) {

      //异常信息处理
      Object body =
          returnValue instanceof ResponseEntity ? ((ResponseEntity<?>) returnValue).getBody()
              : returnValue;
      if (body instanceof Throwable) {
        IRespEntity respEntity = errorAttributes.getErrorAttributes((Throwable) body, webRequest);
        body = respEntity;

        HttpStatus statusCode = ((ResponseEntity<?>) returnValue).getStatusCode();
        Integer httpStatusCode = respEntity.getHttpStatusCode();
        if (httpStatusCode != null) {
          statusCode = HttpStatus.valueOf(httpStatusCode);
        }
        webRequest.getNativeResponse(HttpServletResponse.class)
            .setStatus(statusCode.value());
        if (returnValue instanceof ResponseEntity) {
          returnValue =
              ResponseEntity
                  .status(statusCode)
                  .headers(((ResponseEntity<?>) returnValue).getHeaders())
                  .body(body);
        } else {
          returnValue = body;
        }
      }

      if (summerWebProperties.wrapEnable(webRequest) && (!(returnValue instanceof IRespEntity || (
          returnValue instanceof HttpEntity && ((HttpEntity<?>) returnValue)
              .getBody() instanceof IRespEntity)))
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

      if (summerWebProperties.okEnable(webRequest)) {
        webRequest.getNativeResponse(HttpServletResponse.class).setStatus(HttpStatus.OK.value());
        if (returnValue instanceof ResponseEntity) {
          int statusCode = ((ResponseEntity<?>) returnValue).getStatusCode().value();
          if (statusCode != 404 && statusCode != 405) {
            returnValue = ResponseEntity.ok()
                .headers(((ResponseEntity<?>) returnValue).getHeaders())
                .body(((ResponseEntity<?>) returnValue).getBody());
          }
        }
      }
    }
    delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
  }

  public boolean supportsRewrapType(MethodParameter returnType) {
    Class<?> typeContainingClass = returnType.getContainingClass();
    Class<?> parameterType = returnType.getParameterType();
    boolean support = !AnnotatedElementUtils.hasAnnotation(parameterType, NoWrapResp.class)
        && !AnnotatedElementUtils.hasAnnotation(typeContainingClass, NoWrapResp.class)
        && !returnType.hasMethodAnnotation(NoWrapResp.class);
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
