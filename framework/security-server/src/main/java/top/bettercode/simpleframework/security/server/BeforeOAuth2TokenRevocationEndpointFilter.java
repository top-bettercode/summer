package top.bettercode.simpleframework.security.server;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


public final class BeforeOAuth2TokenRevocationEndpointFilter extends OncePerRequestFilter {

  /**
   * The default endpoint {@code URI} for token revocation requests.
   */
  private static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI = "/oauth2/revoke";
  private static final String COMPATIBLE_REVOCATION_ENDPOINT_URI = "/oauth2/token";
  private final RequestMatcher tokenRevocationEndpointMatcher;
  private final RequestMatcher compatibleTokenRevocationEndpointMatcher;
  private final OAuth2AuthorizationService oauth2AuthorizationService;
  private final IRevokeTokenService revokeTokenService;


  private final AuthenticationManager authenticationManager;
  private final Converter<HttpServletRequest, Authentication> tokenRevocationAuthenticationConverter =
      new DefaultTokenRevocationAuthenticationConverter();
  private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
      new OAuth2ErrorHttpMessageConverter();


  public BeforeOAuth2TokenRevocationEndpointFilter(
      OAuth2AuthorizationService oauth2AuthorizationService,
      IRevokeTokenService revokeTokenService,
      AuthenticationManager authenticationManager) {
    this.oauth2AuthorizationService = oauth2AuthorizationService;
    this.revokeTokenService = revokeTokenService;
    Assert.notNull(authenticationManager, "authenticationManager cannot be null");
    this.authenticationManager = authenticationManager;
    this.tokenRevocationEndpointMatcher = new AntPathRequestMatcher(
        DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI, HttpMethod.POST.name());
    this.compatibleTokenRevocationEndpointMatcher = new AntPathRequestMatcher(
        COMPATIBLE_REVOCATION_ENDPOINT_URI, HttpMethod.DELETE.name());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    if (this.tokenRevocationEndpointMatcher.matches(request)) {
      Map<String, String[]> parameters = request.getParameterMap();

      // token (REQUIRED)
      String token = request.getParameter(OAuth2ParameterNames.TOKEN);
      if (!StringUtils.hasText(token) || parameters.get(OAuth2ParameterNames.TOKEN).length != 1) {
        throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.TOKEN);
      }

      // token_type_hint (OPTIONAL)
      String tokenTypeHint = request.getParameter(OAuth2ParameterNames.TOKEN_TYPE_HINT);
      if (StringUtils.hasText(tokenTypeHint)
          && parameters.get(OAuth2ParameterNames.TOKEN_TYPE_HINT).length != 1) {
        throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.TOKEN_TYPE_HINT);
      }

      OAuth2Authorization authorization = oauth2AuthorizationService.findByToken(token,
          OAuth2TokenType.ACCESS_TOKEN);

      Object principal = authorization.getAttribute(authorization.getPrincipalName());
      if (revokeTokenService != null) {
        revokeTokenService.revokeToken(principal);
      }
    }
    if (this.compatibleTokenRevocationEndpointMatcher.matches(request)) {
      Map<String, String[]> parameters = request.getParameterMap();

      // token (REQUIRED)
      String token = request.getParameter(OAuth2ParameterNames.ACCESS_TOKEN);
      if (!StringUtils.hasText(token)
          || parameters.get(OAuth2ParameterNames.ACCESS_TOKEN).length != 1) {
        throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.ACCESS_TOKEN);
      }
      OAuth2Authorization authorization = oauth2AuthorizationService.findByToken(token,
          OAuth2TokenType.ACCESS_TOKEN);
      Object principal = authorization.getAttribute(authorization.getPrincipalName());
      if (revokeTokenService != null) {
        revokeTokenService.revokeToken(principal);
      }

      try {
        this.authenticationManager.authenticate(
            this.tokenRevocationAuthenticationConverter.convert(request));
        response.setStatus(HttpStatus.OK.value());
      } catch (OAuth2AuthenticationException ex) {
        SecurityContextHolder.clearContext();
        sendErrorResponse(response, ex.getError());
      }
    } else {
      filterChain.doFilter(request, response);
    }

  }

  private void sendErrorResponse(HttpServletResponse response, OAuth2Error error)
      throws IOException {
    ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
    httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
    this.errorHttpResponseConverter.write(error, null, httpResponse);
  }

  private static void throwError(String errorCode, String parameterName) {
    OAuth2Error error = new OAuth2Error(errorCode,
        "OAuth 2.0 Token Revocation Parameter: " + parameterName,
        "https://tools.ietf.org/html/rfc7009#section-2.1");
    throw new OAuth2AuthenticationException(error);
  }

  private static class DefaultTokenRevocationAuthenticationConverter
      implements Converter<HttpServletRequest, Authentication> {

    @Override
    public Authentication convert(HttpServletRequest request) {
      Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

      Map<String, String[]> parameters = request.getParameterMap();

      // token (REQUIRED)
      String token = request.getParameter(OAuth2ParameterNames.TOKEN);
      if (!StringUtils.hasText(token) || parameters.get(OAuth2ParameterNames.TOKEN).length != 1) {
        throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.TOKEN);
      }

      // token_type_hint (OPTIONAL)
      String tokenTypeHint = request.getParameter(OAuth2ParameterNames.TOKEN_TYPE_HINT);
      if (StringUtils.hasText(tokenTypeHint)
          && parameters.get(OAuth2ParameterNames.TOKEN_TYPE_HINT).length != 1) {
        throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.TOKEN_TYPE_HINT);
      }

      return new OAuth2TokenRevocationAuthenticationToken(token, clientPrincipal, tokenTypeHint);
    }
  }
}
