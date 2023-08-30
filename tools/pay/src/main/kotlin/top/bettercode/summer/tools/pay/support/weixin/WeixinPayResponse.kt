package top.bettercode.summer.tools.pay.support.weixin

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.support.WeixinPayException

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class WeixinPayResponse(

        /**
         * 返回状态码
         * SUCCESS/FAIL
         *
         * 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
         */
        @field:JsonProperty("return_code")
        var returnCode: String? = null,

        /**
         * 返回信息
         * 返回信息，如非空，为错误原因
         *
         * 签名失败
         *
         * 参数格式校验错误
         */
        @field:JsonProperty("return_msg")
        var returnMsg: String? = null,

) {

    /**
     * 请求结果
     */
    @JsonIgnore
    fun isOk(): Boolean {
        return returnCode == "SUCCESS"
    }

    /**
     * 业务结果
     */
    abstract fun isBizOk(): Boolean
}