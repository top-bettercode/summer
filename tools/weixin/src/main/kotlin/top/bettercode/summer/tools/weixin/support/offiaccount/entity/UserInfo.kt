package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfo @JvmOverloads constructor(
        /**
         * 用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
         */
        @field:JsonProperty("subscribe")
        var subscribe: Int? = null,

        /**
         * 用户的标识，对当前公众号唯一
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 用户的语言，简体中文为zh_CN
         */
        @field:JsonProperty("language")
        var language: String? = null,
        /**
         * 用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
         */
        @field:JsonProperty("subscribe_time")
        var subscribeTime: Int? = null,
        /**
         * 只有在用户将公众号绑定到微信开放平台账号后，才会出现该字段。
         */
        @field:JsonProperty("unionid")
        var unionid: String? = null,
        /**
         * 公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注
         */
        @field:JsonProperty("remark")
        var remark: String? = null,

        /**
         * 用户所在的分组ID（兼容旧的用户分组接口）
         */
        @field:JsonProperty("groupid")
        var groupid: Int? = null,

        /**
         * 用户被打上的标签ID列表
         */
        @field:JsonProperty("tagid_list")
        var tagidList: List<Int?>? = null,

        /**
         * 返回用户关注的渠道来源，ADD_SCENE_SEARCH 公众号搜索，ADD_SCENE_ACCOUNT_MIGRATION 公众号迁移，ADD_SCENE_PROFILE_CARD 名片分享，ADD_SCENE_QR_CODE 扫描二维码，ADD_SCENE_PROFILE_LINK 图文页内名称点击，ADD_SCENE_PROFILE_ITEM 图文页右上角菜单，ADD_SCENE_PAID 支付后关注，ADD_SCENE_WECHAT_ADVERTISEMENT 微信广告，ADD_SCENE_REPRINT 他人转载 ,ADD_SCENE_LIVESTREAM 视频号直播，ADD_SCENE_CHANNELS 视频号 , ADD_SCENE_OTHERS 其他
         */
        @field:JsonProperty("subscribe_scene")
        var subscribeScene: String? = null,

        /**
         * 二维码扫码场景（开发者自定义）
         */
        @field:JsonProperty("qr_scene")
        var qrScene: Int? = null,

        /**
         * 二维码扫码场景描述（开发者自定义）
         */
        @field:JsonProperty("qr_scene_str")
        var qrSceneStr: String? = null
) : WeixinResponse()