package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html
 */
data class SubscribeMsgRequest<T> @JvmOverloads constructor(

        @field:JsonProperty("touser")
        val touser: String,

        @field:JsonProperty("template_id")
        val templateId: String,

        @field:JsonProperty("data")
        val data: T,

        @field:JsonProperty("page")
        val page: String? = null,

        /**
         * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
         */
        @field:JsonProperty("miniprogram_state")
        var miniprogramState: String? = "formal",

        /**
         * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
         */
        @field:JsonProperty("lang")
        val lang: String? = "zh_CN"
)