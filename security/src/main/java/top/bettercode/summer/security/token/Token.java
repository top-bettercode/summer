package top.bettercode.summer.security.token;

import java.time.Instant;

public class Token extends InstantAt {

  private static final long serialVersionUID = 1L;

  private final String tokenValue;

  public Token(String tokenValue, Instant issuedAt, Instant expiresAt) {
    super(issuedAt, expiresAt);
    this.tokenValue = tokenValue;
  }


  public String getTokenValue() {
    return tokenValue;
  }

}
