package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * <p>
 *     {
 *   "touser": "OPENID",
 *   "msgtype": "msgmenu",
 *   "msgmenu": {
 *     "head_content": "您对本次服务是否满意呢? ",
 *     "list": [
 *       {
 *         "id": "101",
 *         "content": "满意"
 *       },
 *       {
 *         "id": "102",
 *         "content": "不满意"
 *       }
 *     ],
 *     "tail_content": "欢迎再次光临"
 *   }
 * }
 *
 * </p>
 */
data class MenuCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        @field:JsonProperty("msgmenu")
        val msgmenu: Msgmenu,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "msgmenu",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice)

data class Msgmenu(

        @field:JsonProperty("head_content")
        val headContent: String,

        @field:JsonProperty("tail_content")
        val tailContent: String,

        @field:JsonProperty("list")
        val list: List<ListItem>
)

data class ListItem(

        @field:JsonProperty("id")
        val id: String,

        @field:JsonProperty("content")
        val content: String
)
