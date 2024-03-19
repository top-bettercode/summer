package top.bettercode.summer.tools.qvod

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.qvod")
open class QvodProperties {
    /**
     * 点播帐号APP ID
     */
    var appId: Long? = null

    /**
     * 防盗链 Key
     */
    var securityChainKey: String = ""

    /**
     * 视频访问有效时长，单位秒,默认一天有效时间
     */
    var accessValidSeconds = 60 * 60 * 24L

    /**
     * 最多允许多少个不同 IP 的终端播放，以十进制表示，最大值为9，不填表示不做限制 当限制 URL 只能被1个人播放时，建议 rlimit
     * 不要严格限制成1（例如可设置为3），因为移动端断网后重连 IP 可能改变
     */
    var rlimit = 9
    var secretId: String = ""
    var secretKey: String = ""

    /**
     * HTTP 请求头：X-TC-Region。地域参数，用来标识希望操作哪个地域的数据。接口接受的地域取值参考接口文档中输入参数公共参数 Region
     * 的说明。注意：某些接口不需要传递该参数，接口文档中会对此特别说明，此时即使传递该参数也不会生效。
     */
    var region: String = ""

    /**
     * 视频后续任务处理操作任务流模板
     */
    var procedure: String = ""

    /**
     * 转码模板
     */
    var templateIds: Array<Long> = arrayOf()

    /**
     * 图片即时处理模板ID
     */
    var picTemplateId: String = ""

    /**
     * 上传签名有效时长，单位：秒 有效时长最大取值为7776000，即90天。默认2小时.
     */
    var uploadValidSeconds = 2 * 60 * 60L

    /**
     * 请求超时超过多少秒报警，-1表示不报警，默认-1.
     */
    var timeoutAlarmSeconds = -1

    /**
     * 请求连接超时时间秒数
     */
    var connectTimeout = 10

    /**
     * 请求读取超时时间秒数
     */
    var readTimeout = 10

    /**
     * 文件分类，默认为0
     */
    var classId = 0

}
