package cn.bestwu.simpleframework.security.server;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.AbstractErrorHandler;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.CollectionUtils;

/**
 * @author Peter Wu
 */
@Deprecated
public class SecurityOAuth2ErrorHandler extends AbstractErrorHandler {

  public SecurityOAuth2ErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    if (error instanceof OAuth2Exception) {
      int httpErrorCode = ((OAuth2Exception) error).getHttpErrorCode();
      Throwable cause = error.getCause();
      if (cause instanceof InternalAuthenticationServiceException) {
        cause = cause.getCause();
      }
      if (cause instanceof IllegalUserException || cause instanceof IllegalArgumentException) {
        httpErrorCode = HttpStatus.SC_BAD_REQUEST;
      }
      if (cause instanceof IllegalUserException) {
        Map<String, String> userErrors = ((IllegalUserException) cause).getErrors();
        if (!CollectionUtils.isEmpty(userErrors)) {
          errors.putAll(userErrors);
        }
      }
      respEntity.setHttpStatusCode(httpErrorCode);

      Map<String, String> additionalInformation = ((OAuth2Exception) error)
          .getAdditionalInformation();
      if (additionalInformation != null) {
        respEntity.setErrors(additionalInformation);
      }
    }
  }
}
