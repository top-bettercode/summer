package top.bettercode.simpleframework.security;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiToken implements Serializable {

  private static final long serialVersionUID = 914967629530462926L;

  private String token_type;

  private String access_token;

  private Instant expiresAt;

  private String refresh_token;

  private String scope;

  private Map<String, Object> additionalInformation = new HashMap<>();

  public ApiToken() {
  }

  public ApiToken(ApiAuthenticationToken apiAuthenticationToken) {
    Token accessToken = apiAuthenticationToken.getAccessToken();
    UserDetails userDetails = apiAuthenticationToken.getUserDetails();

    this.token_type = "bearer";
    this.access_token = accessToken.getTokenValue();
    this.expiresAt = accessToken.getExpiresAt();
    this.refresh_token = apiAuthenticationToken.getRefreshToken().getTokenValue();
    this.scope = apiAuthenticationToken.getScope();
    this.additionalInformation = userDetails instanceof AdditionalUserDetails
        ? ((AdditionalUserDetails) userDetails).getAdditionalInformation()
        : Collections.emptyMap();
  }

  public int getExpires_in() {
    return expiresAt != null ? Long.valueOf(
            (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000L)
        .intValue() : 0;
  }

  protected void setExpires_in(int delta) {
    expiresAt = Instant.ofEpochMilli(System.currentTimeMillis() + delta);
  }

  public String getAccess_token() {
    return access_token;
  }

  public String getToken_type() {
    return token_type;
  }

  public String getRefresh_token() {
    return refresh_token;
  }

  public String getScope() {
    return scope;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInformation() {
    return additionalInformation;
  }

  public void setToken_type(String token_type) {
    this.token_type = token_type;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public void setRefresh_token(String refresh_token) {
    this.refresh_token = refresh_token;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @JsonAnySetter
  public void setAddress(String name, String value) {
    this.additionalInformation.put(name, value);
  }

  public void setAdditionalInformation(
      Map<String, Object> additionalInformation) {
    this.additionalInformation = additionalInformation;
  }
}
