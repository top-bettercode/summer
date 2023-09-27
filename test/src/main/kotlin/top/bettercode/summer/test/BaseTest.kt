package top.bettercode.summer.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import top.bettercode.summer.web.BaseController
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.util.function.Supplier

/**
 * 基础测试类
 *
 * @author Peter Wu
 */
@Suppress("SpringBootApplicationProperties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = ["summer.security.enabled=false", "seata.enabled=false"])
abstract class BaseTest : BaseLogTest() {
    protected fun embeddedDatabase(): Boolean {
        return ApplicationContextHolder.getProperty("spring.datasource.url").isNullOrBlank()
    }

    protected fun notFound(): Supplier<out RuntimeException?> {
        return BaseController.notFound()
    }

    protected fun notFound(msg: String?): Supplier<out RuntimeException?> {
        return BaseController.notFound(msg)
    }

}
