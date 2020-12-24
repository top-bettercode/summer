package cn.bestwu.simpleframework.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

@SuppressWarnings("deprecation")
public class IllegalUserOauth2Exception extends OAuth2Exception {

  private static final long serialVersionUID = -3550061022144911053L;

  public IllegalUserOauth2Exception(String msg) {
    super(msg);
  }

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