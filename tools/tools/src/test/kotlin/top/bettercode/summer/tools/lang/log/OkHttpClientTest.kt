package top.bettercode.summer.tools.lang.log

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import org.junit.jupiter.api.Test


class OkHttpClientTest {

    @Test
    fun log() {
        // 创建 OkHttpClient 实例
        val client = OkHttpClient.Builder()
                .addInterceptor(OkHttpClientLoggingInterceptor("第三方服务", "测试", "test")) // 添加日志拦截器
                .build()

        // 创建一个 Request 对象，表示要发送的请求
        val request: Request = Builder()
                .url("https://jsonplaceholder.typicode.com:443/posts/1") // 请求的URL
//                .post("{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .build()

        // 使用 OkHttpClient 执行请求
        val call = client.newCall(request)
        // 同步执行请求，并获取响应
        val response = call.execute()

        // 获取响应的状态码
        val statusCode = response.code

        // 获取响应的内容
        val responseBody = response.body!!.string()

        // 输出响应内容和状态码
        println("Response Code: $statusCode")
        println("Response Body:\n$responseBody")
    }
}
