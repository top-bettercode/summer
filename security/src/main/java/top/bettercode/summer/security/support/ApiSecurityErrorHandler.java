package top.bettercode.summer.security.support;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.CollectionUtils;
import top.bettercode.summer.web.RespEntity;
import top.bettercode.summer.web.error.AbstractErrorHandler;

/**
 * @author Peter Wu
 */
public class ApiSecurityErrorHandler extends AbstractErrorHandler {

  public ApiSecurityErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    if (error instanceof IllegalUserException) {
      respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
      Map<String, String> userErrors = ((IllegalUserException) error).getErrors();
      if (!CollectionUtils.isEmpty(userErrors)) {
        errors.putAll(userErrors);
      }
    } else if (error instanceof BadCredentialsException) {
      respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
    }
  }
}
