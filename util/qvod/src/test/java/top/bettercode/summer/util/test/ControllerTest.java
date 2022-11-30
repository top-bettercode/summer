package top.bettercode.summer.util.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

public class ControllerTest extends BaseWebNoAuthTest {

  @Test
  public void test() throws Exception {
    mockMvc.perform(post("/test")
    ).andExpect(status().isOk());
  }
}
