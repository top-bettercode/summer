package top.bettercode.summer.security;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import top.bettercode.summer.test.BaseWebNoAuthTest;

public class SecurityMvcControllerTest extends BaseWebNoAuthTest {

  @Test
  public void test() throws Exception {
    mockMvc.perform(get("/testDefaultAuth")
    ).andExpect(status().isOk());
  }
}
