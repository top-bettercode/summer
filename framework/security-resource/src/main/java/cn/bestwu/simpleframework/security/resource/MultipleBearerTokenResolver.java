package cn.bestwu.simpleframework.security.resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

public class MultipleBearerTokenResolver implements BearerTokenResolver {

  private static final Pattern authorizationPattern = Pattern
      .compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
          Pattern.CASE_INSENSITIVE);

  private boolean allowFormEncodedBodyParameter = false;

  private boolean allowUriQueryParameter = false;

  private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;

  @Override
  public String resolve(HttpServletRequest request) {
    String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
    String parameterToken = resolveFromRequestParameters(request);
    if (authorizationHeaderToken != null) {
      return authorizationHeaderToken;
    }
    if (parameterToken != null && isParameterTokenSupportedForRequest(request)) {
      return parameterToken;
    }
    return null;
  }

  /**
   * Set if transport of access token using form-encoded body parameter is supported. Defaults to
   * {@code false}.
   *
   * @param allowFormEncodedBodyParameter if the form-encoded body parameter is supported
   */
  public void setAllowFormEncodedBodyParameter(boolean allowFormEncodedBodyParameter) {
    this.allowFormEncodedBodyParameter = allowFormEncodedBodyParameter;
  }

  /**
   * Set if transport of access token using URI query parameter is supported. Defaults to {@code
   * false}.
   * <p>
   * The spec recommends against using this mechanism for sending bearer tokens, and even goes as
   * far as stating that it was only included for completeness.
   *
   * @param allowUriQueryParameter if the URI query parameter is supported
   */
  public void setAllowUriQueryParameter(boolean allowUriQueryParameter) {
    this.allowUriQueryParameter = allowUriQueryParameter;
  }

  /**
   * Set this value to configure what header is checked when resolving a Bearer Token. This value is
   * defaulted to {@link HttpHeaders#AUTHORIZATION}.
   * <p>
   * This allows other headers to be used as the Bearer Token source such as {@link
   * HttpHeaders#PROXY_AUTHORIZATION}
   *
   * @param bearerTokenHeaderName the header to check when retrieving the Bearer Token.
   * @since 5.4
   */
  public void setBearerTokenHeaderName(String bearerTokenHeaderName) {
    this.bearerTokenHeaderName = bearerTokenHeaderName;
  }

  private String resolveFromAuthorizationHeader(HttpServletRequest request) {
    String authorization = request.getHeader(this.bearerTokenHeaderName);
    if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
      return null;
    }
    Matcher matcher = authorizationPattern.matcher(authorization);
    if (!matcher.matches()) {
      BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
      throw new OAuth2AuthenticationException(error);
    }
    return matcher.group("token");
  }

  private static String resolveFromRequestParameters(HttpServletRequest request) {
    String[] values = request.getParameterValues("access_token");
    if (values == null || values.length == 0) {
      return null;
    }
    if (values.length == 1) {
      return values[0];
    }
    BearerTokenError error = BearerTokenErrors
        .invalidRequest("Found multiple bearer tokens in the request");
    throw new OAuth2AuthenticationException(error);
  }

  private boolean isParameterTokenSupportedForRequest(HttpServletRequest request) {
    return ((this.allowFormEncodedBodyParameter && "POST".equals(request.getMethod()))
        || (this.allowUriQueryParameter && "GET".equals(request.getMethod())));
  }

}
