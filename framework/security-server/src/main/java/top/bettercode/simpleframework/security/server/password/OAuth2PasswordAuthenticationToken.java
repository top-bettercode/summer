package top.bettercode.simpleframework.security.server.password;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public class OAuth2PasswordAuthenticationToken extends
    OAuth2AuthorizationGrantAuthenticationToken {

  private static final long serialVersionUID = 1676614971836823345L;

  private final Set<String> scopes;
  private final String username;
  private final String password;


  public OAuth2PasswordAuthenticationToken(Authentication clientPrincipal,
      @Nullable Set<String> scopes,
      String username, String password, @Nullable Map<String, Object> additionalParameters) {
    super(AuthorizationGrantType.PASSWORD, clientPrincipal, additionalParameters);
    this.scopes = Collections.unmodifiableSet(
        scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    this.username = username;
    this.password = password;
  }


  public Set<String> getScopes() {
    return this.scopes;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
