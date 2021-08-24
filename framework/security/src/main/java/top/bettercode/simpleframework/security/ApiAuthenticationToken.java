package top.bettercode.simpleframework.security;

import java.io.Serializable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public class ApiAuthenticationToken implements Serializable {

  private static final long serialVersionUID = 1L;

  private String scope;

  private Token accessToken;

  private Token refreshToken;

  private UserDetails userDetails;

  public ApiAuthenticationToken() {
  }

  public ApiAuthenticationToken(String scope, Token accessToken,
      Token refreshToken, UserDetails userDetails) {
    this.scope = scope;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.userDetails = userDetails;
  }

  public String getUsername() {
    return userDetails.getUsername();
  }

  public ApiToken toApiToken(){
    return new ApiToken(this);
  }

  //--------------------------------------------

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public Token getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(Token accessToken) {
    this.accessToken = accessToken;
  }

  public Token getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(Token refreshToken) {
    this.refreshToken = refreshToken;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public void setUserDetails(UserDetails userDetails) {
    this.userDetails = userDetails;
  }
}
