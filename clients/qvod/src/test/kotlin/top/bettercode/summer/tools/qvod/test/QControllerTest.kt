package top.bettercode.summer.tools.qvod.test

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest


class QControllerTest : BaseWebNoAuthTest() {
    @Test
    fun test() {
        mockMvc.perform(post("/test")
        ).andExpect(MockMvcResultMatchers.status().isOk())
    }
}
