package top.bettercode.summer.tools.hikvision

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.hikvision.entity.EventRequest
import top.bettercode.summer.tools.lang.property.PropertiesSource
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class HikvisionClientTest : BaseTest() {

    @Autowired
    var hikvisionClient: HikvisionClient? = null

    /**
     *        {
     *         "eventId": "a0925cffd1e67e6eac1a62c823fc7fc5",
     *         "eventName": "acs.acs.eventType.successFace",
     *         "eventTime": "2024-09-20T17:58:14+08:00",
     *         "personId": "6b4908457d9b47b28d547d7ccce6710e",
     *         "cardNo": null,
     *         "personName": "路军",
     *         "orgIndexCode": "1612d7ba-a596-4276-a4fa-42f99199a5d0",
     *         "orgName": "云图控股/股份公司总部/总裁办公室",
     *         "doorName": "B2楼北门进_门_1",
     *         "doorIndexCode": "a161cd4d18bc467f8b2130969c372cf9",
     *         "doorRegionIndexCode": "e3edab7c-f3c8-46df-8490-569a9feed98e",
     *         "picUri": "/pic?5d00=4002l68-do7d18*6=ec80*1o0l1=2649*6236226=8117*pt==218**=s498*cb8660657-09d5252-139oe6-pi0582=003b0",
     *         "svrIndexCode": "b4238ecc-b86b-4099-b335-287d65cbac29",
     *         "eventType": 196893,
     *         "inAndOutType": 1,
     *         "readerDevIndexCode": "fe7a3a6dfd054bef8f40d7b770327189",
     *         "readerDevName": "读卡器_1",
     *         "devIndexCode": "ab6ef42996184485bc363b6426dd6aef",
     *         "devName": "B2楼北门进",
     *         "identityCardUri": null,
     *         "receiveTime": "2024-09-20T17:58:16.801+08:00",
     *         "jobNo": "101035",
     *         "studentId": null,
     *         "certNo": null,
     *         "temp": null
     *       }
     *
     * 门禁点事件查询:
     * 刷卡+指纹认证通过 196885
     * 指纹+密码认证通过 196887
     * 合法卡比对通过 198914
     * 刷卡+密码认证通过 198915
     * 刷卡+指纹+密码通过 196886
     * 工号+密码认证通过 196897
     * 指纹比对通过 197127
     * 人脸+指纹认证通过 196888
     * 人脸+密码认证通过 196889
     * 人脸+刷卡认证通过 196890
     * 人脸+密码+指纹认证通过 196891
     * 人脸+刷卡+指纹认证通过 196892
     * 人脸认证通过 196893
     * 人证比对通过 197162
     * 首卡比对通过 196874
     * 行动不便人士卡比对通过 196875
     * 巡查卡比对通过 198918
     * 超级卡比对通过 198921
     * 胁迫卡比对通过 199425
     */
    @Test
    fun events() {
        val hasData = mutableListOf<String>()
        val eventTypes = PropertiesSource.of("events")
        eventTypes.source.forEach { (msg, type) ->
            val request = EventRequest()
            request.pageNo = 1
            request.pageSize = 10
            request.eventTypes = arrayOf(type.toInt())
            request.startTime = LocalDateTime.of(2019, 9, 20, 0, 0).atOffset(ZoneOffset.ofHours(8))
            request.endTime = LocalDateTime.of(2024, 9, 30, 18, 0).atOffset(ZoneOffset.ofHours(8))
            val events = hikvisionClient!!.getEvents(request)
            if (events.list?.isNotEmpty() == true && events.list?.all { it.jobNo != null } == true) {
                hasData.add("$type:$msg")
            }
        }
        System.err.println(hasData.joinToString("\n"))
    }

    @Test
    fun event() {
        val request = EventRequest()
        request.pageNo = 1
        request.pageSize = 10
        request.eventTypes = arrayOf(197127)
        request.startTime = LocalDateTime.of(2023, 9, 1, 0, 0).atOffset(ZoneOffset.ofHours(8))
        request.endTime = LocalDateTime.of(2024, 9, 30, 23, 59).atOffset(ZoneOffset.ofHours(8))
        val events = hikvisionClient!!.getEvents(request)
        events.list?.forEach {
            println(it.eventTime)
            println(it.eventTime?.toLocalDateTime())
            println(it.eventTime?.withOffsetSameInstant(ZoneOffset.ofHours(8))?.toLocalDateTime())
        }
    }

    @Test
    fun format() {
        System.err.println(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}