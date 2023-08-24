package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 小程序模板消息相关的信息，可以参考小程序模板消息接口; 有此节点则优先发送小程序模板消息；（小程序模板消息已下线，不用传此节点）
 */
data class WeappTemplateMsg(

        /**
         * 小程序模板ID
         */
        @field:JsonProperty("template_id")
        val templateId: String,

        /**
         * 小程序模板数据
         */
        @field:JsonProperty("data")
        val data: Map<String, Data>? = null,

        /**
         * 小程序页面路径
         */
        @field:JsonProperty("page")
        val page: String? = null,

        /**
         * 小程序模板消息formid
         */
        @field:JsonProperty("form_id")
        val formId: String? = null,

        /**
         * 小程序模板放大关键词
         */
        @field:JsonProperty("emphasis_keyword")
        val emphasisKeyword: String? = null
)