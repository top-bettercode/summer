package top.bettercode.summer.test.web

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest

class WebControllerTest : BaseWebNoAuthTest() {
    @Test
    @Throws(Exception::class)
    fun test() {
        mockMvc.perform(post("/webtest?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
        ).andExpect(MockMvcResultMatchers.status().isOk())
        //    mockMvc.perform(post("/test?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
//    ).andExpect(status().isOk());
    }
}
