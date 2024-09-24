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
     *  门禁点事件查询:
     * 	刷卡+指纹认证通过 	196885
     * 	指纹+密码认证通过 	196887
     * 	合法卡比对通过 	198914
     * 	刷卡+密码认证通过 	198915
     * 	密码错误 	197121
     * 	输入卡号错误 	197122
     * 	输入密码而非卡号 	197123
     * 	卡号长度错误 	197124
     * 	卡号数字检查错误 	197125
     * 	无此卡号 	197634
     * 	卡号过期 	197633
     * 	刷卡+密码不开门 	198145
     * 	卡未分配权限 	197635
     * 	刷卡+指纹+密码通过 	196886
     * 	工号+密码认证通过 	196897
     * 	指纹比对通过 	197127
     * 	指纹比对失败 	197128
     * 	刷卡+指纹+密码失败 	197136
     * 	刷卡+指纹+密码超时 	197137
     * 	指纹不存在 	197140
     * 	工号+密码认证失败 	197158
     * 	人脸+指纹认证通过 	196888
     * 	人脸+指纹认证失败 	197141
     * 	人脸+指纹认证超时 	197142
     * 	人脸+密码认证通过 	196889
     * 	人脸+密码认证失败 	197143
     * 	人脸+密码认证超时 	197144
     * 	人脸+刷卡认证通过 	196890
     * 	人脸+刷卡认证失败 	197145
     * 	人脸+刷卡认证超时 	197146
     * 	人脸+密码+指纹认证通过 	196891
     * 	人脸+密码+指纹认证失败 	197147
     * 	人脸+密码+指纹认证超时 	197148
     * 	人脸+刷卡+指纹认证通过 	196892
     * 	人脸+刷卡+指纹认证失败 	197149
     * 	人脸+刷卡+指纹认证超时 	197150
     * 	人脸认证通过 	196893
     * 	人脸认证失败 	197151
     * 	智能分析失败 	197160
     * 	真人检测失败 	197161
     * 	人证比对通过 	197162
     * 	人证比对失败 	197163
     * 	首卡比对通过 	196874
     * 	行动不便人士卡比对通过 	196875
     * 	多重认证成功 	196883
     * 	多重认证超级密码成功 	196884
     * 	巡查卡比对通过 	198918
     * 	超级卡比对通过 	198921
     * 	胁迫卡比对通过 	199425
     * 	反潜回认证失败 	197383
     * 	卡不在多重认证时段内 	197392
     * 	多重认证超级密码错误 	197393
     * 	多重认证远程认证失败 	197394
     * 	行动不便人士卡未分配权限 	197395
     * 	胁迫卡未分配权限 	197396
     * 	超级卡未分配权限 	197397
     * 	多重认证重复认证 	197400
     * 	多重认证超时 	197401
     * 	互锁中无法开门 	198146
     * 	反潜回读卡器刷卡无效 	198149
     * 	巡查卡无效时段 	197399
     * 	互锁门未关闭 	198658
     * 	黑名单事件 	197889
     * 	时段组错误 	197384
     * 	假期权限不合 	197378
     * 	权限不合 	197377
     * 	指纹+密码认证失败 	197138
     * 	刷卡+指纹认证失败 	197134
     * 	卡号认证超次报警 	199429
     * 	解除警报 	199681
     * 	胁迫报警 	199428
     */
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