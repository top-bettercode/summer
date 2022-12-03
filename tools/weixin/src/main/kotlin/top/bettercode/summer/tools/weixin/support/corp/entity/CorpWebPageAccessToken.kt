package top.bettercode.summer.tools.weixin.support.corp.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

data class CorpWebPageAccessToken(
    /**
     * 成员UserID。若需要获得用户详情信息，可调用通讯录接口：读取成员。如果是互联企业/企业互联/上下游，则返回的UserId格式如：CorpId/userid
     */
    @field:JsonProperty("UserId")
    val UserId: String? = null,

    /**
     * 非企业成员的标识，对当前企业唯一。不超过64字节
     */
    @field:JsonProperty("OpenId")
    val openid: String? = null,

    /**
     * 手机设备号(由企业微信在安装时随机生成，删除重装会改变，升级不受影响)
     */
    @field:JsonProperty("DeviceId")
    val deviceId: String? = null,

    /**
     * 外部联系人id，当且仅当用户是企业的客户，且跟进人在应用的可见范围内时返回。如果是第三方应用调用，针对同一个客户，同一个服务商不同应用获取到的id相同
     */
    @field:JsonProperty("external_userid")
    val externalUserid: String? = null,
) : WeixinResponse()