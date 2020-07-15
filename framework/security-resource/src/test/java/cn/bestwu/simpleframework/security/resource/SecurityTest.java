package cn.bestwu.simpleframework.security.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import cn.bestwu.simpleframework.security.impl.TestApplication;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SuppressWarnings("deprecation")
@SpringBootTest(classes = TestApplication.class, properties = {"api.sign.handler-type-prefix=",
    "logging.level.root=debug"}, webEnvironment = RANDOM_PORT)
public class SecurityTest {

  @Autowired
  ClientDetails clientDetails;
  @Autowired
  TestRestTemplate restTemplate;
  TestRestTemplate clientRestTemplate;
  @Autowired
  IResourceService securityService;

  String username = "root";
  final String password = DigestUtils.md5DigestAsHex("123456".getBytes());

  @BeforeEach
  public void setUp() {
    clientRestTemplate = restTemplate.withBasicAuth(clientDetails.getClientId(),
        clientDetails.getClientSecret());
  }

  @NotNull
  private DefaultOAuth2AccessToken getAccessToken() {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "trust");
    params.add("useDefaultAuthorization", "true");
    params.add("username", username);
    params.add("password", password);

    ResponseEntity<DefaultOAuth2AccessToken> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params),
            DefaultOAuth2AccessToken.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    return entity.getBody();
  }

  @Test
  public void accessToken() {
    org.junit.jupiter.api.Assertions.assertNotNull(getAccessToken());
  }

  /**
   * 刷新token
   */
  @Test
  public void refreshToken() {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("scope", "trust");
    params.add("refresh_token", getAccessToken().getRefreshToken().getValue());
    ResponseEntity<String> entity2 = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    assertEquals(HttpStatus.OK, entity2.getStatusCode());

  }

  @Test
  public void revokeToken() {
    String accessToken = getAccessToken().getValue();
    ResponseEntity<String> entity2 = clientRestTemplate
        .exchange("/oauth/token?access_token=" + accessToken,
            HttpMethod.DELETE, null,
            String.class);
    assertEquals(HttpStatus.NO_CONTENT, entity2.getStatusCode());
  }

  @Test
  public void auth() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "bearer " + getAccessToken().getValue());
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void authority() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "bearer " + getAccessToken().getValue());
    ResponseEntity<String> entity = restTemplate
        .exchange("/testAuth", HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void noauthority() {
    username = "peter";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "bearer " + getAccessToken().getValue());
    ResponseEntity<String> entity = restTemplate
        .exchange("/testAuth", HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.FORBIDDEN, entity.getStatusCode());
  }

  @Test
  public void unauthority() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/testAuth", HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void testNoAuth() {
    ResponseEntity<String> entity = restTemplate.getForEntity("/testNoAuth", String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void testtestClientAuthFail() {
    ResponseEntity<String> entity = restTemplate.getForEntity("/testClientAuth", String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void testtestClientAuth() {
    ResponseEntity<String> entity = clientRestTemplate
        .getForEntity("/testClientAuth", String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

}
