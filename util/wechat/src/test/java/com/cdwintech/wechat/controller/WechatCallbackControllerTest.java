package com.cdwintech.wechat.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.bestwu.summer.util.test.BaseWebNoAuthTest;
import om.cdwintech.test.wechat.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = TestApplication.class, properties = {"api.sign.handler-type-prefix=",
    "spring.jackson.default-property-inclusion=USE_DEFAULTS", "logging.level.root=debug",
    "logging.level.org.hibernate.SQL=debug",
    "logging.request.timeout-alarm-seconds=30"})
public class WechatCallbackControllerTest extends BaseWebNoAuthTest {


  @Test
  public void sign() throws Exception {
    mockMvc.perform(get("/wechat/jsSign")
        .param("url", "http://xxx.com")
    ).andExpect(status().isOk());
  }

}