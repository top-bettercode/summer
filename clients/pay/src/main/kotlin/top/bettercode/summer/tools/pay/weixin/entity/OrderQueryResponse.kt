package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class OrderQueryResponse(
        /**
         * 交易状态；必填；SUCCESS--支付成功 REFUND--转入退款 NOTPAY--未支付 CLOSED--已关闭 REVOKED--已撤销(刷卡支付) USERPAYING--用户支付中 PAYERROR--支付失败(其他原因，如银行返回失败) ACCEPT--已接收，等待扣款；示例：SUCCESS
         */
        @field:JsonProperty("trade_state")
        var tradeState: String? = null,
        /**
         * 交易状态描述；必填；对当前查询订单状态的描述和下一步操作的指引；示例：支付失败，请重新下单支付
         */
        @field:JsonProperty("trade_state_desc")
        var tradeStateDesc: String? = null,
) : PayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode && "SUCCESS" == tradeState
    }
}
