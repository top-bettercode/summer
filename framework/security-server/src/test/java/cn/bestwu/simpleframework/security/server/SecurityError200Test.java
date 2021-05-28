package cn.bestwu.simpleframework.security.server;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import cn.bestwu.simpleframework.security.impl.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, properties = {
    "api.sign.handler-type-prefix=",
    "logging.level.root=debug",
}, webEnvironment = RANDOM_PORT)
public class SecurityError200Test {

  @Deprecated
  @Autowired
  ClientDetails clientDetails;
  @Autowired
  TestRestTemplate restTemplate;
  TestRestTemplate clientRestTemplate;
  final String username = "root";

  @BeforeEach
  public void setUp() {
    clientRestTemplate = restTemplate.withBasicAuth(clientDetails.getClientId(),
        clientDetails.getClientSecret());
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

    ResponseEntity<String> entity = clientRestTemplate
        .postForEntity("/oauth/token", new HttpEntity<>(params),
            String.class);
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }
}
