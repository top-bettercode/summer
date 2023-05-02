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
     * @return 接口传输号
     */
    /**
     * 接口传输号 20
     */
    @SapField("IFNO")
    var ifno = uuid()
        private set
    /**
     * @return 系统Id
     */
    /**
     * 系统Id
     */
    @SapField("SYSID")
    var sysid: String? = null
        private set
    /**
     * @return 接口编号
     */
    /**
     * 接口编号
     */
    @SapField("IFID")
    var ifid: String? = null
        private set
    /**
     * @return 场景编号
     */
    /**
     * 场景编号
     */
    @SapField("SCENEID")
    var sceneid: String? = null
        private set
    /**
     * @return 用户名
     */
    /**
     * 用户名
     */
    @SapField("SUSER")
    var suser: String? = null
        private set
    /**
     * @return 字符字段，8 个字符长度
     */
    /**
     * 字符字段，8 个字符长度
     */
    @SapField("SDATE")
    var sdate = dateFormat.format(Date())
        private set
    /**
     * @return 长度为6的字符字段
     */
    /**
     * 长度为6的字符字段
     */
    @SapField("STIME")
    var stime = timeFormate.format(Date())
        private set
    /**
     * @return 长度为 40 的字符型字段
     */
    /**
     * 长度为 40 的字符型字段
     */
    @SapField("SKDATA")
    var skdata: String? = null
        private set
    /**
     * @return 单一字符标识
     */
    /**
     * 单一字符标识
     */
    @SapField("OPERATION")
    var operation: String? = null
        private set

    /**
     * 设置接口传输号
     *
     * @param ifno 接口传输号
     * @return 接口控制数据
     */
    fun setIfno(ifno: String): SapHead {
        this.ifno = ifno
        return this
    }

    /**
     * 设置系统Id
     *
     * @param sysid 系统Id
     * @return 接口控制数据
     */
    fun setSysid(sysid: String?): SapHead {
        this.sysid = sysid
        return this
    }

    /**
     * 设置接口编号
     *
     * @param ifid 接口编号
     * @return 接口控制数据
     */
    fun setIfid(ifid: String?): SapHead {
        this.ifid = ifid
        return this
    }

    /**
     * 设置场景编号
     *
     * @param sceneid 场景编号
     * @return 接口控制数据
     */
    fun setSceneid(sceneid: String?): SapHead {
        this.sceneid = sceneid
        return this
    }

    /**
     * 设置用户名
     *
     * @param suser 用户名
     * @return 接口控制数据
     */
    fun setSuser(suser: String?): SapHead {
        this.suser = suser
        return this
    }

    /**
     * 设置字符字段，8 个字符长度
     *
     * @param sdate 字符字段，8 个字符长度
     * @return 接口控制数据
     */
    fun setSdate(sdate: String): SapHead {
        this.sdate = sdate
        return this
    }

    /**
     * 设置长度为6的字符字段
     *
     * @param stime 长度为6的字符字段
     * @return 接口控制数据
     */
    fun setStime(stime: String): SapHead {
        this.stime = stime
        return this
    }

    /**
     * 设置长度为 40 的字符型字段
     *
     * @param skdata 长度为 40 的字符型字段
     * @return 接口控制数据
     */
    fun setSkdata(skdata: String?): SapHead {
        this.skdata = skdata
        return this
    }

    /**
     * 设置单一字符标识
     *
     * @param operation 单一字符标识
     * @return 接口控制数据
     */
    fun setOperation(operation: String?): SapHead {
        this.operation = operation
        return this
    }

    override fun toString(): String {
        return json(this)
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyyMMdd")
        private val timeFormate = SimpleDateFormat("HHmmss")
    }
}
