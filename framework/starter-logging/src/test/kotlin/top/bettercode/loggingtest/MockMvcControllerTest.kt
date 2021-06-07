package top.bettercode.loggingtest

import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author Peter Wu
 */
class MockMvcControllerTest : BaseWebTest() {

    private val requestBody = "///////////////////////request_body///////////////////////"

    @Test
    @Throws(Exception::class)
    fun test() {
        mockMvc.perform(post("/test")
                .param("username", "1")
                .param("username", "1")
                .param("password", "20")
                .param("word", "中文")
                .param("中文", "中文")
                .content(requestBody)
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testGet() {
        mockMvc.perform(get("/test")
                .param("username", "1")
                .param("username", "1")
                .param("password", "20")
                .param("word", "中文")
                .param("中文", "中文")
                .content(requestBody)
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testGet1() {
        mockMvc.perform(get("/test")
                .content(requestBody)
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testGet2() {
        mockMvc.perform(get("/test")
                .param("username", "1")
                .param("username", "1")
                .param("password", "20")
                .param("word", "中文")
                .param("中文", "中文")
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun testPostForm() {
        mockMvc.perform(post("/test")
                .param("username", "1")
                .param("username", "1")
                .param("password", "20")
                .param("word", "中文")
                .param("中文", "中文")
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk)
    }


    @Test
    fun testNoRead() {
        mockMvc.perform(post("/testNoRead")
                .param("username", "1")
                .param("password", "20")
                .content(requestBody)
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk).andExpect(content().string("null"))
    }

    @Test
    fun testNoReqestbody() {
        mockMvc.perform(post("/testNoRead")
                .param("username", "1")
                .param("password", "20")
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk).andExpect(content().string("null"))
    }

    @Test
    fun encrypted() {
        mockMvc.perform(post("/encrypted")
                .param("username", "1")
                .param("password", "20")
                .header("token", "abcdefg")
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk).andExpect(content().string("ok"))
    }

    @Test
    fun testMultipart() {
        mockMvc.perform(multipart("/multipart")
                .file(MockMultipartFile("file", "filename", null, ClassPathResource("application.yml").inputStream))
                .param("username", "1")
                .param("password", "20")
                .header("token", "abcdefg")
                .header("Accept", "application/json;version=2.0")
                .header("user-agent", "app/2.0 (iPhone; iOS 8.1.2; Scale/2.00)")
        ).andExpect(status().isOk).andExpect(content().string("ok"))
    }
}