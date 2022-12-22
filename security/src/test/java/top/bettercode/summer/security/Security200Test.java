package top.bettercode.summer.security;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import top.bettercode.summer.security.config.ApiSecurityProperties;
import top.bettercode.summer.security.token.ApiAccessToken;
import top.bettercode.summer.test.autodoc.Autodoc;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, properties = {
    "summer.web.wrap-enable=false",
}, webEnvironment = RANDOM_PORT)
public class Security200Test {

  @Autowired
  TestRestTemplate restTemplate;
  @Autowired
  TestRestTemplate clientRestTemplate;
  @Autowired
  ApiSecurityProperties apiSecurityProperties;
  final ObjectMapper objectMapper = new ObjectMapper();

  final String username = "root";
  final String password = DigestUtils.md5DigestAsHex("123456".getBytes());


  @BeforeEach
  public void setUp() {
    Autodoc.setCollectionName("登录授权");
    Autodoc.requiredHeaders(HttpHeaders.AUTHORIZATION);
    clientRestTemplate = restTemplate.withBasicAuth(apiSecurityProperties.getClientId(),
        apiSecurityProperties.getClientSecret());
  }

  @NotNull
  private ApiAccessToken getApiAccessToken(String tag) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "app");
    params.add("username", username);
    params.add("password", password);

    HttpHeaders headers = new HttpHeaders();
    if (StringUtils.hasText(tag)) {
      headers.add("tag", tag);
    }

    ResponseEntity<String> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params, headers), String.class);
    String body = entity.getBody();
    Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());

    return objectMapper.readValue(body, ApiAccessToken.class);
  }

  @Test
  public void accessToken() throws Exception {
    Autodoc.setDescription("");
    Autodoc.setName("获取accessToken");
    Autodoc.requiredParameters("grant_type", "scope", "username", "password");
    ApiAccessToken accessToken = getApiAccessToken(null);
    Assertions.assertNotNull(accessToken);
    Thread.sleep(1000);
  }

  /**
   * 刷新token
   */
  @Test
  public void refreshToken() throws Exception {
    Autodoc.disable();
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("scope", "app");
    params.add("refresh_token", getApiAccessToken("refresh_token").getRefreshToken());
    Autodoc.enable();
    Autodoc.setName("刷新accessToken");
    Autodoc.requiredParameters("grant_type", "scope", "refresh_token");
    ResponseEntity<String> entity2 = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    Assertions.assertEquals(HttpStatus.OK, entity2.getStatusCode());
    Thread.sleep(1000);
  }

  @Test
  public void revokeToken() throws Exception {
    Autodoc.disable();
    String accessToken = getApiAccessToken("revokeToken").getAccessToken();
    Autodoc.enable();
    Autodoc.setName("撤销accessToken");

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + accessToken);
    ResponseEntity<String> entity2 = restTemplate.exchange("/oauth/token",
        HttpMethod.DELETE, new HttpEntity<>(httpHeaders),
        String.class);
    Assertions.assertEquals(HttpStatus.OK, entity2.getStatusCode());
  }


  @Test
  public void auth() throws Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiAccessToken("auth").getAccessToken());
    ResponseEntity<String> entity = restTemplate
        .exchange("/testDefaultAuth", HttpMethod.POST,
            new HttpEntity<>(Collections.singletonMap("aa", "xxx"), httpHeaders), String.class);
    Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }


}
