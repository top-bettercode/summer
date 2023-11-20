package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
 */
data class SubscribeMsgRequest @JvmOverloads constructor(

        /**
         * 接收者（用户）的 openid
         */
        @field:JsonProperty("touser")
        var touser: String,
        /**
         * 所需下发的订阅模板id
         */
        @field:JsonProperty("template_id")
        var templateId: String,

        /**
         * 模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }的object
         */
        @field:JsonProperty("data")
        var data: Map<String, Data>,

        /**
         * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
         */
        @field:JsonProperty("page")
        var page: String? = null,

        /**
         * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
         */
        @field:JsonProperty("miniprogram_state")
        var miniprogramState: String? = null,

        /**
         * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
         */
        @field:JsonProperty("lang")
        var lang: String? = "zh_CN"
)