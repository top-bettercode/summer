package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapStructure

/**
 * 返回sap实体
 */
open class SapReturn<T : EsMessage?> : ISapReturn {

    @SapStructure("ES_MESSAGE")
    var esMessage: T? = null

    override fun toString(): String {
        return json(this)
    }

    override val isOk: Boolean
        //--------------------------------------------
        get() = esMessage?.isOk ?: false
    override val isSuccess: Boolean
        get() = esMessage?.isSuccess ?: false
    override val message: String?
        get() = esMessage?.message
}
