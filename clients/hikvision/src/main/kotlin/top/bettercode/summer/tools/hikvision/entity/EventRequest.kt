package top.bettercode.summer.tools.hikvision.entity

import com.fasterxml.jackson.annotation.JsonFormat
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.ISO8601
import java.time.ZonedDateTime

/**
 * 门禁点事件请求参数
 */
class EventRequest {
    /**
     * 当前页码（pageNo>0） 必填
     */
    var pageNo: Int? = null

    /**
     * 每页展示数目（0<pageSize></pageSize><=1000） 必填
     */
    var pageSize: Int? = null

    /**
     * 门禁点唯一标识数组，最大支持10个门禁点，查询门禁点列表v2接口获取返回报文中的doorIndexCode字段
     */
    var doorIndexCodes: Array<String>? = null

    /**
     * 门禁点名称，支持模糊查询，从查询门禁点列表v2接口获取返回报文中的name字段
     */
    var doorName: String? = null

    /**
     * 读卡器唯一标识数组，最大支持50个读卡器，查询门禁读卡器列表接口获取返回报文中的indexCode字段
     */
    var readerDevIndexCodes: Array<String>? = null

    /**
     * 开始时间（事件开始时间，采用ISO8601时间格式，与endTime配对使用，不能单独使用，时间范围最大不能超过3个月，与receiveStartTime和receiveEndTime两者二选一为必填），参考附录ISO8601时间格式说明
     */
    @JsonFormat(pattern = ISO8601)
    var startTime: ZonedDateTime? = null

    /**
     * 结束时间（事件结束时间，采用ISO8601时间格式，最大长度32个字符，与startTime配对使用，不能单独使用，时间范围最大不能超过3个月），参考附录ISO8601时间格式说明
     */
    @JsonFormat(pattern = ISO8601)
    var endTime: ZonedDateTime? = null

    /**
     * 入库开始时间，采用ISO8601时间格式，与receiveEndTime配对使用，不能单独使用，时间范围最大不能超过3个月，与startTime和endTime两者二选一为必填，参考附录ISO8601时间格式说明
     */
    @JsonFormat(pattern = ISO8601)
    var receiveStartTime: ZonedDateTime? = null

    /**
     * 入库结束时间，采用ISO8601时间格式，最大长度32个字符，与receiveStartTime配对使用，不能单独使用，时间范围最大不能超过3个月，参考附录ISO8601时间格式说明
     */
    @JsonFormat(pattern = ISO8601)
    var receiveEndTime: ZonedDateTime? = null

    /**
     * 门禁点所在区域集合，查询区域列表v2接口获取返回参数indexCode，最大支持500个区域
     */
    var doorRegionIndexCodes: Array<String>? = null

    /**
     * 事件类型，参考附录D2.1 门禁事件
     */
    var eventTypes: Array<Int>? = null

    /**
     * 人员数组（最大支持100个人员）
     */
    var personIds: Array<String>? = null

    /**
     * 人员姓名(支持中英文字符，不能包含 ’ / \ : * ? " < >
     */
    var personName: String? = null

    /**
     * 排序字段（支持personName、doorName、eventTime填写排序的字段名称）
     */
    var sort: String? = null

    /**
     * 升/降序（指定排序字段是使用升序（asc）还是降序（desc）
     */
    var order: String? = null
}
