package top.bettercode.summer.tools.weixin.support.aes

import com.fasterxml.jackson.annotation.JsonProperty

data class DecryptMsg(

        /**
         * 开发者微信号
         */
        @field:JsonProperty("ToUserName")
        val toUserName: String? = null,

        /**
         * 发送方账号（一个OpenID）
         */
        @field:JsonProperty("FromUserName")
        val fromUserName: String? = null,

        /**
         * 消息创建时间,秒
         */
        @field:JsonProperty("CreateTime")
        val createTime: Long? = null,

        /**
         * 消息类型，event
         */
        @field:JsonProperty("MsgType")
        val msgType: String? = null,

        /**
         * 事件类型,subscribe(订阅)、unsubscribe(取消订阅)
         *
         * subscribe:用户未关注时，进行关注后的事件推送,
         * SCAN:用户已关注时的事件推送
         * LOCATION:上报地理位置事件
         */
        @field:JsonProperty("Event")
        val event: String? = null,

        /**
         * 事件KEY值，qrscene_为前缀，后面为二维码的参数值
         */
        @field:JsonProperty("EventKey")
        val eventKey: String? = null,

        /**
         * 二维码的ticket，可用来换取二维码图片
         */
        @field:JsonProperty("Ticket")
        val ticket: String? = null,

        /**
         * 地理位置纬度
         */
        @field:JsonProperty("Latitude")
        val latitude: String? = null,

        /**
         * 地理位置经度
         */
        @field:JsonProperty("Longitude")
        val longitude: String? = null,

        /**
         * 地理位置精度
         */
        @field:JsonProperty("Precision")
        val precision: String? = null
)