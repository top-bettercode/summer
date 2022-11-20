package top.bettercode.simpleframework.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

public class MultipleBearerTokenResolver {

  private final Logger log = LoggerFactory.getLogger(MultipleBearerTokenResolver.class);
  private static final Pattern authorizationPattern = Pattern
      .compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
          Pattern.CASE_INSENSITIVE);

  private boolean allowFormEncodedBodyParameter = true;

  private boolean allowUriQueryParameter = true;

  private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;
  /**
   * 是否兼容旧toekn名称
   */
  private Boolean compatibleAccessToken = false;

  public String resolve(HttpServletRequest request) {
    String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
    if (authorizationHeaderToken == null && isParameterTokenSupportedForRequest(request)) {
      authorizationHeaderToken = resolveFromRequestParameters(request,
          SecurityParameterNames.ACCESS_TOKEN);
    }

    if (compatibleAccessToken) {
      if (authorizationHeaderToken == null) {
        authorizationHeaderToken = resolveCompatibleAccessTokenFromHeader(request);
      }
      if (authorizationHeaderToken == null && isParameterTokenSupportedForRequest(request)) {
        authorizationHeaderToken = resolveFromRequestParameters(request,
            SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
      }
    }
    return authorizationHeaderToken;
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
   * Set if transport of access token using URI query parameter is supported. Defaults to
   * {@code false}.
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
   * This allows other headers to be used as the Bearer Token source such as
   * {@link HttpHeaders#PROXY_AUTHORIZATION}
   *
   * @param bearerTokenHeaderName the header to check when retrieving the Bearer Token.
   * @since 5.4
   */
  public void setBearerTokenHeaderName(String bearerTokenHeaderName) {
    this.bearerTokenHeaderName = bearerTokenHeaderName;
  }

  public void setCompatibleAccessToken(Boolean compatibleAccessToken) {
    this.compatibleAccessToken = compatibleAccessToken;
  }

  private String resolveFromAuthorizationHeader(HttpServletRequest request) {
    String authorization = request.getHeader(this.bearerTokenHeaderName);
    if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
      return null;
    }
    Matcher matcher = authorizationPattern.matcher(authorization);
    if (matcher.matches()) {
      return matcher.group("token");
    } else {
      log.warn("Bearer token is malformed");
      return null;
    }
  }

  private String resolveCompatibleAccessTokenFromHeader(HttpServletRequest request) {
    return request.getHeader(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN);
  }

  private static String resolveFromRequestParameters(HttpServletRequest request, String tokenName) {
    String[] values = request.getParameterValues(tokenName);
    if (values == null || values.length == 0) {
      return null;
    }
    return values[0];
  }

  private boolean isParameterTokenSupportedForRequest(HttpServletRequest request) {
    String method = request.getMethod();
    return ((this.allowFormEncodedBodyParameter
        && (RequestMethod.POST.name().equals(method)
        || RequestMethod.PUT.name().equals(method)))
        || (this.allowUriQueryParameter
        && (RequestMethod.GET.name().equals(method)
        || RequestMethod.DELETE.name().equals(method))));
  }

}
