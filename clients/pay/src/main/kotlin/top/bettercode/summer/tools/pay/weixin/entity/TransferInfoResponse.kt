package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class TransferInfoResponse(

        /**
         * 业务结果；必填；SUCCESS/FAIL ，非付款标识，付款是否成功需要查看status字段来判断；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码；非必填；错误码信息；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；结果信息描述 以下字段在return_code 和result_code都为SUCCESS的时候有返回；示例：系统错误
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 商户单号；必填；商户使用查询API填写的单号的原路返回.；示例：10000098201411111234567890
         */
        @field:JsonProperty("partner_trade_no")
        var partnerTradeNo: String? = null,
        /**
         * Appid；必填；商户号的appid；示例：wxe062425f740d30d8
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：10000098
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 付款单号；必填；调用付款API时，微信支付系统内部产生的单号；示例：1000000000201503283103439304
         */
        @field:JsonProperty("detail_id")
        var detailId: String? = null,
        /**
         * 转账状态；必填；SUCCESS:转账成功 FAILED:转账失败 PROCESSING:处理中；示例：SUCCESS
         */
        @field:JsonProperty("status")
        var status: String? = null,
        /**
         * 失败原因；非必填；如果失败则有失败原因；示例：余额不足
         */
        @field:JsonProperty("reason")
        var reason: String? = null,
        /**
         * 收款用户openid；必填；转账的openid；示例：oxTWIuGaIt6gTKsQRLau2M0yL16E
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 收款用户姓名；非必填；收款用户姓名；示例：马华
         */
        @field:JsonProperty("transfer_name")
        var transferName: String? = null,
        /**
         * 付款金额；必填；付款金额单位为“分”；示例：5000
         */
        @field:JsonProperty("payment_amount")
        var paymentAmount: Int? = null,
        /**
         * 转账时间；必填；发起转账的时间；示例：2015-04-21 20:00:00
         */
        @field:JsonProperty("transfer_time")
        var transferTime: String? = null,
        /**
         * 付款成功时间；必填；付款成功时间；示例：2015-04-21 20:01:00
         */
        @field:JsonProperty("payment_time")
        var paymentTime: String? = null,
        /**
         * 付款备注；必填；付款备注；示例：车险理赔
         */
        @field:JsonProperty("desc")
        var desc: String? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode
    }

    fun isStatusOk(): Boolean {
        return "SUCCESS" == status
    }
}
