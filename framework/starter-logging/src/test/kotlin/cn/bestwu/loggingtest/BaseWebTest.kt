package cn.bestwu.loggingtest

import cn.bestwu.logging.RequestLoggingFilter
import cn.bestwu.logging.RequestLoggingProperties
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
import org.springframework.web.context.WebApplicationContext

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = ["logging.level.root=info"])
abstract class BaseWebTest {

    @Autowired
    private lateinit var context: WebApplicationContext
    protected lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        val properties = RequestLoggingProperties()
        properties.isIncludeRequestBody = true
        properties.isIncludeResponseBody = true
        properties.isFormat = true
        properties.encryptHeaders = arrayOf("token")
        properties.encryptParameters = arrayOf("password")
        mockMvc = webAppContextSetup(context).addFilter<DefaultMockMvcBuilder>(RequestLoggingFilter(properties,
                emptyList())).build()
    }

}
