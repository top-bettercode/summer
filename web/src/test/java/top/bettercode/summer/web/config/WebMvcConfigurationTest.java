package top.bettercode.summer.web.config;

import org.junit.jupiter.api.Test;
import top.bettercode.summer.test.BaseWebNoAuthTest;

/**
 * @author Peter Wu
 */
class WebMvcConfigurationTest extends BaseWebNoAuthTest {


  @Test
  public void test() throws Exception {
    download(get("/captcha.jpg"), "captcha.jpg");
  }

}