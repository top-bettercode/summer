package top.bettercode.simpleframework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.logging.RequestLoggingFilter;
import top.bettercode.simpleframework.AnnotatedUtils;
import top.bettercode.simpleframework.config.SummerWebProperties;
import top.bettercode.simpleframework.exception.UnauthorizedException;
import top.bettercode.simpleframework.security.repository.ApiTokenRepository;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.simpleframework.servlet.HandlerMethodContextHolder;
import top.bettercode.simpleframework.web.RespEntity;

public final class ApiTokenEndpointFilter extends OncePerRequestFilter {

  /**
   * The default endpoint {@code URI} for access token requests.
   */
  private static final String DEFAULT_TOKEN_ENDPOINT_URI = "/oauth/token";

  private final ApiTokenService apiTokenService;
  private final PasswordEncoder passwordEncoder;
  private final RequestMatcher tokenEndpointMatcher;
  private final RequestMatcher revokeTokenEndpointMatcher;
  private final SummerWebProperties summerWebProperties;
  private final IRevokeTokenService revokeTokenService;
  private final ObjectMapper objectMapper;
  private final String basicCredentials;

  private final MultipleBearerTokenResolver bearerTokenResolver = new MultipleBearerTokenResolver();


  public ApiTokenEndpointFilter(
      ApiTokenService apiTokenService,
      PasswordEncoder passwordEncoder,
      SummerWebProperties summerWebProperties,
      IRevokeTokenService revokeTokenService,
      ObjectMapper objectMapper) {
    this(apiTokenService, passwordEncoder, summerWebProperties, revokeTokenService, objectMapper,
        DEFAULT_TOKEN_ENDPOINT_URI);
  }


  public ApiTokenEndpointFilter(
      ApiTokenService apiTokenService,
      PasswordEncoder passwordEncoder,
      SummerWebProperties summerWebProperties,
      IRevokeTokenService revokeTokenService,
      ObjectMapper objectMapper,
      String tokenEndpointUri
  ) {
    this.apiTokenService = apiTokenService;
    this.passwordEncoder = passwordEncoder;
    this.summerWebProperties = summerWebProperties;
    ApiSecurityProperties securityProperties = apiTokenService.getSecurityProperties();
    if (StringUtils.hasText(securityProperties.getClientId()) && StringUtils.hasText(
        securityProperties.getClientSecret())) {
      this.basicCredentials =
          securityProperties.getClientId() + ":" + securityProperties.getClientSecret();
    } else {
      this.basicCredentials = null;
    }
    this.revokeTokenService = revokeTokenService;
    this.objectMapper = objectMapper;
    Assert.hasText(tokenEndpointUri, "tokenEndpointUri cannot be empty");
    this.tokenEndpointMatcher = new AntPathRequestMatcher(tokenEndpointUri, HttpMethod.POST.name());
    this.revokeTokenEndpointMatcher = new AntPathRequestMatcher(tokenEndpointUri,
        HttpMethod.DELETE.name());

    bearerTokenResolver.setCompatibleAccessToken(securityProperties.getCompatibleAccessToken());
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    ApiTokenRepository apiTokenRepository = apiTokenService.getApiTokenRepository();
    if (this.tokenEndpointMatcher.matches(request)) {
      authenticateBasic(request);
      try {
        String grantType = request.getParameter(SecurityParameterNames.GRANT_TYPE);
        Assert.hasText(grantType, "grantType 不能为空");
        String scope = request.getParameter(SecurityParameterNames.SCOPE);
        Assert.hasText(scope, "scope 不能为空");

        ApiToken apiAuthenticationToken;

        if (SecurityParameterNames.PASSWORD.equals(grantType)) {
          String username = request.getParameter(SecurityParameterNames.USERNAME);
          Assert.hasText(username, "用户名不能为空");
          String password = request.getParameter(SecurityParameterNames.PASSWORD);
          Assert.hasText(password, "密码不能为空");

          UserDetails userDetails = apiTokenService.getUserDetails(scope, username);
          Assert.isTrue(passwordEncoder.matches(password, userDetails.getPassword()),
              "用户名或密码错误");

          apiAuthenticationToken = apiTokenService.getApiToken(scope, userDetails,
              apiTokenService.getSecurityProperties().needKickedOut(scope));
        } else if (SecurityParameterNames.REFRESH_TOKEN.equals(grantType)) {
          String refreshToken = request.getParameter(SecurityParameterNames.REFRESH_TOKEN);
          Assert.hasText(refreshToken, "refreshToken不能为空");
          apiAuthenticationToken = apiTokenRepository.findByRefreshToken(
              refreshToken);

          if (apiAuthenticationToken == null || apiAuthenticationToken.getRefreshToken()
              .isExpired()) {
            if (apiAuthenticationToken != null) {
              apiTokenRepository.remove(apiAuthenticationToken);
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
          UserDetails userDetails = apiTokenService.getUserDetails(grantType, request);
          apiAuthenticationToken = apiTokenService.getApiToken(scope, userDetails,
              apiTokenService.getSecurityProperties().needKickedOut(scope));
        }

        UserDetails userDetails = apiAuthenticationToken.getUserDetails();
        Authentication authenticationResult = new UserDetailsAuthenticationToken(userDetails);

        apiTokenRepository.save(apiAuthenticationToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        SecurityContextHolder.setContext(context);

        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
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
        ApiToken apiAuthenticationToken = apiTokenRepository.findByAccessToken(
            accessToken);
        if (apiAuthenticationToken != null && !apiAuthenticationToken.getAccessToken()
            .isExpired()) {
          try {
            UserDetails userDetails = apiAuthenticationToken.getUserDetails();
            apiTokenRepository.validateUserDetails(userDetails);
            Authentication authenticationResult = new UserDetailsAuthenticationToken(userDetails);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationResult);
            request.setAttribute(RequestLoggingFilter.REQUEST_LOGGING_USERNAME,
                apiAuthenticationToken.getScope() + ":" + userDetails.getUsername());
            SecurityContextHolder.setContext(context);
            if (this.revokeTokenEndpointMatcher.matches(request)) {//撤消token
              if (revokeTokenService != null) {
                revokeTokenService.revokeToken(userDetails);
              }
              apiTokenRepository.remove(apiAuthenticationToken);
              SecurityContextHolder.clearContext();
              response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
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
          if (this.revokeTokenEndpointMatcher.matches(request)) {//撤消token
            throw new UnauthorizedException("错误或过期的token:" + accessToken);
          }
          logger.warn("错误或过期的token:" + accessToken);
        }
      } else if (needClientAuthorize(request)) {
        authenticateBasic(request);
      }
      filterChain.doFilter(request, response);
    }
  }

  public static boolean needClientAuthorize(HttpServletRequest request) {
    //ClientAuthorize
    HandlerMethod handler = HandlerMethodContextHolder.getHandler(request);
    if (handler != null) {
      return AnnotatedUtils.hasAnnotation(handler, ClientAuthorize.class);
    }
    return false;
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
