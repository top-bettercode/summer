package top.bettercode.summer.test.web

import org.junit.jupiter.api.Test
import top.bettercode.summer.test.BaseWebNoAuthTest

class WebControllerTest : BaseWebNoAuthTest() {
    @Test
    fun test() {
        perform(post("/webtest?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
        )
        //    mockMvc.perform(post("/test?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
//    ).andExpect(status().isOk());
    }
}
