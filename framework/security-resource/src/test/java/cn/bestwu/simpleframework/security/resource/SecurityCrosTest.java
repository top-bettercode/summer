package cn.bestwu.simpleframework.security.resource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.security.server.AccessTokenService;
import cn.bestwu.simpleframework.security.impl.TestApplication;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, properties = {
    "api.sign.handler-type-prefix=",
    "logging.level.root=debug"}, webEnvironment = RANDOM_PORT)
public class SecurityCrosTest {

  @Autowired
  TestRestTemplate clientRestTemplate;
  @Autowired
  AccessTokenService accessTokenService;

  @Test
  public void getToken() {
    OAuth2AccessToken accessToken = accessTokenService.getAccessToken("root");
    System.err.println(StringUtil.valueOf(accessToken, true));
  }

  @Test
  public void options() {
    Set<HttpMethod> httpMethods = clientRestTemplate.optionsForAllow("/oauth/token");
    System.out.println(httpMethods);
  }

}
