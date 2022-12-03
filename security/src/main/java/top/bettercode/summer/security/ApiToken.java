package top.bettercode.summer.security;

import java.io.Serializable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public class ApiToken implements Serializable {

  private static final long serialVersionUID = 1L;

  private String scope;

  private Token accessToken;

  private Token refreshToken;

  private  InstantAt userDetailsInstantAt;

  private UserDetails userDetails;

  public ApiToken() {
  }

  public ApiToken(String scope, Token accessToken,
      Token refreshToken, InstantAt userDetailsInstantAt, UserDetails userDetails) {
    this.scope = scope;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.userDetailsInstantAt = userDetailsInstantAt;
    this.userDetails = userDetails;
  }

  public String getUsername() {
    return userDetails.getUsername();
  }

  public ApiAccessToken toApiToken(){
    return new ApiAccessToken(this);
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

  public InstantAt getUserDetailsInstantAt() {
    return userDetailsInstantAt;
  }

  public void setUserDetailsInstantAt(
      InstantAt userDetailsInstantAt) {
    this.userDetailsInstantAt = userDetailsInstantAt;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public void setUserDetails(UserDetails userDetails) {
    this.userDetails = userDetails;
  }

}
