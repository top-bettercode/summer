package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class CloseOrderResponse(

        /**
         * 应用ID；必填；微信开放平台审核通过的应用APPID；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名；必填；签名，详见签名生成算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 业务结果；必填；SUCCESS/FAIL；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 业务结果描述；必填；对于业务执行的详细描述；示例：OK
         */
        @field:JsonProperty("result_msg")
        var resultMsg: String? = null,
        /**
         * 错误代码；非必填；详细参见第6节错误列表；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；结果信息描述；示例：系统错误
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode
    }

}
