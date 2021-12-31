package top.bettercode.simpleframework.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import top.bettercode.simpleframework.security.config.ApiSecurityProperties;
import top.bettercode.simpleframework.security.impl.TestApplication;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = RANDOM_PORT)
public class SecurityError200Test {

  @Autowired
  ApiSecurityProperties apiSecurityProperties;
  @Autowired
  TestRestTemplate restTemplate;
  final String username = "root";

  @BeforeEach
  public void setUp() {

  }

  @Test
  public void unAuth() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void authInParam() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/test?access_token=", HttpMethod.GET,
            new HttpEntity<>(httpHeaders), String.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());

  }

  @Test
  public void noToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
//    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void invalidToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/test?access_token=xxx", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void testNoAuthInvalidToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/testNoAuth?access_token=xxx", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

//  @Test
  public void expiredToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange(
            "/test?access_token=",
            HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void accessTokenError() {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "trust");
    params.add("useDefaultAuthorization", "true");
    params.add("username", username);
    params.add("password", "wrong password");
    params.add("openId", "");

    ResponseEntity<String> entity = restTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }
}
