package top.bettercode.summer.util.qvod;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import top.bettercode.summer.util.test.BaseWebNoAuthTest;

public class ControllerTest extends BaseWebNoAuthTest {

  @Test
  public void test() throws Exception {
    mockMvc.perform(post("/test")
    ).andExpect(status().isOk());
  }
}
