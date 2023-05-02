package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure

@SapStructure("ET_RETURN")
class EtReturn {
    /**
     * @return 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
     */
    /**
     * 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
     */
    @SapField("TYPE")
    var type: String? = null
        private set
    /**
     * @return 消息文本
     */
    /**
     * 消息文本
     */
    @SapField("MESSAGE")
    var message: String? = null
        private set

    /**
     * 设置消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
     *
     * @param type 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
     * @return 返回消息
     */
    fun setType(type: String?): EtReturn {
        this.type = type
        return this
    }

    /**
     * 设置消息文本
     *
     * @param message 消息文本
     * @return 返回消息
     */
    fun setMessage(message: String?): EtReturn {
        this.message = message
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}
