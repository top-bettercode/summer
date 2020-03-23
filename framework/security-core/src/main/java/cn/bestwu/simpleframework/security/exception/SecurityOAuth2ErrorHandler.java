package cn.bestwu.simpleframework.security.exception;

import cn.bestwu.simpleframework.web.IErrorHandler;
import cn.bestwu.simpleframework.web.RespEntity;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.firewall.RequestRejectedException;

/**
 * @author Peter Wu
 */
public class SecurityOAuth2ErrorHandler implements IErrorHandler {

  @Override
  public void handlerException(Throwable error, RespEntity respEntity,
      Map<String, String> errors) {
    if (error instanceof OAuth2Exception) {
      respEntity.setHttpStatusCode(((OAuth2Exception) error).getHttpErrorCode());
      respEntity.setMessage(error.getMessage());
    } else if (error instanceof AccessDeniedException
        || error instanceof RequestRejectedException) {
      respEntity.setHttpStatusCode(HttpStatus.FORBIDDEN.value());
      respEntity.setMessage(error.getMessage());
    }
  }
}
