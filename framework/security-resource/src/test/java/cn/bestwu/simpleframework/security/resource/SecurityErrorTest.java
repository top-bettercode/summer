package cn.bestwu.simpleframework.security.resource;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import cn.bestwu.simpleframework.security.impl.TestApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, properties = {"api.sign.handler-type-prefix=",
    "logging.level.root=debug"}, webEnvironment = RANDOM_PORT)
public class SecurityErrorTest {

  @Autowired
  ClientDetails clientDetails;
  @Autowired
  TestRestTemplate restTemplate;
  TestRestTemplate clientRestTemplate;
  String username = "root";

  @Before
  public void setUp() {
    clientRestTemplate = restTemplate.withBasicAuth(clientDetails.getClientId(),
        clientDetails.getClientSecret());
  }

  @Test
  public void unAuth() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void unAuthUseClient() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = clientRestTemplate
        .exchange("/test", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void noToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
//    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    ResponseEntity<String> entity = restTemplate
        .exchange("/test", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void invalidToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
//    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    ResponseEntity<String> entity = restTemplate
        .exchange("/test?access_token=xxx", HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void expiredToken() {
    HttpHeaders httpHeaders = new HttpHeaders();
    ResponseEntity<String> entity = restTemplate
        .exchange(
            "/test?access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MzMxNjExMjcsInVzZXJfbmFtZSI6InJvb3QiLCJhdXRob3JpdGllcyI6WyJhdXRoZW50aWNhdGVkIl0sImp0aSI6IjlhNTdiOGE4LTc5MDQtNDU3YS04YWQzLWVlYTYyMDhkNmM3ZCIsImNsaWVudF9pZCI6IjBpOXdPdEc1VkdXYVVpU0wxZHZWTlQxeVQ1U29qWW1qIiwic2NvcGUiOlsidHJ1c3QiXX0.etKB993mcwO6-_2EldCd2Evg24USQVxOfk-FZVZxwf7GyOILyuCmCFdhNh7IfvsyV1DRf0gaAl5gXUPhP9RCQQ7WQ6B4Kn3mSoR08N425RTj1nUXAc68gLS87E6GsfKMmboO7yRJOAjkvqFSw4evoOeKAXtAwNde9t5U9xRgLdo",
            HttpMethod.GET, new HttpEntity<>(httpHeaders),
            String.class);
    System.err.println(entity.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
  }

  @Test
  public void accessTokenError() throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("scope", "trust");
    params.add("useDefaultAuthorization", "true");
    params.add("username", username);
    params.add("password", "wrong password");
    params.add("openId", "");

    ResponseEntity<String> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params),
            String.class);
    System.err.println(entity.getBody());
    Thread.sleep(5 * 1000);
    Assert.assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
  }
}