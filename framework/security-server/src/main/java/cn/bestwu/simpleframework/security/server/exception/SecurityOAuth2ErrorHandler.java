package cn.bestwu.simpleframework.security.server.exception;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.AbstractErrorHandler;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author Peter Wu
 */
@SuppressWarnings("deprecation")
public class SecurityOAuth2ErrorHandler extends AbstractErrorHandler {

  public SecurityOAuth2ErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    if (error instanceof OAuth2Exception) {
      respEntity.setHttpStatusCode(((OAuth2Exception) error).getHttpErrorCode());

      Map<String, String> additionalInformation = ((OAuth2Exception) error).getAdditionalInformation();
      if (additionalInformation != null) {
        respEntity.setErrors(additionalInformation);
      }
    }
  }
}
