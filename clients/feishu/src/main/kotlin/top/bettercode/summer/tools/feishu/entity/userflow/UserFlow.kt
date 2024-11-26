package top.bettercode.summer.tools.feishu.entity.userflow

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 打卡流水
 */
data class UserFlow(
    /**
     * 用户 ID。与employee_type对应 。示例值："abd754f7" 必填
     */
    @JsonProperty("user_id")
    var userId: String? = null,

    /**
     * 记录创建者 ID。与employee_type对应 。示例值："abd754f7" 必填
     */
    @JsonProperty("creator_id")
    var creatorId: String? = null,

    /**
     * 打卡位置名称信息 。示例值："西溪八方城" 必填
     */
    @JsonProperty("location_name")
    var locationName: String? = null,

    /**
     * 打卡时间，精确到秒的时间戳（只支持导入打卡时间在2022年1月1日之后的数据） 。示例值："1611476284" 必填
     */
    @JsonProperty("check_time")
    var checkTime: String? = null,

    /**
     * 打卡备注 。示例值："上班打卡" 必填
     */
    @JsonProperty("comment")
    var comment: String? = null,

    /**
     * 打卡记录 ID，导入时此参数无效 。示例值："6709359313699356941"
     */
    @JsonProperty("record_id")
    var recordId: String? = null,

    /**
     * 打卡 Wi-Fi 的 SSID 。示例值："b0:b8:67:5c:1d:72"
     */
    @JsonProperty("ssid")
    var ssid: String? = null,

    /**
     * 打卡 Wi-Fi 的 MAC 地址 。示例值："b0:b8:67:5c:1d:72"
     */
    @JsonProperty("bssid")
    var bssid: String? = null,

    /**
     * 是否为外勤打卡。默认为false，非外勤打卡 。示例值：true
     */
    @JsonProperty("is_field")
    var isField: Boolean? = null,

    /**
     * 是否为 Wi-Fi 打卡。默认为false，非Wi-Fi打卡 。示例值：true
     */
    @JsonProperty("is_wifi")
    var isWifi: Boolean? = null,

    /**
     * 记录生成方式。在开放平台调用时，此参数无效，内部值始终是7 。示例值：7 可选值有： 0：用户打卡 1：管理员修改 2：用户补卡 3：系统自动生成 4：下班免打卡 5：考勤机 6：极速打卡 7：考勤开放平台导入
     */
    @JsonProperty("type")
    var type: Int? = null,

    /**
     * 打卡照片列表（该字段目前不支持） 。示例值：["https://time.clockin.biz/manage/download/6840389754748502021"]
     */
    @JsonProperty("photo_urls")
    var photoUrls: Array<String>? = null,

    /**
     * 打卡设备ID，（只支持小程序打卡，导入时无效） 。示例值："99e0609ee053448596502691a81428654d7ded64c7bd85acd982d26b3636c37d"
     */
    @JsonProperty("device_id")
    var deviceId: String? = null,

    /**
     * 打卡结果，作为入参时无效 。示例值："Invalid" 可选值有： NoNeedCheck：无需打卡 SystemCheck：系统打卡 Normal：正常 Early：早退 Late：迟到 SeriousLate：严重迟到 Lack：缺卡 Invalid：无效 None：无状态 Todo：尚未打卡
     */
    @JsonProperty("check_result")
    var checkResult: String? = null,

    /**
     * 用户导入的外部打卡记录ID，用于和外部数据对比，如果不传，在查询的时候不方便区分 。示例值："record_123"
     */
    @JsonProperty("external_id")
    var externalId: String? = null,

    /**
     * 唯一幂等键，不传的话无法实现幂等处理 。示例值："*_"
     */
    @JsonProperty("idempotent_id")
    var idempotentId: String? = externalId
)
