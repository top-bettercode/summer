package top.bettercode.summer.security.token;

import java.io.Serializable;
import java.time.Instant;

public class InstantAt implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Instant issuedAt;

  private final Instant expiresAt;

  public InstantAt(Instant issuedAt, Instant expiresAt) {
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
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
