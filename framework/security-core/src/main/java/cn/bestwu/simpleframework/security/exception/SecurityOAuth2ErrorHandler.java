package cn.bestwu.simpleframework.security.exception;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.IErrorHandler;
import java.util.Map;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author Peter Wu
 */
@SuppressWarnings("deprecation")
public class SecurityOAuth2ErrorHandler implements IErrorHandler {

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors) {
    if (error instanceof OAuth2Exception) {
      respEntity.setHttpStatusCode(((OAuth2Exception) error).getHttpErrorCode());
    }
  }
}
