package top.bettercode.simpleframework.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import top.bettercode.summer.util.test.BaseWebNoAuthTest;

public class ControllerTest extends BaseWebNoAuthTest {

  @Test
  public void test() throws Exception {
    mockMvc.perform(get("/test?price=12&cent=22&a=1585549626000&cell=1822406010")
    ).andExpect(status().isOk());
  }
}
