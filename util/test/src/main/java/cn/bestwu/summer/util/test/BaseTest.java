package cn.bestwu.summer.util.test;

import cn.bestwu.autodoc.gen.AutodocSetting;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 基础测试类
 *
 * @author Peter Wu
 */
@ExtendWith(value = {SpringExtension.class, AutodocSetting.class})
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "api.sign.handler-type-prefix=",
        "spring.jackson.default-property-inclusion=USE_DEFAULTS",
        "logging.level.root=debug",
        "logging.level.org.springframework.test=warn",
        "logging.level.org.springframework.boot.test=warn",
    })
public abstract class BaseTest {

}

