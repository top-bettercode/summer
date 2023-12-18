package top.bettercode.summer.tools.sap.connection.pojo

import org.springframework.util.CollectionUtils
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapTable

open class EtReturns : ISapReturn {
    /**
     * 返回消息
     */
    @SapTable("ET_RETURN")
    var etReturn: List<EtReturn>? = null


    override fun toString(): String {
        return json(this)
    }

    override val isSuccess: Boolean
        //--------------------------------------------
        get() = if (CollectionUtils.isEmpty(etReturn)) {
            false
        } else {
            "S" == etReturn!![0].type
        }
    override val message: String?
        get() = if (CollectionUtils.isEmpty(etReturn)) {
            null
        } else {
            etReturn!![0].type
        }
}