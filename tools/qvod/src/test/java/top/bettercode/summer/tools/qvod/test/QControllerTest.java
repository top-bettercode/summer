package top.bettercode.summer.tools.qvod.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import top.bettercode.summer.test.BaseWebNoAuthTest;

public class QControllerTest extends BaseWebNoAuthTest {

  @Test
  public void test() throws Exception {
    mockMvc.perform(post("/test")
    ).andExpect(status().isOk());
  }
}
