package top.bettercode.summer.tools.hikvision

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.hikvision.entity.EventRequest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
internal class HikvisionClientTest : BaseTest() {
    @Autowired
    var hikvisionClient: HikvisionClient? = null

    @Disabled
    @Test
    fun request() {
        val request = EventRequest()
        request.pageNo = 1
        request.pageSize = 10
        request.eventTypes = arrayOf(196893)
        request.startTime = LocalDateTime.of(2024, 9, 20, 0, 0).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        request.endTime = LocalDateTime.of(2024, 9, 20, 18, 0).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        hikvisionClient!!.getEvents(request)
    }
}