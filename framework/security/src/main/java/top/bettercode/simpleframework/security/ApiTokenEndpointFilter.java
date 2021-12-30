package top.bettercode.simpleframework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.bettercode.simpleframework.config.SummerWebProperties;
import top.bettercode.simpleframework.exception.UnauthorizedException;
import top.bettercode.simpleframework.security.authorization.ApiAuthorizationService;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.UserInfoHelper;

public final class ApiTokenEndpointFilter extends OncePerRequestFilter {

  /**
   * The default endpoint {@code URI} for access token requests.
   */
  private static final String DEFAULT_TOKEN_ENDPOINT_URI = "/oauth/token";

  private final ApiAuthorizationService apiAuthorizationService;
  private final AuthenticationManager authenticationManager;
  private final RequestMatcher tokenEndpointMatcher;
  private final RequestMatcher revokeTokenEndpointMatcher;
  private final ApiTokenService apiTokenService;
  private final SummerWebProperties summerWebProperties;
  private final ApiSecurityProperties apiSecurityProperties;
  private final IRevokeTokenService revokeTokenService;
  private final ObjectMapper objectMapper;
  private final String basicCredentials;

  private final MultipleBearerTokenResolver bearerTokenResolver = new MultipleBearerTokenResolver();


  public ApiTokenEndpointFilter(AuthenticationManager authenticationManager,
      ApiAuthorizationService apiAuthorizationService,
      ApiTokenService apiTokenService,
      SummerWebProperties summerWebProperties,
      IRevokeTokenService revokeTokenService,
      ApiSecurityProperties apiSecurityProperties,
      ObjectMapper objectMapper) {
    this(apiAuthorizationService, authenticationManager, DEFAULT_TOKEN_ENDPOINT_URI,
        apiTokenService, summerWebProperties, apiSecurityProperties,
        revokeTokenService, objectMapper);
  }


  public ApiTokenEndpointFilter(
      ApiAuthorizationService apiAuthorizationService,
      AuthenticationManager authenticationManager, String tokenEndpointUri,
      ApiTokenService apiTokenService,
      SummerWebProperties summerWebProperties,
      ApiSecurityProperties apiSecurityProperties,
      IRevokeTokenService revokeTokenService,
      ObjectMapper objectMapper) {
    this.summerWebProperties = summerWebProperties;
    this.apiSecurityProperties = apiSecurityProperties;
    if (StringUtils.hasText(apiSecurityProperties.getClientId()) && StringUtils.hasText(
        apiSecurityProperties.getClientSecret())) {
      this.basicCredentials =
          apiSecurityProperties.getClientId() + ":" + apiSecurityProperties.getClientSecret();
    } else {
      this.basicCredentials = null;
    }
    this.revokeTokenService = revokeTokenService;
    this.objectMapper = objectMapper;
    Assert.notNull(authenticationManager, "authenticationManager cannot be null");
    Assert.hasText(tokenEndpointUri, "tokenEndpointUri cannot be empty");
    this.apiAuthorizationService = apiAuthorizationService;
    this.apiTokenService = apiTokenService;
    this.authenticationManager = authenticationManager;
    this.tokenEndpointMatcher = new AntPathRequestMatcher(tokenEndpointUri, HttpMethod.POST.name());
    this.revokeTokenEndpointMatcher = new AntPathRequestMatcher(tokenEndpointUri,
        HttpMethod.DELETE.name());

    bearerTokenResolver.setCompatibleAccessToken(apiSecurityProperties.getCompatibleAccessToken());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    if (this.tokenEndpointMatcher.matches(request)) {
      authenticateBasic(request);
      try {
        String grantType = request.getParameter(SecurityParameterNames.GRANT_TYPE);
        Assert.hasText(grantType, "grantType 不能为空");
        String scope = request.getParameter(SecurityParameterNames.SCOPE);
        Assert.hasText(scope, "scope 不能为空");

        ApiAuthenticationToken apiAuthenticationToken;

        if (SecurityParameterNames.PASSWORD.equals(grantType)) {
          String username = request.getParameter(SecurityParameterNames.USERNAME);
          Assert.hasText(username, "用户名不能为空");
          String password = request.getParameter(SecurityParameterNames.PASSWORD);
          Assert.hasText(password, "密码不能为空");

          UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
              username, password);
          Authentication authentication = authenticationManager.authenticate(
              usernamePasswordAuthenticationToken);
          Object principal = authentication.getPrincipal();
          Assert.isTrue(principal instanceof UserDetails, "授权异常");

          UserDetails userDetails = (UserDetails) principal;

          if (apiSecurityProperties.needKickedOut(scope)) {
            apiAuthenticationToken = new ApiAuthenticationToken(scope,
                apiTokenService.createAccessToken(),
                apiTokenService.createRefreshToken(), userDetails);
          } else {
            apiAuthenticationToken = apiAuthorizationService.findByScopeAndUsername(scope,
                username);
            if (apiAuthenticationToken == null || apiAuthenticationToken.getRefreshToken()
                .isExpired()) {
              apiAuthenticationToken = new ApiAuthenticationToken(scope,
                  apiTokenService.createAccessToken(),
                  apiTokenService.createRefreshToken(), userDetails);
            } else if (apiAuthenticationToken.getAccessToken()
                .isExpired()) {
              apiAuthenticationToken.setAccessToken(apiTokenService.createAccessToken());
              apiAuthenticationToken.setUserDetails(userDetails);
            } else {
              apiAuthenticationToken.setUserDetails(userDetails);
            }
          }
        } else if (SecurityParameterNames.REFRESH_TOKEN.equals(grantType)) {
          String refreshToken = request.getParameter(SecurityParameterNames.REFRESH_TOKEN);
          Assert.hasText(refreshToken, "refreshToken不能为空");
          apiAuthenticationToken = apiAuthorizationService.findByRefreshToken(
              refreshToken);

          if (apiAuthenticationToken == null || apiAuthenticationToken.getRefreshToken()
              .isExpired()) {
            if (apiAuthenticationToken != null) {
              apiAuthorizationService.remove(apiAuthenticationToken);
            }
            throw new UnauthorizedException("请重新登录");
          }

          try {
            UserDetails userDetails = apiTokenService.getUserDetails(scope,
                apiAuthenticationToken.getUsername());

            apiAuthenticationToken.setAccessToken(apiTokenService.createAccessToken());
            apiAuthenticationToken.setUserDetails(userDetails);
          } catch (Exception e) {
            throw new UnauthorizedException("请重新登录", e);
          }

        } else {
          throw new IllegalArgumentException("不支持的grantType类型");
        }

        UserDetails userDetails = apiAuthenticationToken.getUserDetails();
        Authentication authenticationResult = authenticationManager.authenticate(
            new UserDetailsAuthenticationToken(userDetails));

        apiAuthorizationService.save(apiAuthenticationToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        SecurityContextHolder.setContext(context);

        Object apiTokenResponse = apiAuthenticationToken.toApiToken();
        if (summerWebProperties.wrapEnable(request)) {
          apiTokenResponse = RespEntity.ok(apiTokenResponse);
        }
        objectMapper.writeValue(response.getOutputStream(), apiTokenResponse);
      } catch (AuthenticationException ex) {
        SecurityContextHolder.clearContext();
        throw ex;
      }
    } else {
      String accessToken = bearerTokenResolver.resolve(request);
      if (StringUtils.hasText(accessToken)) {
        ApiAuthenticationToken apiAuthenticationToken = apiAuthorizationService.findByAccessToken(
            accessToken);
        if (apiAuthenticationToken != null && !apiAuthenticationToken.getAccessToken()
            .isExpired()) {
          try {
            UserDetails userDetails = apiAuthenticationToken.getUserDetails();
            Authentication authenticationResult = authenticationManager.authenticate(
                new UserDetailsAuthenticationToken(userDetails));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationResult);
            SecurityContextHolder.setContext(context);
            UserInfoHelper.put(request, userDetails);
            if (this.revokeTokenEndpointMatcher.matches(request)) {//撤消token
              if (revokeTokenService != null) {
                revokeTokenService.revokeToken(userDetails);
              }
              apiAuthorizationService.remove(apiAuthenticationToken);
              SecurityContextHolder.clearContext();
              if (summerWebProperties.okEnable(request)) {
                response.setStatus(HttpStatus.OK.value());
              } else {
                response.setStatus(HttpStatus.NO_CONTENT.value());
              }
              if (summerWebProperties.wrapEnable(request)) {
                RespEntity<Object> respEntity = new RespEntity<>();
                respEntity.setStatus(String.valueOf(HttpStatus.NO_CONTENT.value()));
                objectMapper.writeValue(response.getOutputStream(), respEntity);
              } else {
                response.flushBuffer();
              }
              return;
            }
          } catch (Exception failed) {
            SecurityContextHolder.clearContext();
            throw failed;
          }
        } else {
          logger.warn("错误或过期的token:" + accessToken);
        }
      } else if (URLFilterInvocationSecurityMetadataSource.matchClientAuthorize(request)) {
        authenticateBasic(request);
      }
      filterChain.doFilter(request, response);
    }
  }

  private void authenticateBasic(HttpServletRequest request) {
    if (this.basicCredentials == null) {
      return;
    }
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null) {
      header = header.trim();
      if (StringUtils.startsWithIgnoreCase(header, "Basic") && !header.equalsIgnoreCase("Basic")) {
        String encodedBasicCredentials = new String(
            decode(header.substring(6).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        if (this.basicCredentials.equals(encodedBasicCredentials)) {
          return;
        }
      }
    }
    throw new BadCredentialsException("basic authentication 认证失败");
  }

  private byte[] decode(byte[] base64Token) {
    try {
      return java.util.Base64.getDecoder().decode(base64Token);
    } catch (IllegalArgumentException var3) {
      throw new BadCredentialsException("Failed to decode basic authentication token");
    }
  }
}
