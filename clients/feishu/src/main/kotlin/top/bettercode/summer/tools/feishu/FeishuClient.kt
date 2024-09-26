package top.bettercode.summer.tools.feishu

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.Assert
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.postForObject
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.feishu.entity.UserFlow
import top.bettercode.summer.tools.feishu.entity.UserFlowCreateResults
import top.bettercode.summer.tools.feishu.entity.UserFlowRequest
import top.bettercode.summer.tools.feishu.entity.UserFlowResults
import top.bettercode.summer.tools.lang.ExpiringValue
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.lang.log.feishu.FeishuDataResult
import top.bettercode.summer.tools.lang.log.feishu.FeishuError
import top.bettercode.summer.tools.lang.log.feishu.FeishuResult
import top.bettercode.summer.tools.lang.log.feishu.FeishuTokenResult
import java.time.Duration

/**
 * 推送接口
 *
 * @author Peter Wu
 */
@LogMarker(FeishuClient.MARKER)
open class FeishuClient(
    properties: FeishuProperties
) : ApiTemplate<FeishuProperties>(
    marker = MARKER,
    properties = properties
) {

    companion object {
        const val MARKER = "feishu"

        @JvmField
        val MAX_PAGE_SIZE = 50
    }

    private var token: ExpiringValue<String>? = null

    init {
        val messageConverter = MappingJackson2HttpMessageConverter()
        messageConverter.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        this.messageConverters = messageConverters

        this.errorHandler = object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {
                val body = getResponseBody(response)
                val error = messageConverter.objectMapper.readValue(
                    body,
                    FeishuError::class.java
                )
                throw clientSysException(error.message, error)
            }
        }
    }


    /**
     * https://open.feishu.cn/document/server-docs/authentication-management/access-token/app_access_token_internal
     */
    private fun requestToken(): ExpiringValue<String> {
        val headers = HttpHeaders()
        headers.contentType =
            MediaType(MediaType.APPLICATION_JSON, mapOf("charset" to Charsets.UTF_8.name()))
        val requestEntity = HttpEntity(
            mapOf("app_id" to properties.appId, "app_secret" to properties.appSecret),
            headers
        )
        val authToken: FeishuTokenResult =
            postForObject(
                "${properties.api}/auth/v3/tenant_access_token/internal",
                requestEntity,
                FeishuTokenResult::class
            )
        return ExpiringValue(
            authToken.tenantAccessToken ?: throw clientException("获取飞书token失败"),
            Duration.ofSeconds(authToken.expire!!.toLong())
        )
    }

    private fun getToken(requestToken: Boolean): String {
        synchronized(this) {
            if (requestToken) {
                token = requestToken()
            }
            var value = token?.value
            if (value == null) {
                token = requestToken()
                value = token?.value
            }
            return value ?: throw RuntimeException("获取飞书token失败")
        }
    }

    private fun <T : FeishuResult> request(
        url: String,
        request: Any? = null,
        method: HttpMethod = HttpMethod.POST,
        contentType: MediaType? = null,
        requestToken: Boolean = false,
        responseType: ParameterizedTypeReference<T>? = null,
        responseClass: Class<T>? = null,
        vararg uriVariables: Any?,
    ): T? {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ${getToken(requestToken)}")
        if (contentType != null) {
            headers.contentType = contentType
        }

        val response = if (responseType != null)
            exchange(
                properties.api + url,
                method,
                HttpEntity(request, headers),
                responseType, *uriVariables
            ) else
            exchange(
                properties.api + url,
                method,
                HttpEntity(request, headers),
                responseClass
                    ?: throw RuntimeException("responseClass or responseType must be not null"),
                *uriVariables
            )
        val body = response.body
        if (body != null) {
            if (body.isInvalidAccessToken()) {
                return request(
                    url = url,
                    request = request,
                    method = method,
                    contentType = contentType,
                    requestToken = true,
                    responseType = responseType,
                    responseClass = responseClass,
                    uriVariables = uriVariables
                )
            }
        }
        return body
    }

    private val createFlowResultsType = object :
        ParameterizedTypeReference<FeishuDataResult<UserFlowCreateResults>>() {}

    /**
     * 批量查询打卡流水
     *
     * https://open.feishu.cn/document/server-docs/attendance-v1/user_task/query-2
     *
     * @param employeeType 请求体中的 user_ids 和响应体中的 user_id 的员工ID类型。,employee_no:员工工号，employee_id:员工ID
     * @param includeTerminatedUser 是否包含已离职员工,由于新入职用户可以复用已离职用户的employee_no/employee_id。如果true，返回employee_no/employee_id对应的所有在职+离职用户数据；如果false，只返回employee_no/employee_id对应的在职或最近一个离职用户数据
     *
     */
    @JvmOverloads
    fun queryUserFlows(
        userFlowRequest: UserFlowRequest,
        employeeType: String = "employee_no",
        includeTerminatedUser: Boolean = false
    ): List<UserFlow> {
        val result: FeishuDataResult<UserFlowResults>? =
            request(
                url = "/attendance/v1/user_flows/query?employee_type={0}&include_terminated_user={1}",
                request = userFlowRequest,
                responseType = userFlowResultsType,
                method = HttpMethod.POST,
                uriVariables = arrayOf(employeeType, includeTerminatedUser),
            )

        return result?.data?.userFlowResults ?: throw clientException("批量查询打卡流水失败")
    }

    private val userFlowResultsType = object :
        ParameterizedTypeReference<FeishuDataResult<UserFlowResults>>() {}

    /**
     * 导入打卡流水
     *
     * https://open.feishu.cn/document/server-docs/attendance-v1/user_task/batch_create
     *
     * @param employeeType 请求体中的 user_ids 和响应体中的 user_id 的员工ID类型。,employee_no:员工工号，employee_id:员工ID
     *
     */
    @JvmOverloads
    fun createUserFlows(
        flowRecords: List<UserFlow>,
        employeeType: String = "employee_no",
    ): List<UserFlow> {
        Assert.notEmpty(flowRecords, "打卡流水不能为空")
        Assert.isTrue(flowRecords.size <= MAX_PAGE_SIZE, "打卡流水不能超过${MAX_PAGE_SIZE}条")
        val result: FeishuDataResult<UserFlowCreateResults>? =
            request(
                url = "/attendance/v1/user_flows/batch_create?employee_type={0}",
                request = mapOf("flow_records" to flowRecords),
                responseType = createFlowResultsType,
                method = HttpMethod.POST,
                uriVariables = arrayOf(employeeType),
            )
        if (result?.msg.isNullOrBlank()) {
            return result?.data?.flow_records ?: throw clientException(
                result?.msg ?: "导入打卡流水失败", result
            )
        } else {
            throw clientException(
                result?.msg ?: "导入打卡流水失败", result
            )
        }
    }

}