package top.bettercode.simpleframework.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
import top.bettercode.simpleframework.security.ApiToken;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.simpleframework.security.impl.TestApplication;
import top.bettercode.simpleframework.web.RespEntity;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, properties = {
    "summer.web.ok-enable=false",
}, webEnvironment = RANDOM_PORT)
public class SecurityTest {

  @Autowired
  TestRestTemplate restTemplate;
  @Autowired
  TestRestTemplate clientRestTemplate;
  @Autowired
  ApiSecurityProperties apiSecurityProperties;
  final ObjectMapper objectMapper = new ObjectMapper();

  String username = "root";
  final String password = DigestUtils.md5DigestAsHex("123456".getBytes());


  @BeforeEach
  public void setUp() {
    Autodoc.setCollectionName("????????????");
    Autodoc.requiredHeaders(HttpHeaders.AUTHORIZATION);
    clientRestTemplate = restTemplate.withBasicAuth(apiSecurityProperties.getClientId(),
        apiSecurityProperties.getClientSecret());
  }

  @NotNull
  private ApiToken getApiToken() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "app");
    params.add("username", username);
    params.add("password", password);

    ResponseEntity<String> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params), String.class);
    String body = entity.getBody();
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());

    RespEntity<ApiToken> resp = objectMapper
        .readValue(body, TypeFactory.defaultInstance().constructParametricType(
            RespEntity.class, ApiToken.class));
    return resp.getData();
  }

  @Test
  public void accessToken() throws Exception {
    Autodoc.setDescription("");
    Autodoc.setName("??????accessToken");
    Autodoc.requiredParameters("grant_type", "scope", "username", "password");
    ApiToken accessToken = getApiToken();
    org.junit.jupiter.api.Assertions.assertNotNull(accessToken);
    Thread.sleep(1000);
  }

  /**
   * ??????token
   */
  @Test
  public void refreshToken() throws Exception {
    Autodoc.disable();
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("scope", "app");
    params.add("refresh_token", getApiToken().getRefreshToken());
    Autodoc.enable();
    Autodoc.setName("??????accessToken");
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
    Autodoc.setName("??????accessToken");

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + accessToken);
    ResponseEntity<String> entity2 = restTemplate.exchange("/oauth/token",
        HttpMethod.DELETE, new HttpEntity<>(httpHeaders),
        String.class);
    assertEquals(HttpStatus.NO_CONTENT, entity2.getStatusCode());
  }


  @Test
  public void auth() throws Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiToken().getAccessToken());
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.POST, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void authInParam() throws Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/test?access_token=" + getApiToken().getAccessToken(), HttpMethod.GET,
            new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void authority() throws Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiToken().getAccessToken());
    ResponseEntity<String> entity = restTemplate
        .exchange("/testAuth", HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void noauthority() throws Exception {
    username = "peter";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiToken().getAccessToken());
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
  public void testNoAuthWithToken() throws Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiToken().getAccessToken());
    ResponseEntity<String> entity = restTemplate
        .exchange("/testNoAuth", HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

}
