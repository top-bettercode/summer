package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Options @JvmOverloads constructor(

        @field:JsonProperty("time_to_live")
        val timeToLive: Long? = null,

        @field:JsonProperty("apns_production")
        val apnsProduction: Boolean? = null
)