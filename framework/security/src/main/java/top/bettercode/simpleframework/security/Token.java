package top.bettercode.simpleframework.security;

import java.io.Serializable;
import java.time.Instant;

public class Token implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String tokenValue;

  private final Instant issuedAt;

  private final Instant expiresAt;

  public Token(String tokenValue, Instant issuedAt, Instant expiresAt) {
    this.tokenValue = tokenValue;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  //--------------------------------------------
  public boolean isExpired() {
    return getExpiresAt() != null && Instant.now().isAfter(getExpiresAt());
  }

  public int getExpires_in() {
    return expiresAt != null ? Long.valueOf(
        (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000L).intValue() : -1;
  }

}
