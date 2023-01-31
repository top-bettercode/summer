package top.bettercode.summer.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationToken;
import top.bettercode.summer.security.authorize.ClientAuthorize;
import top.bettercode.summer.security.config.ApiSecurityProperties;
import top.bettercode.summer.security.repository.ApiTokenRepository;
import top.bettercode.summer.security.support.SecurityParameterNames;
import top.bettercode.summer.security.token.ApiToken;
import top.bettercode.summer.security.token.IRevokeTokenService;
import top.bettercode.summer.security.token.MultipleBearerTokenResolver;
import top.bettercode.summer.tools.lang.operation.HttpOperation;
import top.bettercode.summer.tools.lang.util.ArrayUtil;
import top.bettercode.summer.web.AnnotatedUtils;
import top.bettercode.summer.web.RespEntity;
import top.bettercode.summer.web.config.SummerWebProperties;
import top.bettercode.summer.web.exception.UnauthorizedException;
import top.bettercode.summer.web.form.FormDuplicateCheckInterceptor;
import top.bettercode.summer.web.form.FormDuplicateException;
import top.bettercode.summer.web.form.IFormkeyService;
import top.bettercode.summer.web.servlet.HandlerMethodContextHolder;

public final class ApiTokenEndpointFilter extends OncePerRequestFilter {

  private final Logger log = LoggerFactory.getLogger(ApiTokenEndpointFilter.class);

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
  private final IFormkeyService formkeyService;
  private final String basicCredentials;

  private final MultipleBearerTokenResolver bearerTokenResolver = new MultipleBearerTokenResolver();


  public ApiTokenEndpointFilter(
      ApiTokenService apiTokenService,
      PasswordEncoder passwordEncoder,
      SummerWebProperties summerWebProperties,
      IRevokeTokenService revokeTokenService,
      ObjectMapper objectMapper, IFormkeyService formkeyService) {
    this(apiTokenService, passwordEncoder, summerWebProperties, revokeTokenService, objectMapper,
        formkeyService, DEFAULT_TOKEN_ENDPOINT_URI);
  }

  public ApiTokenEndpointFilter(
      ApiTokenService apiTokenService,
      PasswordEncoder passwordEncoder,
      SummerWebProperties summerWebProperties,
      IRevokeTokenService revokeTokenService,
      ObjectMapper objectMapper,
      IFormkeyService formkeyService,
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
    this.formkeyService = formkeyService;
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
    ApiSecurityProperties securityProperties = apiTokenService.getSecurityProperties();
    if (this.tokenEndpointMatcher.matches(request)) {
      String formkey = formkeyService.getFormkey(request, summerWebProperties.getFormKeyName(),
          true);
      if (formkey != null && formkeyService.exist(formkey, -1)) {
        throw new FormDuplicateException(FormDuplicateCheckInterceptor.DEFAULT_MESSAGE);
      }
      try {
        authenticateBasic(request);
        String grantType = request.getParameter(SecurityParameterNames.GRANT_TYPE);
        Assert.hasText(grantType, "grantType 不能为空");
        String scope = request.getParameter(SecurityParameterNames.SCOPE);
        Assert.hasText(scope, "scope 不能为空");
        Assert.isTrue(ArrayUtil.contains(securityProperties.getSupportScopes(), scope),
            "不支持的scope:" + scope);

        ApiToken apiToken;

        if (SecurityParameterNames.PASSWORD.equals(grantType)) {
          apiTokenService.beforeLogin(request, grantType, scope);
          String username = request.getParameter(SecurityParameterNames.USERNAME);
          Assert.hasText(username, "用户名不能为空");
          String password = request.getParameter(SecurityParameterNames.PASSWORD);
          Assert.hasText(password, "密码不能为空");

          UserDetails userDetails = apiTokenService.getUserDetails(scope, username);
          Assert.isTrue(passwordEncoder.matches(password, userDetails.getPassword()),
              "用户名或密码错误");

          apiToken = apiTokenService.getApiToken(scope, userDetails);
          apiTokenService.afterLogin(apiToken, request);
        } else if (SecurityParameterNames.REFRESH_TOKEN.equals(grantType)) {
          String refreshToken = request.getParameter(SecurityParameterNames.REFRESH_TOKEN);
          Assert.hasText(refreshToken, "refreshToken不能为空");
          apiToken = apiTokenRepository.findByRefreshToken(
              refreshToken);

          if (apiToken == null || apiToken.getRefreshToken().isExpired()) {
            if (apiToken != null) {
              apiTokenRepository.remove(apiToken);
            }
            throw new UnauthorizedException("请重新登录");
          }

          try {
            UserDetails userDetails = apiTokenService.getUserDetails(scope,
                apiToken.getUsername());

            apiToken.setAccessToken(apiTokenService.createAccessToken());
            apiToken.setUserDetailsInstantAt(apiTokenService.createUserDetailsInstantAt());
            apiToken.setUserDetails(userDetails);
          } catch (Exception e) {
            throw new UnauthorizedException("请重新登录", e);
          }

        } else {
          UserDetails userDetails = apiTokenService.getUserDetails(grantType, request);
          apiToken = apiTokenService.getApiToken(scope, userDetails);
        }

        UserDetails userDetails = apiToken.getUserDetails();
        Authentication authenticationResult = new UserDetailsAuthenticationToken(userDetails);

        apiTokenRepository.save(apiToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        SecurityContextHolder.setContext(context);

        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Object apiTokenResponse = apiToken.toApiToken();
        if (summerWebProperties.wrapEnable(request)) {
          apiTokenResponse = RespEntity.ok(apiTokenResponse);
        }
        objectMapper.writeValue(response.getOutputStream(), apiTokenResponse);
      } catch (AuthenticationException ex) {
        if (formkey != null) {
          formkeyService.remove(formkey);
          if (log.isTraceEnabled()) {
            log.trace("{} remove:{}", request.getRequestURI(), formkey);
          }
        }
        SecurityContextHolder.clearContext();
        throw ex;
      }
    } else {
      String accessToken = bearerTokenResolver.resolve(request);
      if (StringUtils.hasText(accessToken)) {
        ApiToken apiToken = apiTokenRepository.findByAccessToken(accessToken);
        if (apiToken != null && !apiToken.getAccessToken().isExpired() && ArrayUtil.contains(
            securityProperties.getSupportScopes(), apiToken.getScope())) {
          try {
            String scope = apiToken.getScope();
            UserDetails userDetails = apiToken.getUserDetails();
            apiTokenService.validate(userDetails);

            if (apiToken.getUserDetailsInstantAt().isExpired()) {//刷新userDetails
              try {
                userDetails = apiTokenService.getUserDetails(scope, apiToken.getUsername());
                apiToken.setUserDetailsInstantAt(apiTokenService.createUserDetailsInstantAt());
                apiToken.setUserDetails(userDetails);
                apiTokenRepository.save(apiToken);
              } catch (Exception e) {
                SecurityContextHolder.clearContext();
                apiTokenRepository.remove(apiToken);
                throw e;
              }
            }

            Authentication authenticationResult = new UserDetailsAuthenticationToken(userDetails);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationResult);
            String username = userDetails.getUsername();
            request.setAttribute(HttpOperation.REQUEST_LOGGING_USERNAME, scope + ":" + username);
            SecurityContextHolder.setContext(context);
            if (this.revokeTokenEndpointMatcher.matches(request)) {//撤消token
              if (revokeTokenService != null) {
                revokeTokenService.revokeToken(userDetails);
              }
              apiTokenRepository.remove(apiToken);
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
          if (apiToken != null) {
            String scope = apiToken.getScope();
            if (!ArrayUtil.contains(securityProperties.getSupportScopes(), scope)) {
              logger.warn("不支持token所属scope:" + scope);
            }
          }
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
