package top.bettercode.simpleframework.security;

import java.io.Serializable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public class ApiAuthenticationToken implements Serializable {

  private static final long serialVersionUID = -6193857647931135747L;

  private String scope;

  private ApiToken accessToken;

  private ApiToken refreshToken;

  private UserDetails userDetails;

  public ApiAuthenticationToken() {
  }

  public ApiAuthenticationToken(String scope, ApiToken accessToken,
      ApiToken refreshToken, UserDetails userDetails) {
    this.scope = scope;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.userDetails = userDetails;
  }

  public String getId() {
    return userDetails.getUsername();
  }

  public ApiTokenResponse tokenResponse(){
    return new ApiTokenResponse(this);
  }

  //--------------------------------------------

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public ApiToken getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(ApiToken accessToken) {
    this.accessToken = accessToken;
  }

  public ApiToken getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(ApiToken refreshToken) {
    this.refreshToken = refreshToken;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public void setUserDetails(UserDetails userDetails) {
    this.userDetails = userDetails;
  }
}
