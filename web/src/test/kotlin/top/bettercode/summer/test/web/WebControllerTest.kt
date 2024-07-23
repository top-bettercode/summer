package top.bettercode.summer.test.web

import org.junit.jupiter.api.Test
import top.bettercode.summer.test.BaseWebNoAuthTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.math.log10

class WebControllerTest : BaseWebNoAuthTest() {

    @Test
    fun temp() {
        System.err.println(log10(1000.0))
    }

    @Test
    fun test() {
        perform(
            post("/webtest?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
        )
        //    mockMvc.perform(post("/test?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1")
//    ).andExpect(status().isOk());
    }

    @Test
    fun testDate() {
        perform(
            post("/testDate")
                .contentType("application/json")
                .content(json(DateData(Date(), LocalDate.now(), LocalDateTime.now())))
        )
    }

}
