package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/uniform-message/sendUniformMessage.html
 *
 * 统一服务消息
 */
data class UniformMsgRequest @JvmOverloads constructor(

        /**
         * 用户openid，可以是小程序的openid，也可以是mp_template_msg.appid对应的公众号的openid
         */
        @field:JsonProperty("touser")
        var touser: String,
        /**
         * 公众号模板消息相关的信息，可以参考公众号模板消息接口；有此节点并且没有weapp_template_msg节点时，发送公众号模板消息
         */
        @field:JsonProperty("mp_template_msg")
        var mpTemplateMsg: MpTemplateMsg? = null,

        /**
         * 小程序模板消息相关的信息，可以参考小程序模板消息接口; 有此节点则优先发送小程序模板消息；（小程序模板消息已下线，不用传此节点）
         */
        @field:JsonProperty("weapp_template_msg")
        var weappTemplateMsg: WeappTemplateMsg? = null

)