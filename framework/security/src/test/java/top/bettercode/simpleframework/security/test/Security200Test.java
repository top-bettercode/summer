package top.bettercode.simpleframework.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.ApiTokenResponse;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.impl.TestApplication;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, properties = {
    "summer.web.wrap-enable=false",
    "summer.web.ok-enable=false",
}, webEnvironment = RANDOM_PORT)
public class Security200Test {

  @Autowired
  TestRestTemplate restTemplate;
  @Autowired
  IResourceService securityService;
  final ObjectMapper objectMapper = new ObjectMapper();

  String username = "root";
  final String password = DigestUtils.md5DigestAsHex("123456".getBytes());

  @BeforeEach
  public void setUp() {
  }

  @NotNull
  private ApiTokenResponse getAccessToken() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "trust");
    params.add("username", username);
    params.add("password", password);

    ResponseEntity<String> entity = restTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    String body = entity.getBody();
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());

    ApiTokenResponse resp = objectMapper.readValue(body, ApiTokenResponse.class);
    return resp;
  }

  @Test
  public void accessToken() throws Exception {
    ApiTokenResponse accessToken = getAccessToken();
    System.err.println(StringUtil.valueOf(accessToken, true));
    org.junit.jupiter.api.Assertions.assertNotNull(accessToken);
  }

  /**
   * 刷新token
   */
  @Test
  public void refreshToken() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("scope", "trust");
    params.add("refresh_token", getAccessToken().getRefresh_token());
    ResponseEntity<String> entity2 = restTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    assertEquals(HttpStatus.OK, entity2.getStatusCode());

  }

  @Test
  public void revokeToken() throws Exception {
    String accessToken = getAccessToken().getAccess_token();
    ResponseEntity<String> entity2 = restTemplate
        .exchange("/oauth/token?access_token=" + accessToken,
            HttpMethod.DELETE, null,
            String.class);
    assertEquals(HttpStatus.NO_CONTENT, entity2.getStatusCode());
  }

}
