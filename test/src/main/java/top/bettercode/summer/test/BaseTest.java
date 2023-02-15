package top.bettercode.summer.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;
import top.bettercode.summer.web.support.ApplicationContextHolder;

/**
 * 基础测试类
 *
 * @author Peter Wu
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "summer.security.enabled=false",
    "seata.enabled=false",
})
public abstract class BaseTest extends BaseLogTest {


  protected boolean embeddedDatabase() {
    return !StringUtils.hasText(ApplicationContextHolder.getProperty("spring.datasource.url"));
  }
}

