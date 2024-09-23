package top.bettercode.summer.tools.hikvision

import com.hikvision.artemis.sdk.ArtemisHttpUtil
import com.hikvision.artemis.sdk.config.ArtemisConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import java.time.ZoneId

class CameraPreviewURLTest : BaseTest() {

    @Autowired
    lateinit var hikvisionProperties: HikvisionProperties

    @Disabled
    @Test
    fun test() {
        /**
         * STEP1：设置平台参数，根据实际情况,设置host appkey appsecret 三个参数.
         */
        ArtemisConfig.host = hikvisionProperties.host.substringAfter("https://") // 平台的ip端口
        ArtemisConfig.appKey = hikvisionProperties.appKey // 密钥appkey
        ArtemisConfig.appSecret = hikvisionProperties.appSecret // 密钥appSecret

        /**
         * STEP2：设置OpenAPI接口的上下文
         */
        val ARTEMIS_PATH = "/artemis"

        /**
         * STEP3：设置接口的URI地址
         */
        val previewURLsApi = "$ARTEMIS_PATH/api/acs/v2/door/events"
        val path: Map<String, String> = mapOf(
            "https://" to previewURLsApi //根据现场环境部署确认是http还是https
        )

        /**
         * STEP4：设置参数提交方式
         */
        val contentType = "application/json"

        /**
         * STEP5：组装请求参数
         */
        val jsonBody: MutableMap<String, Any> = java.util.HashMap()
        jsonBody["pageNo"] = 1
        jsonBody["pageSize"] = 10
        jsonBody["startTime"] =
            java.time.LocalDateTime.of(2024, 9, 21, 0, 0).atZone(ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        jsonBody["endTime"] = java.time.LocalDateTime.now().atZone(ZoneId.systemDefault()).format(
            java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )
        val body = top.bettercode.summer.tools.lang.util.StringUtil.json(jsonBody)

        /**
         * STEP6：调用接口
         */
        val result: String = ArtemisHttpUtil.doPostStringArtemis(
            path, body, null, null, contentType,
            null
        ) // post请求application/json类型参数
        System.err.println(result)
    }
}
