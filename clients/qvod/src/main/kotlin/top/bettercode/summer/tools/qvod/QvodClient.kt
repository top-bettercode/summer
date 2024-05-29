package top.bettercode.summer.tools.qvod

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.exception.TencentCloudSDKException
import com.tencentcloudapi.common.http.HttpConnection
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.vod.v20180717.VodClient
import com.tencentcloudapi.vod.v20180717.models.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.util.DigestUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.OkHttpLoggingInterceptor
import top.bettercode.summer.tools.lang.util.RandomUtil
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * 腾讯云接口
 *
 * @author Peter Wu
 */
@LogMarker(QvodClient.MARKER)
open class QvodClient(
    val properties: QvodProperties
) {
    private val log: Logger = LoggerFactory.getLogger(QvodClient::class.java)

    companion object {
        const val MARKER = "qvod"
    }

    val vodClient: VodClient

    init {
        val cred = Credential(properties.secretId, properties.secretKey)
        val httpProfile = HttpProfile()
        httpProfile.endpoint = "vod.tencentcloudapi.com"
        httpProfile.connTimeout = properties.connectTimeout
        httpProfile.readTimeout = properties.readTimeout
        httpProfile.writeTimeout = properties.readTimeout
        val clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        vodClient = VodClient(cred, properties.region, clientProfile)
        val field = VodClient::class.java.superclass.getDeclaredField("httpConnection")
        field.isAccessible = true
        val httpConnection = field.get(vodClient) as HttpConnection
        httpConnection.addInterceptors(/* interceptor = */ OkHttpLoggingInterceptor(
            collectionName = "第三方平台",
            name = "腾讯云",
            logMarker = MARKER,
            logClazz = QvodClient::class.java,
            timeoutAlarmSeconds = properties.timeoutAlarmSeconds
        )
        )
    }

    /**
     * 客户端上传签名
     * https://cloud.tencent.com/document/product/266/9221
     */
    fun signature(): String {
        val currentTimeStamp = System.currentTimeMillis() / 1000
        val original =
            "secretId=${properties.secretId}&currentTimeStamp=$currentTimeStamp&expireTime=${currentTimeStamp + properties.uploadValidSeconds}&random=${
                RandomUtil.nextInt(9)
            }&classId=${properties.classId}&procedure=${properties.procedure}&vodSubAppId=${properties.appId}"
        if (log.isDebugEnabled) {
            log.debug(MarkerFactory.getMarker(MARKER), "original signature:{}", original)
        }
        val mac: Mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(properties.secretKey.toByteArray(), mac.algorithm)
        mac.init(secretKey)
        val signatureTmp: ByteArray = mac.doFinal(original.toByteArray())
        val signature = Base64.getEncoder().encodeToString(
            byteMerger(
                signatureTmp,
                original.toByteArray(charset("utf8"))
            )
        ).replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
        log.info(MarkerFactory.getMarker(MARKER), "signature: $signature")
        return signature
    }


    /**
     * 播放器签名
     *
     * https://cloud.tencent.com/document/product/266/42437
     */
    fun playSignature(
        fileId: String,
        currentTimeStamp: Long = System.currentTimeMillis() / 1000,
        //派发签名到期 Unix 时间戳，不填表示不过期,默认一天有效时间
        expireTimeStamp: Long = currentTimeStamp + properties.accessValidSeconds,
        //播放地址的过期时间戳，以 Unix 时间的十六进制小写形式表示
        //过期后该 URL 将不再有效，返回403响应码。考虑到机器之间可能存在时间差，防盗链 URL 的实际过期时间一般比指定的过期时间长5分钟，即额外给出300秒的容差时间
        //建议过期时间戳不要过短，确保视频有足够时间完整播放
        //默认一天有效时间
        urlTimeExpire: String = java.lang.Long.toHexString(currentTimeStamp + properties.accessValidSeconds)
    ): String {
        val urlAccessInfo = HashMap<String, String>()
        urlAccessInfo["t"] = urlTimeExpire

        val algorithm: Algorithm = Algorithm.HMAC256(properties.securityChainKey)
        return JWT.create().withClaim("appId", properties.appId)
            .withClaim("fileId", fileId)
            .withClaim("currentTimeStamp", currentTimeStamp)
            .withClaim("expireTimeStamp", expireTimeStamp)
            .withClaim("urlAccessInfo", urlAccessInfo)
            .sign(algorithm)
    }

    /**
     * 防盗链 URL 生成
     *
     * https://cloud.tencent.com/document/product/266/14047
     */
    fun antiLeechUrl(
        url: String,
        //播放地址的过期时间戳，以 Unix 时间的十六进制小写形式表示
        //过期后该 URL 将不再有效，返回403响应码。考虑到机器之间可能存在时间差，防盗链 URL 的实际过期时间一般比指定的过期时间长5分钟，即额外给出300秒的容差时间
        //建议过期时间戳不要过短，确保视频有足够时间完整播放
        t: String = java.lang.Long.toHexString(System.currentTimeMillis() / 1000 + properties.accessValidSeconds),
        //最多允许多少个不同 IP 的终端播放，以十进制表示，最大值为9，不填表示不做限制
        //当限制 URL 只能被1个人播放时，建议 rlimit 不要严格限制成1（例如可设置为3），因为移动端断网后重连 IP 可能改变
        rlimit: Int = properties.rlimit
    ): String {
        val trueUrl = url.substringBefore("?")
        val dir = trueUrl.substringAfter("vod2.myqcloud.com").substringBeforeLast("/") + "/"
        val us = RandomUtil.nextString(10)
//        sign = md5(KEY + Dir + t + exper + rlimit + us + uv)
        val sign =
            DigestUtils.md5DigestAsHex("${properties.securityChainKey}${dir}${t}${rlimit}${us}".toByteArray())
        return "$trueUrl?t=$t&rlimit=$rlimit&us=$us&sign=$sign"
    }


    /**
     * https://cloud.tencent.com/document/product/266/31763
     *
     * 获取媒体详细信息
     * @param fileId 媒体文件 ID 列表，N 从 0 开始取值，最大 19。
     * @param filter 指定所有媒体文件需要返回的信息，可同时指定多个信息，N 从 0 开始递增。如果未填写该字段，默认返回所有信息。选项有：
    basicInfo（视频基础信息）。
    metaData（视频元信息）。
    transcodeInfo（视频转码结果信息）。
    animatedGraphicsInfo（视频转动图结果信息）。
    imageSpriteInfo（视频雪碧图信息）。
    snapshotByTimeOffsetInfo（视频指定时间点截图信息）。
    sampleSnapshotInfo（采样截图信息）。
    keyFrameDescInfo（打点信息）。
    adaptiveDynamicStreamingInfo（转自适应码流信息）。
    miniProgramReviewInfo（小程序审核信息）。
     */
    open fun describeMediaInfo(
        fileId: String,
        filter: String
    ): DescribeMediaInfosResponse {
        return describeMediaInfos(arrayOf(fileId), arrayOf(filter))
    }

    /**
     * 获取媒体详细信息
     *
     * https://cloud.tencent.com/document/product/266/31763
     *
     * @param fileIds 媒体文件 ID 列表，N 从 0 开始取值，最大 19。
     * @param filters 指定所有媒体文件需要返回的信息，可同时指定多个信息，N 从 0 开始递增。如果未填写该字段，默认返回所有信息。选项有：
    basicInfo（视频基础信息）。
    metaData（视频元信息）。
    transcodeInfo（视频转码结果信息）。
    animatedGraphicsInfo（视频转动图结果信息）。
    imageSpriteInfo（视频雪碧图信息）。
    snapshotByTimeOffsetInfo（视频指定时间点截图信息）。
    sampleSnapshotInfo（采样截图信息）。
    keyFrameDescInfo（打点信息）。
    adaptiveDynamicStreamingInfo（转自适应码流信息）。
    miniProgramReviewInfo（小程序审核信息）。
     */
    open fun describeMediaInfos(
        fileIds: Array<String>,
        filters: Array<String>
    ): DescribeMediaInfosResponse {
        val req = DescribeMediaInfosRequest()
        req.fileIds = fileIds
        req.filters = filters
        req.subAppId = properties.appId

        return vodClient.DescribeMediaInfos(req)
    }

    /**
     * https://cloud.tencent.com/document/product/266/72480
     *
     * 该接口用于： 1. 查询点播可开通的所有存储园区列表。 2. 查询已经开通的园区列表。 3. 查询默认使用的存储园区。
     */
    open fun storageRegions(): DescribeStorageRegionsResponse {
        val req = DescribeStorageRegionsRequest()
        req.subAppId = properties.appId

        return vodClient.DescribeStorageRegions(req)
    }

    /**
     * https://cloud.tencent.com/document/product/266/31764
     *
     * 删除媒体
     */
    open fun deleteMedia(fileId: String): DeleteMediaResponse {
        val req = DeleteMediaRequest()
        req.fileId = fileId
        req.subAppId = properties.appId

        return vodClient.DeleteMedia(req)
    }


    /**
     * 使用任务流模板进行视频处理
     * https://cloud.tencent.com/document/product/266/34782
     */
    open fun processMediaByProcedure(
        fileId: String,
        procedureName: String
    ): ProcessMediaByProcedureResponse {
        val req = ProcessMediaByProcedureRequest()
        req.fileId = fileId
        req.procedureName = procedureName
        req.sessionId = UUID.randomUUID().toString()
        req.subAppId = properties.appId

        return vodClient.ProcessMediaByProcedure(req)
    }

    /**
     * 音视频转码
     *
     * https://cloud.tencent.com/document/product/266/33427
     */
    open fun processMediaTranscode(fileId: String, vararg templateId: Long): ProcessMediaResponse {
        val req = ProcessMediaRequest()
        req.fileId = fileId

        req.mediaProcessTask = MediaProcessTaskInput()
        val templateIds =
            if (templateId.isNotEmpty()) templateId.toTypedArray() else properties.templateIds

        req.mediaProcessTask.transcodeTaskSet = templateIds.map {
            val transcodeTaskInput = TranscodeTaskInput()
            transcodeTaskInput.definition = it
            transcodeTaskInput
        }.toTypedArray()

        req.subAppId = properties.appId

        req.sessionId = UUID.randomUUID().toString()

        return vodClient.ProcessMedia(req)
    }

    /**
     * 音视频审核
     *
     * https://cloud.tencent.com/document/product/266/33427
     */
    open fun processMediaAiReview(fileId: String): ProcessMediaResponse {
        val req = ProcessMediaRequest()
        req.fileId = fileId
        val aiContentReviewTaskInput = AiContentReviewTaskInput()
        aiContentReviewTaskInput.definition = 20
        req.aiContentReviewTask = aiContentReviewTaskInput
        req.subAppId = properties.appId

        req.sessionId = UUID.randomUUID().toString()

        return vodClient.ProcessMedia(req)
    }

    /**
     * https://cloud.tencent.com/document/product/266/73217
     *
     * 图片审核
     */
    open fun reviewImage(fileId: String): ReviewImageResponse {
        val req = ReviewImageRequest()
        req.fileId = fileId
        req.definition = 10L
        req.subAppId = properties.appId

        return vodClient.ReviewImage(req)
    }

    /**
     *
     * https://cloud.tencent.com/document/product/266/33433
     *
     * 拉取事件通知
     */
    open fun pullEvents(): PullEventsResponse {
        val req = PullEventsRequest()
        req.subAppId = properties.appId

        var resp: PullEventsResponse?
        try {
            resp = vodClient.PullEvents(req)
            return resp
        } catch (e: TencentCloudSDKException) {
            if (e.errorCode.equals("ResourceNotFound")) {
                resp = PullEventsResponse()
                resp.requestId = e.requestId
                resp.eventSet = arrayOf()
                return resp
            }
            throw e
        }
    }

    /**
     * https://cloud.tencent.com/document/product/266/33431
     * 查询任务详情
     */
    open fun taskDetail(taskId: String): DescribeTaskDetailResponse {
        val req = DescribeTaskDetailRequest()
        req.taskId = taskId
        req.subAppId = properties.appId

        return vodClient.DescribeTaskDetail(req)
    }

    /**
     *
     * https://cloud.tencent.com/document/product/266/33434
     * 确认事件通知
     */
    open fun confirmEvents(vararg eventHandle: String): ConfirmEventsResponse {
        val req = ConfirmEventsRequest()
        req.eventHandles = eventHandle
        req.subAppId = properties.appId

        return vodClient.ConfirmEvents(req)
    }

    private fun byteMerger(byte1: ByteArray, byte2: ByteArray): ByteArray {
        val byte3 = ByteArray(byte1.size + byte2.size)
        System.arraycopy(byte1, 0, byte3, 0, byte1.size)
        System.arraycopy(byte2, 0, byte3, byte1.size, byte2.size)
        return byte3
    }

}