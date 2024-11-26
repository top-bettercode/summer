package top.bettercode.summer.tools.feishu

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.feishu.entity.userflow.UserFlow
import top.bettercode.summer.tools.feishu.entity.userflow.UserFlowRequest
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 *
 * @author Peter Wu
 */
@Disabled
class FeishuClientTest : BaseTest() {

    @Autowired
    lateinit var feishuClient: FeishuClient

    @Test
    fun queryUserFlows() {
        feishuClient.queryUserFlows(
//            employeeType = "employee_id",
            userFlowRequest = UserFlowRequest(
                userIds = arrayOf("116325"),
                checkTimeFrom = "${
                    LocalDateTime.of(2024, 9, 25, 0, 0).toEpochSecond(ZoneOffset.ofHours(8))
                }",
                checkTimeTo = "${
                    LocalDateTime.of(2024, 9, 30, 23, 59).toEpochSecond(ZoneOffset.ofHours(8))
                }"
            )
        )
    }

    @Test
    fun createUserFlows() {
        feishuClient.createUserFlows(
//            employeeType = "employee_id",
            flowRecords = listOf(
                UserFlow(
                    userId = "116325",
                    creatorId = "116325",
                    locationName = "测试",
                    checkTime = "${
                        LocalDateTime.of(2024, 9, 25, 8, 2).toEpochSecond(ZoneOffset.ofHours(8))
                    }",
                    comment = "测试",
                    externalId = "1237",
                ),
                UserFlow(
                    userId = "1163256",
                    creatorId = "116325",
                    locationName = "测试",
                    checkTime = "${
                        LocalDateTime.of(2024, 9, 25, 8, 3).toEpochSecond(ZoneOffset.ofHours(8))
                    }",
                    comment = "测试",
                    externalId = "1237",
                ),
                UserFlow(
                    userId = "1163257",
                    creatorId = "116325",
                    locationName = "测试",
                    checkTime = "${
                        LocalDateTime.of(2024, 9, 25, 8, 3).toEpochSecond(ZoneOffset.ofHours(8))
                    }",
                    comment = "测试",
                    externalId = "1237",
                )
            )
        )
    }

}