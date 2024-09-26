package top.bettercode.summer.tools.hikvision.entity

import com.fasterxml.jackson.annotation.JsonFormat
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.ISO8601
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.ISO8601_SSS
import java.time.OffsetDateTime

/**
 * 门禁点事件返回参数
 */
class EventData {
    /**
     * 事件ID，唯一标识这个事件
     */
    var eventId: String? = null

    /**
     * 事件名称
     */
    var eventName: String? = null

    /**
     * 事件产生时间，参考附录ISO8601时间格式说明
     */
    @JsonFormat(pattern = ISO8601, timezone = "+08:00")
    var eventTime: OffsetDateTime? = null

    /**
     * 卡号
     */
    var cardNo: String? = null

    /**
     * 人员唯一编码
     */
    var personId: String? = null

    /**
     * 人员名称
     */
    var personName: String? = null

    /**
     * 人员所属组织编码
     */
    var orgIndexCode: String? = null

    /**
     * 人员所属组织名称
     */
    var orgName: String? = null

    /**
     * 门禁点编码
     */
    var doorIndexCode: String? = null

    /**
     * 门禁点名称
     */
    var doorName: String? = null

    /**
     * 门禁点所在区域编码
     */
    var doorRegionIndexCode: String? = null

    /**
     * 抓拍图片地址，通过接口获取门禁事件的图片接口获取门禁事件的图片数据
     */
    var picUri: String? = null

    /**
     * 图片存储服务的唯一标识
     */
    var svrIndexCode: String? = null

    /**
     * 事件类型，参考附录D2.1门禁事件
     */
    var eventType: Int? = null

    /**
     * 进出类型(1：进0：出-1:未知要求：进门读卡器拨码设置为1，出门读卡器拨码设置为2)
     */
    var inAndOutType: Int? = null

    /**
     * 读卡器IndexCode
     */
    var readerDevIndexCode: String? = null

    /**
     * 读卡器名称
     */
    var readerDevName: String? = null

    /**
     * 控制器设备IndexCode
     */
    var devIndexCode: String? = null

    /**
     * 控制器名称
     */
    var devName: String? = null

    /**
     * 身份证图片uri，它是一个相对地址，可以通过获取门禁事件的图片接口，获取到图片的数据
     */
    var identityCardUri: String? = null

    /**
     * 事件入库时间，参考附录ISO8601时间格式说明
     */
    @JsonFormat(pattern = ISO8601_SSS, timezone = "+08:00")
    var receiveTime: OffsetDateTime? = null

    /**
     * 工号
     */
    var jobNo: String? = null

    /**
     * 学号
     */
    var studentId: String? = null

    /**
     * 证件号码
     */
    var certNo: String? = null
}
