package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html
 */
data class TemplateMsgRequest @JvmOverloads constructor(
        /**
         * 接收者openid
         */
        @field:JsonProperty("touser")
        val touser: String,
        /**
         * 模板ID
         */
        @field:JsonProperty("template_id")
        val templateId: String,

        /**
         * 模板数据
         */
        @field:JsonProperty("data")
        val data: Map<String, Data>,

        /**
         * 模板跳转链接（海外账号没有跳转能力）
         */
        @field:JsonProperty("url")
        val url: String? = null,

        /**
         * 跳小程序所需数据，不需跳小程序可不用传该数据
         */
        @field:JsonProperty("miniprogram")
        val miniprogram: Miniprogram? = null,

        /**
         * 防重入id。对于同一个openid + client_msg_id, 只发送一条消息,10分钟有效,超过10分钟不保证效果。若无防重入需求，可不填
         */
        @field:JsonProperty("client_msg_id")
        val clientMsgId: String? = null
)