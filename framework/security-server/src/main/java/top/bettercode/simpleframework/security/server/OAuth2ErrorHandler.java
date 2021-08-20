package top.bettercode.simpleframework.security.server;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.util.CollectionUtils;
import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.error.AbstractErrorHandler;

/**
 * @author Peter Wu
 */
public class OAuth2ErrorHandler extends AbstractErrorHandler {

  public OAuth2ErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    if (error instanceof OAuth2AuthenticationException) {
      String httpErrorCode = ((OAuth2AuthenticationException) error).getError().getErrorCode();
      Throwable cause = error.getCause();
      if (cause instanceof InternalAuthenticationServiceException) {
        cause = cause.getCause();
      }
      if (cause instanceof IllegalUserException || cause instanceof IllegalArgumentException) {
        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
      }
      if (cause instanceof IllegalUserException) {
        Map<String, String> userErrors = ((IllegalUserException) cause).getErrors();
        if (!CollectionUtils.isEmpty(userErrors)) {
          errors.putAll(userErrors);
        }
      }
      respEntity.setStatus(httpErrorCode);
    }
  }
}
