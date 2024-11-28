package top.bettercode.summer.tools.feishu

import com.lark.oapi.Client
import com.lark.oapi.core.httpclient.OkHttpTransport
import com.lark.oapi.okhttp.OkHttpClient
import top.bettercode.summer.logging.annotation.LogMarker
import java.util.concurrent.TimeUnit

/**
 * 飞书
 *
 * @author Peter Wu
 */
@LogMarker(FeishuClientBuilder.MARKER)
open class FeishuClientBuilder(
    properties: FeishuProperties
) {

    companion object {
        const val MARKER = "feishu"

        @JvmField
        val MAX_PAGE_SIZE = 50
    }

    val client: Client = Client.newBuilder(properties.appId, properties.appSecret)
        .httpTransport(
            OkHttpTransport(
                OkHttpClient.Builder()
                    .callTimeout(0, TimeUnit.MILLISECONDS)
                    .connectTimeout(properties.connectTimeout.toLong(), TimeUnit.SECONDS)
                    .readTimeout(properties.readTimeout.toLong(), TimeUnit.SECONDS)
                    .addInterceptor(
                        OkHttpLoggingInterceptor(
                            collectionName = "第三方平台",
                            name = properties.platformName,
                            logMarker = MARKER,
                            logClazz = Client::class.java,
                            timeoutAlarmSeconds = properties.timeoutAlarmSeconds
                        )
                    )
                    .build()
            )
        )
        .build()
}