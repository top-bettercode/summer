package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure

@SapStructure("ET_RETURN")
class EtReturn {
    /**
     * 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
     */
    @SapField("TYPE")
    var type: String? = null

    /**
     * 消息文本
     */
    @SapField("MESSAGE")
    var message: String? = null

    override fun toString(): String {
        return json(this)
    }
}
