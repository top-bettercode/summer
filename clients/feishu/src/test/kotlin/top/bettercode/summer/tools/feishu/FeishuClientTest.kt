package top.bettercode.summer.tools.feishu

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.feishu.entity.UserFlow
import top.bettercode.summer.tools.feishu.entity.UserFlowRequest
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
            employeeType = "employee_id",
            userFlowRequest = UserFlowRequest(
                userIds = arrayOf("fe75c4b6"),
                checkTimeFrom = "${
                    LocalDateTime.of(2024, 9, 21, 0, 0).toEpochSecond(ZoneOffset.ofHours(8))
                }",
                checkTimeTo = "${
                    LocalDateTime.of(2024, 9, 21, 0, 1).toEpochSecond(ZoneOffset.ofHours(8))
                }"
            )
        )?.forEach {
            println(it)
        }
    }

    @Test
    fun createUserFlows() {
        feishuClient.createUserFlows(
            employeeType = "employee_id",
            flowRecords = listOf(
                UserFlow(
                    userId = "fe75c4b6",
                    creatorId = "fe75c4b6",
                    locationName = "测试",
                    checkTime = "${
                        LocalDateTime.of(2024, 9, 21, 0, 0).toEpochSecond(ZoneOffset.ofHours(8))
                    }",
                    comment = "测试",
                    externalId = "123",
                    idempotentId = "external_id",
                )
            )
        )?.forEach {
            println(it)
        }
    }

}