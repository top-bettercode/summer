package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure

@SapStructure("ES_MESSAGE")
class RkEsMessage : EsMessage() {
    /**
     * @return 长度为 40 的字符型字段
     */
    /**
     * 接收数据键值 长度为 40 的字符型字段
     */
    @SapField("RKDATA")
    var rkdata: String? = null
        private set
    //--------------------------------------------
    /**
     * 设置长度为 40 的字符型字段
     *
     * @param rkdata 长度为 40 的字符型字段
     * @return 返回消息结构
     */
    fun setRkdata(rkdata: String?): RkEsMessage {
        this.rkdata = rkdata
        return this
    }

    override fun toString(): String {
        return json(this)
    }
}
