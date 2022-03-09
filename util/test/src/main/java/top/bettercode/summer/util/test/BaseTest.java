package top.bettercode.summer.util.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.bettercode.autodoc.gen.AutodocSetting;

/**
 * 基础测试类
 *
 * @author Peter Wu
 */
@ExtendWith(value = {SpringExtension.class, AutodocSetting.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BaseTest {

}

