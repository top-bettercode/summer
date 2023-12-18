package top.bettercode.summer.tools.sap.connection.pojo

import top.bettercode.summer.tools.lang.util.RandomUtil.uuid
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.sap.annotation.SapField
import java.text.SimpleDateFormat
import java.util.*

/**
 * sapHead
 */
class SapHead {
    /**
     * 接口传输号 20
     */
    @SapField("IFNO")
    var ifno = uuid()

    /**
     * 系统Id
     */
    @SapField("SYSID")
    var sysid: String? = null

    /**
     * 接口编号
     */
    @SapField("IFID")
    var ifid: String? = null

    /**
     * 场景编号
     */
    @SapField("SCENEID")
    var sceneid: String? = null

    /**
     * 用户名
     */
    @SapField("SUSER")
    var suser: String? = null

    /**
     * 字符字段，8 个字符长度
     */
    @SapField("SDATE")
    var sdate: String? = dateFormat.format(Date())

    /**
     * 长度为6的字符字段
     */
    @SapField("STIME")
    var stime: String? = timeFormate.format(Date())

    /**
     * 长度为 40 的字符型字段
     */
    @SapField("SKDATA")
    var skdata: String? = null

    /**
     * 单一字符标识
     */
    @SapField("OPERATION")
    var operation: String? = null

    override fun toString(): String {
        return json(this)
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyyMMdd")
        private val timeFormate = SimpleDateFormat("HHmmss")
    }
}
