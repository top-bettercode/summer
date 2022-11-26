package top.bettercode.simpleframework.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.simpleframework.security.ApiAccessToken;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.simpleframework.security.impl.TestApplication;

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
  private ApiAccessToken getApiToken() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "app");
    params.add("username", username);
    params.add("password", password);

    ResponseEntity<String> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    String body = entity.getBody();
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());

    return objectMapper.readValue(body, ApiAccessToken.class);
  }

  @Test
  public void accessToken() throws Exception {
    Autodoc.setDescription("");
    Autodoc.setName("获取accessToken");
    Autodoc.requiredParameters("grant_type", "scope", "username", "password");
    ApiAccessToken accessToken = getApiToken();
    org.junit.jupiter.api.Assertions.assertNotNull(accessToken);
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
    params.add("refresh_token", getApiToken().getRefreshToken());
    Autodoc.enable();
    Autodoc.setName("刷新accessToken");
    Autodoc.requiredParameters("grant_type", "scope", "refresh_token");
    ResponseEntity<String> entity2 = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    assertEquals(HttpStatus.OK, entity2.getStatusCode());
    Thread.sleep(1000);
  }

  @Test
  public void revokeToken() throws Exception {
    Autodoc.disable();
    String accessToken = getApiToken().getAccessToken();
    Autodoc.enable();
    Autodoc.setName("撤销accessToken");

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + accessToken);
    ResponseEntity<String> entity2 = restTemplate.exchange("/oauth/token",
        HttpMethod.DELETE, new HttpEntity<>(httpHeaders),
        String.class);
    assertEquals(HttpStatus.OK, entity2.getStatusCode());
  }


  @Test
  public void auth() throws Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer "+getApiToken().getAccessToken());
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.POST,
            new HttpEntity<>(Collections.singletonMap("aa", "xxx"), httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }


}
