package top.bettercode.simpleframework.security;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.time.Instant;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;

/**
 * @author Peter Wu
 */
public class ApiTokenBuild {

  private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20);

  private final ApiSecurityProperties apiSecurityProperties;

  public ApiTokenBuild(
      ApiSecurityProperties apiSecurityProperties) {
    this.apiSecurityProperties = apiSecurityProperties;
  }

  public ApiToken createAccessToken() {
    String tokenValue = new String(
        Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
    Instant now = Instant.now();
    return new ApiToken(tokenValue, now,
        now.plusSeconds(apiSecurityProperties.getAccessTokenValiditySeconds()));
  }

  public ApiToken createRefreshToken() {
    String tokenValue = new String(
        Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
    Instant now = Instant.now();
    return new ApiToken(tokenValue, now,
        now.plusSeconds(apiSecurityProperties.getRefreshTokenValiditySeconds()));
  }

}
