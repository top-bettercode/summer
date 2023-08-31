package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
data class TransfersRequest(

        /**
         * 商户账号appid；必填；申请商户号的appid或商户号绑定的appid；示例：wx8888888888888888
         */
        @field:JsonProperty("mch_appid")
        var mchAppid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mchid")
        var mchid: String? = null,
        /**
         * 设备号；非必填；微信支付分配的终端设备号；示例：013467007045764
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = RandomUtil.nextString2(32),
        /**
         * 签名；必填；签名，详见签名算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 商户订单号；必填；商户订单号，需保持唯一性 (只能是字母或者数字，不能包含有其它字符)；示例：10000098201411111234567890
         */
        @field:JsonProperty("partner_trade_no")
        var partnerTradeNo: String? = null,
        /**
         * 用户openid；必填；openid是微信用户在公众账号下的唯一用户标识（appid不同，则获取到的openid就不同），可用于永久标记一个用户。 获取openid的链接；示例：oxTWIuGaIt6gTKsQRLau2M0yL16E
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 校验用户姓名选项；必填；NO_CHECK：不校验真实姓名 FORCE_CHECK：强校验真实姓名；示例：FORCE_CHECK
         */
        @field:JsonProperty("check_name")
        var checkName: String? = null,
        /**
         * 收款用户姓名；非必填；收款用户真实姓名。 如果check_name设置为FORCE_CHECK，则必填用户真实姓名； 如需电子回单，需要传入收款用户姓名。 商户需确保向微信支付传输用户身份信息和账号标识信息做一致性校验已合法征得用户授权；示例：王小王
         */
        @field:JsonProperty("re_user_name")
        var reUserName: String? = null,
        /**
         * 金额；必填；付款金额，单位为分；示例：10099
         */
        @field:JsonProperty("amount")
        var amount: Int? = null,
        /**
         * 付款备注；必填；付款备注，必填；示例：理赔
         */
        @field:JsonProperty("desc")
        var desc: String? = null,
        /**
         * Ip地址；非必填；该IP同在商户平台设置的IP白名单中的IP没有关联，该IP可传用户端或者服务端的IP；示例：192.168.0.1
         */
        @field:JsonProperty("spbill_create_ip")
        var spbillCreateIp: String? = null,
        /**
         * 付款场景；非必填；BRAND_REDPACKET：品牌红包， 其他值或不传则默认为普通付款到零钱 （品牌红包能力暂未全量开放，若有意愿参与内测请填写问卷https://wj.qq.com/s2/9229085/29f4/）；示例：BRAND_REDPACKET
         */
        @field:JsonProperty("scene")
        var scene: String? = null,
        /**
         * 品牌ID；非必填；品牌在微信支付的唯一标识。仅在付款场景为品牌红包时必填；示例：1234
         */
        @field:JsonProperty("brand_id")
        var brandId: Int? = null,
        /**
         * 消息模板ID；非必填；品牌所配置的消息模板的唯一标识。仅在付款场景为品牌红包时必填；示例：1243100000000000
         */
        @field:JsonProperty("finder_template_id")
        var finderTemplateId: String? = null,
)
