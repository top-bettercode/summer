package cn.bestwu.simpleframework.security.exception;

import cn.bestwu.simpleframework.web.error.ErrorAttributes;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author Peter Wu
 */
public class CustomWebResponseExceptionTranslator implements
    WebResponseExceptionTranslator<OAuth2Exception> {

  private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();
  private final boolean okEnable;
  public final MessageSource messageSource;

  public CustomWebResponseExceptionTranslator(boolean okEnable,
      MessageSource messageSource) {
    this.okEnable = okEnable;
    this.messageSource = messageSource;
  }

  @Override
  public ResponseEntity<OAuth2Exception> translate(Exception e) {

    Throwable cause = e.getCause();
    if (cause instanceof IllegalUserException) {
      e = new IllegalUserOauth2Exception(cause.getMessage(), cause);
    }
    // Try to extract a SpringSecurityException from the stacktrace
    Throwable[] causeChain = throwableAnalyzer.determineCauseChain(e);
    Exception ase = (OAuth2Exception) throwableAnalyzer
        .getFirstThrowableOfType(OAuth2Exception.class, causeChain);

    if (ase != null) {
      return handleOAuth2Exception((OAuth2Exception) ase);
    }

    ase = (AuthenticationException) throwableAnalyzer
        .getFirstThrowableOfType(AuthenticationException.class,
            causeChain);
    if (ase != null) {
      return handleOAuth2Exception(new UnauthorizedException(e.getMessage(), e));
    }

    ase = (AccessDeniedException) throwableAnalyzer
        .getFirstThrowableOfType(AccessDeniedException.class, causeChain);
    if (ase instanceof AccessDeniedException) {
      return handleOAuth2Exception(new ForbiddenException(ase.getMessage(), ase));
    }

    ase = (HttpRequestMethodNotSupportedException) throwableAnalyzer.getFirstThrowableOfType(
        HttpRequestMethodNotSupportedException.class, causeChain);
    if (ase instanceof HttpRequestMethodNotSupportedException) {
      return handleOAuth2Exception(new MethodNotAllowed(ase.getMessage(), ase));
    }

    return handleOAuth2Exception(
        new ServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e));

  }

  private ResponseEntity<OAuth2Exception> handleOAuth2Exception(OAuth2Exception e) {
    int status = e.getHttpErrorCode();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cache-Control", "no-store");
    headers.set("Pragma", "no-cache");
    if (status == HttpStatus.UNAUTHORIZED.value() || (e instanceof InsufficientScopeException)) {
      headers.set("WWW-Authenticate",
          String.format("%s %s", OAuth2AccessToken.BEARER_TYPE, e.getSummary()));
    }
    HttpStatus httpStatus;
    if (okEnable) {
      httpStatus = HttpStatus.OK;
    } else {
      httpStatus = HttpStatus.valueOf(status);
    }
    String message = e.getMessage();
    if (message != null) {
      int indexOf = message.indexOf(':');
      if (indexOf != -1) {
        message = message.substring(0, indexOf);
      }
    }

    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    HttpServletRequest request =
        requestAttributes == null ? null : requestAttributes.getRequest();
    if (request != null) {
      message = getText(request, message);
      ErrorAttributes.setErrorInfo(new ServletWebRequest(request), status, String.valueOf(status), message, e);
    }

    return new ResponseEntity<>(new WrapOAuth2Exception(message, e), headers, httpStatus);
  }

  private String getText(HttpServletRequest request, Object code, Object... args) {
    String codeString = String.valueOf(code);
    return messageSource.getMessage(codeString, args, codeString,
        request == null ? Locale.CHINA : request.getLocale());
  }

  public void setThrowableAnalyzer(ThrowableAnalyzer throwableAnalyzer) {
    this.throwableAnalyzer = throwableAnalyzer;
  }

  @SuppressWarnings("serial")
  private static class IllegalUserOauth2Exception extends OAuth2Exception {

    public IllegalUserOauth2Exception(String msg, Throwable t) {
      super(msg, t);
    }

    @Override
    public String getOAuth2ErrorCode() {
      return HttpStatus.BAD_REQUEST.getReasonPhrase();
    }

    @Override
    public int getHttpErrorCode() {
      return 400;
    }

  }

  @SuppressWarnings("serial")
  private static class ForbiddenException extends OAuth2Exception {

    public ForbiddenException(String msg, Throwable t) {
      super(msg, t);
    }

    @Override
    public String getOAuth2ErrorCode() {
      return "access_denied";
    }

    @Override
    public int getHttpErrorCode() {
      return 403;
    }

  }

  @SuppressWarnings("serial")
  private static class ServerErrorException extends OAuth2Exception {

    public ServerErrorException(String msg, Throwable t) {
      super(msg, t);
    }

    @Override
    public String getOAuth2ErrorCode() {
      return "server_error";
    }

    @Override
    public int getHttpErrorCode() {
      return 500;
    }

  }

  @SuppressWarnings("serial")
  private static class UnauthorizedException extends OAuth2Exception {

    public UnauthorizedException(String msg, Throwable t) {
      super(msg, t);
    }

    @Override
    public String getOAuth2ErrorCode() {
      return "unauthorized";
    }

    @Override
    public int getHttpErrorCode() {
      return 401;
    }

  }

  @SuppressWarnings("serial")
  private static class MethodNotAllowed extends OAuth2Exception {

    public MethodNotAllowed(String msg, Throwable t) {
      super(msg, t);
    }

    @Override
    public String getOAuth2ErrorCode() {
      return "method_not_allowed";
    }

    @Override
    public int getHttpErrorCode() {
      return 405;
    }

  }

}
