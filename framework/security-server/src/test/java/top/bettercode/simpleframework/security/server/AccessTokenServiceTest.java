package top.bettercode.simpleframework.security.server;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.impl.TestApplication;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = RANDOM_PORT)
public class AccessTokenServiceTest {

  @Autowired
  AccessTokenService accessTokenService;

  @Test
  public void getToken() {
    OAuth2Authorization authorization = accessTokenService.getOAuth2Authorization("root");
    System.err.println(StringUtil.valueOf(authorization, true));
  }


}
