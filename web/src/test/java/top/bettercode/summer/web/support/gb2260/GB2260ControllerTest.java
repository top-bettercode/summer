package top.bettercode.summer.web.support.gb2260;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.bettercode.summer.test.BaseWebNoAuthTest;
import top.bettercode.summer.web.TestApplication;

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = TestApplication.class)
public class GB2260ControllerTest extends BaseWebNoAuthTest {

  @Test
  void list() throws Exception {
    perform(get("/divisions/list"));
  }

  @Test
  void select() throws Exception {
    perform(
        get("/divisions/select")
            .param("code", "")
    );
  }

}
