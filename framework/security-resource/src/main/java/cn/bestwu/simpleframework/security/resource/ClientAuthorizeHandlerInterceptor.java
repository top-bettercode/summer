package cn.bestwu.simpleframework.security.resource;

import cn.bestwu.logging.AnnotatedUtils;
import cn.bestwu.simpleframework.exception.UnauthorizedException;
import cn.bestwu.simpleframework.security.ClientDetailsProperties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 权限拦截
 *
 * @author Peter Wu
 */
public class ClientAuthorizeHandlerInterceptor extends HandlerInterceptorAdapter {

  private final ClientDetailsProperties clientDetailsProperties;
  private final BasicAuthenticationConverter authenticationConverter=new BasicAuthenticationConverter();

  public ClientAuthorizeHandlerInterceptor(
      ClientDetailsProperties clientDetailsProperties) {
    this.clientDetailsProperties = clientDetailsProperties;
  }


  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    if (handler instanceof HandlerMethod) {
      if (ErrorController.class.isAssignableFrom(((HandlerMethod) handler).getBeanType())) {
        return true;
      }

      if (AnnotatedUtils.hasAnnotation((HandlerMethod) handler, Anonymous.class)) {
        return true;
      } else {
        ClientAuthorize clientAuthorize = AnnotatedUtils
            .getAnnotation((HandlerMethod) handler, ClientAuthorize.class);

        if (clientAuthorize != null) {
          UsernamePasswordAuthenticationToken authRequest = authenticationConverter.convert(request);
          if (authRequest == null) {
            throw  new UnauthorizedException();
          }
          if (!(authRequest.getPrincipal().equals(clientDetailsProperties.getClientId())&&authRequest.getCredentials().equals(clientDetailsProperties.getClientSecret()))) {
            throw  new UnauthorizedException();
          }
        }
      }
    }
    return true;
  }

}
