package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.Data
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.Miniprogram

/**
 * 公众号模板消息相关的信息，可以参考公众号模板消息接口；有此节点并且没有weapp_template_msg节点时，发送公众号模板消息
 */
data class MpTemplateMsg(
        /**
         * 公众号appid，要求与小程序有绑定且同主体
         */
        @field:JsonProperty("appid")
        var appid: String,
        /**
         * 公众号模板id
         */
        @field:JsonProperty("template_id")
        var templateId: String,
        /**
         * 公众号模板消息的数据
         */
        @field:JsonProperty("data")
        var data: Map<String, Data>,
        /**
         * 公众号模板消息所要跳转的小程序，小程序的必须与公众号具有绑定关系
         */
        @field:JsonProperty("miniprogram")
        var miniprogram: Miniprogram,
        /**
         * 公众号模板消息所要跳转的url
         */
        @field:JsonProperty("url")
        var url: String? = null
)