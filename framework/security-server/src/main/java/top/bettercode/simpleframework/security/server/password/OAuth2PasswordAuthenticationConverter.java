package top.bettercode.simpleframework.security.server.password;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import kotlin.collections.SetsKt;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public final class OAuth2PasswordAuthenticationConverter implements AuthenticationConverter {

  @Nullable
  @Override
  public Authentication convert(HttpServletRequest request) {
    // grant_type (REQUIRED)
    String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
    if (!AuthorizationGrantType.PASSWORD.getValue().equals(grantType)) {
      return null;
    }
    String username = request.getParameter(OAuth2ParameterNames.USERNAME);
    Assert.hasText(username, "用户名不能为空");
    String password = request.getParameter(OAuth2ParameterNames.PASSWORD);
    Assert.hasText(password, "密码不能为空");

    Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

    MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);

    // scope (OPTIONAL)
    String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
    if (StringUtils.hasText(scope) &&
        parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
      OAuth2EndpointUtils.throwError(
          OAuth2ErrorCodes.INVALID_REQUEST,
          OAuth2ParameterNames.SCOPE,
          OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
    }
    Set<String> requestedScopes = null;
    if (StringUtils.hasText(scope)) {
      requestedScopes = new HashSet<>(
          Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
    }

    Map<String, Object> additionalParameters = new HashMap<>();
    Set<String> processedParameters = SetsKt.setOf(OAuth2ParameterNames.GRANT_TYPE,
        OAuth2ParameterNames.SCOPE, OAuth2ParameterNames.USERNAME, OAuth2ParameterNames.PASSWORD);
    parameters.forEach((key, value) -> {
      if (!processedParameters.contains(key)) {
        additionalParameters.put(key, value.get(0));
      }
    });

    return new OAuth2PasswordAuthenticationToken(
        clientPrincipal, requestedScopes, username, password, additionalParameters);
  }
}
