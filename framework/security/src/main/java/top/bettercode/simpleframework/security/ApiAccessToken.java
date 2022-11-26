package top.bettercode.simpleframework.security;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiAccessToken implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("access_token")
  private String accessToken;

  private Instant expiresAt;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("scope")
  private String scope;

  private Map<String, Object> additionalInformation = new HashMap<>();

  private ApiToken apiAuthenticationToken;

  public ApiAccessToken() {
  }

  public ApiAccessToken(ApiToken apiAuthenticationToken) {
    this.apiAuthenticationToken = apiAuthenticationToken;
    Token accessToken = apiAuthenticationToken.getAccessToken();
    UserDetails userDetails = apiAuthenticationToken.getUserDetails();

    this.tokenType = "bearer";
    this.accessToken = accessToken.getTokenValue();
    this.expiresAt = accessToken.getExpiresAt();
    this.refreshToken = apiAuthenticationToken.getRefreshToken().getTokenValue();
    this.scope = apiAuthenticationToken.getScope();
    this.additionalInformation = userDetails instanceof AdditionalUserDetails
        ? ((AdditionalUserDetails) userDetails).getAdditionalInformation()
        : Collections.emptyMap();
  }

  @JsonIgnore
  public ApiToken getApiAuthenticationToken() {
    return apiAuthenticationToken;
  }

  @JsonIgnore
  public UserDetails getUserDetails() {
    return apiAuthenticationToken.getUserDetails();
  }

  //--------------------------------------------

  @JsonProperty("expires_in")
  public int getExpiresIn() {
    return expiresAt != null ? Long.valueOf(
            (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000L)
        .intValue() : 0;
  }

  protected void setExpiresIn(int delta) {
    expiresAt = Instant.ofEpochSecond(System.currentTimeMillis() / 1000L + delta);
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getScope() {
    return scope;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInformation() {
    return additionalInformation;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
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
