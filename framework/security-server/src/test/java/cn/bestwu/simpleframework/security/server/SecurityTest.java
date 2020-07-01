package cn.bestwu.simpleframework.security.server;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import cn.bestwu.simpleframework.security.impl.TestApplication;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, properties = {"api.sign.handler-type-prefix=",
    "logging.level.root=debug"}, webEnvironment = RANDOM_PORT)
public class SecurityTest {

  @Autowired
  ClientDetails clientDetails;
  @Autowired
  TestRestTemplate restTemplate;
  TestRestTemplate clientRestTemplate;

  final String username = "root";
  final String password = DigestUtils.md5DigestAsHex("123456".getBytes());

  @Before
  public void setUp() {
    clientRestTemplate = restTemplate.withBasicAuth(clientDetails.getClientId(),
        clientDetails.getClientSecret());
  }

  @NotNull
  private DefaultOAuth2AccessToken getAccessToken() {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "trust");
//    params.add("useDefaultAuthorization", "true");
    params.add("username", username);
    params.add("password", password);

    ResponseEntity<DefaultOAuth2AccessToken> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params),
            DefaultOAuth2AccessToken.class);
    Assert.assertEquals(HttpStatus.OK, entity.getStatusCode());
    return entity.getBody();
  }

  @Test
  public void accessToken() {
    Assert.assertNotNull(getAccessToken());
  }

  /**
   * 刷新token
   *
   */
  @Test
  public void refreshToken() {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("scope", "trust");
    params.add("refresh_token", getAccessToken().getRefreshToken().getValue());
    ResponseEntity<String> entity2 = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    Assert.assertEquals(HttpStatus.OK, entity2.getStatusCode());

  }

  @Test
  public void revokeToken() {
    String accessToken = getAccessToken().getValue();
    ResponseEntity<String> entity2 = clientRestTemplate
        .exchange("/oauth/token?access_token=" + accessToken,
            HttpMethod.DELETE, null,
            String.class);
    Assert.assertEquals(HttpStatus.NO_CONTENT, entity2.getStatusCode());
  }


}
