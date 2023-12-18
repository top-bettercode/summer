package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField

open class EsMessage {
    /**
     * 消息类型 S:成功；E:错误；W:警告
     */
    @SapField("MTYPE")
    var type: String? = null


    /**
     * 消息描述 字符100
     */
    @SapField("MSGTXT")
    var message: String? = null

    override fun toString(): String {
        return json(this)
    }

    val isOk: Boolean
        //--------------------------------------------
        get() = !type.isNullOrBlank() && "E" != type
    val isSuccess: Boolean
        get() = "S" == type
}
