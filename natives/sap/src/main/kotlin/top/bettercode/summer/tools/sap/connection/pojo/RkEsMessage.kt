package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure

@SapStructure("ES_MESSAGE")
class RkEsMessage : EsMessage() {
    /**
     * 接收数据键值 长度为 40 的字符型字段
     */
    @SapField("RKDATA")
    var rkdata: String? = null

    //--------------------------------------------

    override fun toString(): String {
        return json(this)
    }
}
