package cn.bestwu.simpleframework.security.resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.bestwu.simpleframework.security.impl.TestApplication;
import cn.bestwu.summer.util.test.BaseWebNoAuthTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class MvcControllerTest extends BaseWebNoAuthTest {


  @Test
  public void shipBases() throws Exception {
    mockMvc.perform(get("/test")
    ).andExpect(status().isOk());
  }
}
