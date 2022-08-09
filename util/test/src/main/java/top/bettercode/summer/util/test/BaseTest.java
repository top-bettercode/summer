package top.bettercode.summer.util.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.support.ApplicationContextHolder;

/**
 * 基础测试类
 *
 * @author Peter Wu
 */
@ExtendWith(value = {SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "summer.security.enabled=false"
})
public abstract class BaseTest extends BaseLogTest{


  protected boolean embeddedDatabase() {
    return !StringUtils.hasText(ApplicationContextHolder.getProperty("spring.datasource.url"));
  }
}

