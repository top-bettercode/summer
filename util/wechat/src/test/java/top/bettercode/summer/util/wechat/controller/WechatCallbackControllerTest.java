package top.bettercode.summer.util.wechat.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cdwintech.test.wechat.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.summer.util.test.BaseWebNoAuthTest;

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = TestApplication.class)
public class WechatCallbackControllerTest extends BaseWebNoAuthTest {

  @Test
  public void miniOauth() throws Exception {
    Autodoc.setDescription("<<_xiaochengxudengluliucheng>>");
    mockMvc.perform(post("/wechat/miniOauth")
        .param("code", "xxx")
    ).andExpect(status().isOk());
  }

  @Test
  public void sign() throws Exception {
    mockMvc.perform(get("/wechat/jsSign")
        .param("url", "http://xxx.com")
    ).andExpect(status().isOk());
  }
}