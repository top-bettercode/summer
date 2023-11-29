package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiQuota @JvmOverloads constructor(
        /**
         * quota详情
         */
        @field:JsonProperty("quota")
        var quota: Quota? = null
) : WeixinResponse()

data class Quota @JvmOverloads constructor(
        /**
         * 当天该账号可调用该接口的次数
         */
        @field:JsonProperty("daily_limit")
        var dailyLimit: Int? = null,

        /**
         * 当天已经调用的次数
         */
        @field:JsonProperty("used")
        var used: Int? = null,
        /**
         * 当天剩余调用次数
         */
        @field:JsonProperty("remain")
        var remain: Int? = null,
)
