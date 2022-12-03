package top.bettercode.summer.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import top.bettercode.summer.test.BaseWebNoAuthTest;

public class WebControllerTest extends BaseWebNoAuthTest {

  @Test
  public void test() throws Exception {
    mockMvc.perform(post("/webtest?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
    ).andExpect(status().isOk());
//    mockMvc.perform(post("/test?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
//    ).andExpect(status().isOk());
  }
}
