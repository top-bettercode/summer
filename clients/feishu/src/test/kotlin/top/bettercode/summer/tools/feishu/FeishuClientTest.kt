package top.bettercode.summer.tools.feishu

import com.lark.oapi.Client
import com.lark.oapi.service.attendance.v1.model.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 *
 * @author Peter Wu
 */
//@Disabled
class FeishuClientTest : BaseTest() {

    @Autowired
    lateinit var feishuClient: Client

    @Test
    fun queryUserFlows() {
        feishuClient.attendance().userFlow()
            .query(
                QueryUserFlowReq.newBuilder()
                    .employeeType("employee_no")
                    .queryUserFlowReqBody(
                        QueryUserFlowReqBody.newBuilder()
                            .userIds(arrayOf("116325"))
                            .checkTimeFrom(
                                "${
                                    LocalDateTime.of(2024, 9, 25, 0, 0)
                                        .toEpochSecond(ZoneOffset.ofHours(8))
                                }"
                            )
                            .checkTimeTo(
                                "${
                                    LocalDateTime.of(2024, 9, 30, 23, 59)
                                        .toEpochSecond(ZoneOffset.ofHours(8))
                                }"
                            )
                            .build()
                    )
                    .build()
            )
    }

    @Test
    fun createUserFlows() {
        val resp = feishuClient.attendance().userFlow().batchCreate(
            BatchCreateUserFlowReq.newBuilder()
                .employeeType("employee_no")
                .batchCreateUserFlowReqBody(
                    BatchCreateUserFlowReqBody.newBuilder()
                        .flowRecords(
                            arrayOf(
                                UserFlow.newBuilder()
                                    .userId("1163256")
                                    .creatorId("116325")
                                    .locationName("测试")
                                    .checkTime(
                                        "${
                                            LocalDateTime.of(2024, 11, 27, 8, 2)
                                                .toEpochSecond(ZoneOffset.ofHours(8))
                                        }"
                                    )
                                    .comment("测试")
                                    .externalId("1237")
                                    .build(),
                                UserFlow.newBuilder()
                                    .userId("116325")
                                    .creatorId("116325")
                                    .locationName("测试")
                                    .checkTime(
                                        "${
                                            LocalDateTime.of(2024, 11, 27, 8, 2)
                                                .toEpochSecond(ZoneOffset.ofHours(8))
                                        }"
                                    )
                                    .comment("测试")
                                    .externalId("12379090")
                                    .build()

                            )
                        ).build()
                )
                .build()
        )

        val code = resp.code
        val msg = resp.msg
        System.err.println(code)
        System.err.println(msg)
    }

}