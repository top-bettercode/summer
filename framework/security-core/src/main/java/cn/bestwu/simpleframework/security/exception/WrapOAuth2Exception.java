package cn.bestwu.simpleframework.security.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author Peter Wu
 */
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = OAuth2ExceptionJackson2Serializer.class)
public class WrapOAuth2Exception extends OAuth2Exception {

  private static final long serialVersionUID = 6008121344203507019L;
  private final OAuth2Exception delegate;

  public WrapOAuth2Exception(String msg,
      OAuth2Exception delegate) {
    super(msg, delegate.getCause());
    this.delegate = delegate;
  }

  @Override
  public String getOAuth2ErrorCode() {
    return delegate.getOAuth2ErrorCode();
  }

  @Override
  public int getHttpErrorCode() {
    return delegate.getHttpErrorCode();
  }

  @Override
  public Map<String, String> getAdditionalInformation() {
    return delegate.getAdditionalInformation();
  }

  @Override
  public void addAdditionalInformation(String key, String value) {
    delegate.addAdditionalInformation(key, value);
  }

  public static OAuth2Exception create(String errorCode, String errorMessage) {
    return OAuth2Exception
        .create(errorCode, errorMessage);
  }

  public static OAuth2Exception valueOf(Map<String, String> errorParams) {
    return OAuth2Exception.valueOf(errorParams);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public String getSummary() {
    return delegate.getSummary();
  }

  @Override
  public Throwable getCause() {
    return delegate.getCause();
  }

  @Override
  public Throwable initCause(Throwable cause) {
    return delegate.initCause(cause);
  }

  @Override
  public void printStackTrace() {
    delegate.printStackTrace();
  }

  @Override
  public void printStackTrace(PrintStream s) {
    delegate.printStackTrace(s);
  }

  @Override
  public void printStackTrace(PrintWriter s) {
    delegate.printStackTrace(s);
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    return delegate.getStackTrace();
  }

  @Override
  public void setStackTrace(StackTraceElement[] stackTrace) {
    delegate.setStackTrace(stackTrace);
  }
}
