package top.bettercode.simpleframework.security.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.bettercode.simpleframework.security.impl.TestApplication;
import top.bettercode.summer.util.test.BaseWebNoAuthTest;

@SpringBootTest(classes = TestApplication.class)
public class MvcControllerTest extends BaseWebNoAuthTest {


  @Test
  public void shipBases() throws Exception {
    mockMvc.perform(get("/test")
    ).andExpect(status().isOk());
  }
}
