package top.bettercode.summer.security

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest

class SecurityMvcControllerTest : BaseWebNoAuthTest() {
    @Test
    @Throws(Exception::class)
    fun test() {
        mockMvc.perform(get("/testDefaultAuth")
        ).andExpect(MockMvcResultMatchers.status().isOk())
    }
}
