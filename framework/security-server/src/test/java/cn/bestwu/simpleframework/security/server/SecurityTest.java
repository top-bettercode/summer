package cn.bestwu.simpleframework.security.server;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import cn.bestwu.simpleframework.security.impl.TestApplication;
import cn.bestwu.simpleframework.web.RespEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
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
@SpringBootTest(classes = TestApplication.class, properties = {
    "summer.sign.handler-type-prefix=",
    "logging.level.root=debug",
}, webEnvironment = RANDOM_PORT)
public class SecurityTest {

  @Deprecated
  @Autowired
  ClientDetails clientDetails;
  @Autowired
  TestRestTemplate restTemplate;
  TestRestTemplate clientRestTemplate;
  final ObjectMapper objectMapper = new ObjectMapper();

  final String username = "root";
  final String password = DigestUtils.md5DigestAsHex("123456".getBytes());

  @BeforeEach
  public void setUp() {
    clientRestTemplate = restTemplate.withBasicAuth(clientDetails.getClientId(),
        clientDetails.getClientSecret());
  }

  @Deprecated
  @NotNull
  private DefaultOAuth2AccessToken getAccessToken() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "trust");
    params.add("username", username);
    params.add("password", password);

    ResponseEntity<String> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
    String body = entity.getBody();

    RespEntity<DefaultOAuth2AccessToken> resp = objectMapper
        .readValue(body, TypeFactory.defaultInstance().constructParametricType(
            RespEntity.class, DefaultOAuth2AccessToken.class));
    return resp.getData();

  }

  @Test
  public void options() {
    Set<HttpMethod> httpMethods = clientRestTemplate.optionsForAllow("/oauth/token");
    System.out.println(httpMethods);
  }


  @Test
  public void accessToken() throws Exception {
    org.junit.jupiter.api.Assertions.assertNotNull(getAccessToken());
  }

  /**
   * 刷新token
   */
  @Test
  public void refreshToken() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("scope", "trust");
    params.add("refresh_token", getAccessToken().getRefreshToken().getValue());
    ResponseEntity<String> entity2 = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity2.getStatusCode());

  }

  @Test
  public void revokeToken() throws Exception {
    String accessToken = getAccessToken().getValue();
    ResponseEntity<String> entity2 = clientRestTemplate
        .exchange("/oauth/token?access_token=" + accessToken,
            HttpMethod.DELETE, null,
            String.class);
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity2.getStatusCode());
  }


}
