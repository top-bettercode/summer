package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import top.bettercode.summer.tools.lang.util.IPAddressUtil
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
@JacksonXmlRootElement(localName = "xml")
data class UnifiedOrderRequest @JvmOverloads constructor(

        /**
         * 交易类型；必填；支付类型；示例：APP
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 商户订单号；必填；商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号；示例：20150806125346
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 总金额；必填；订单总金额，单位为分，详见支付金额；示例：888
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 商品描述；必填；商品描述交易字段格式根据不同的应用场景按照以下格式： APP——需传入应用市场上的APP名字-实际商品名称，天天爱消除-游戏充值；示例：腾讯充值中心-QQ会员充值
         */
        @field:JsonProperty("body")
        var body: String? = null,
        /**
         * 用户标识；非必填；trade_type=JSAPI时（即JSAPI支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识。openid如何获取，可参考【获取openid】。企业号请使用【企业号OAuth2.0接口】获取企业号内成员userid，再调用【企业号userid转openid接口】进行转换；示例：oUpF8uMuAJO_M2pxb1Q9zNjWeS6o
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 通知地址；必填；接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。公网域名必须为https，如果是走专线接入，使用专线NAT IP或者私有回调域名可使用http；示例：https://www.weixin.qq.com/wxpay/pay.php
         */
        @field:JsonProperty("notify_url")
        var notifyUrl: String? = null,
        /**
         * 交易起始时间；非必填；订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则；示例：20091225091010
         */
        @field:JsonProperty("time_start")
        var timeStart: String? = null,
        /**
         * 交易结束时间；非必填；订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010；示例：20091227091010
         */
        @field:JsonProperty("time_expire")
        var timeExpire: String? = null,
        /**
         * 终端IP；必填；支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP；示例：123.12.12.123
         */
        @field:JsonProperty("spbill_create_ip")
        var spbillCreateIp: String? = IPAddressUtil.inet4Address,
        /**
         * 应用ID；必填；微信开放平台审核通过的应用APPID（请登录open.weixin.qq.com查看，注意与公众号的APPID不同）；示例：wxd678efh567hg6787
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1230000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 设备号；非必填；终端设备号(门店号或收银设备ID)，默认请传"WEB"；示例：013467007045764
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = RandomUtil.nextString2(32),
        /**
         * 签名；必填；签名，详见签名生成算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 签名类型；非必填；签名类型，目前支持HMAC-SHA256和MD5，默认为MD5；示例：HMAC-SHA256
         */
        @field:JsonProperty("sign_type")
        var signType: String? = null,
        /**
         * 商品详情；非必填；商品详细描述，对于使用单品优惠的商户，该字段必须按照规范上传，详见“单品优惠参数说明”；示例：}]
         */
        @field:JsonProperty("detail")
        var detail: String? = null,
        /**
         * 附加数据；非必填；附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据；示例：深圳分店
         */
        @field:JsonProperty("attach")
        var attach: String? = null,
        /**
         * 货币类型；非必填；符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 订单优惠标记；非必填；订单优惠标记，代金券或立减优惠功能的参数，说明详见代金券或立减优惠；示例：WXG
         */
        @field:JsonProperty("goods_tag")
        var goodsTag: String? = null,
        /**
         * 指定支付方式；非必填；no_credit--指定不能使用信用卡支付；示例：no_credit
         */
        @field:JsonProperty("limit_pay")
        var limitPay: String? = null,
        /**
         * 商品ID；非必填；trade_type=NATIVE时，此参数必传。此参数为二维码中包含的商品ID，商户自行定义；示例：12235413214070356458058
         */
        @field:JsonProperty("product_id")
        var productId: String? = null,
        /**
         * 开发票入口开放标识；非必填；Y，传入Y时，支付成功消息和支付详情页将出现开票入口。需要在微信支付商户平台或微信公众平台开通电子发票功能，传此字段才可生效；示例：Y
         */
        @field:JsonProperty("receipt")
        var receipt: String? = null,
        /**
         * 是否需要分账；非必填；Y-是，需要分账 N-否，不分账 字母要求大写，不传默认不分账；示例：Y
         */
        @field:JsonProperty("profit_sharing")
        var profitSharing: String? = null,
        /**
         * 场景信息；非必填；该字段常用于线下活动时的场景信息上报，支持上报实际门店信息，商户也可以按需求自己上报相关信息。该字段为JSON对象数据，对象格式为{"store_info":{"id": "门店ID","name": "名称","area_code": "编码","address": "地址" }} ，字段详细说明请点击行前的+展开；示例：1
         */
        @field:JsonProperty("scene_info")
        var sceneInfo: String? = null,
)
