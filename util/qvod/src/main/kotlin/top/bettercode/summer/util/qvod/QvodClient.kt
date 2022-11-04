package top.bettercode.summer.util.qvod

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonInclude
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.exception.TencentCloudSDKException
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.vod.v20180717.VodClient
import com.tencentcloudapi.vod.v20180717.models.*
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.Base64Utils
import org.springframework.util.DigestUtils
import top.bettercode.lang.util.RandomUtil
import top.bettercode.lang.util.StringUtil
import top.bettercode.simpleframework.support.client.ApiTemplate
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * 腾讯云接口
 *
 * @author Peter Wu
 */
open class QvodClient(
    val properties: QvodProperties
) : ApiTemplate(
    "第三方平台", "腾讯云点播", "qvod", properties.connectTimeout, properties.readTimeout
) {

    val vodClient: VodClient

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
            object : MappingJackson2HttpMessageConverter() {
                override fun canRead(@Nullable mediaType: MediaType?): Boolean {
                    return true
                }

                override fun canWrite(clazz: Class<*>, @Nullable mediaType: MediaType?): Boolean {
                    return true
                }
            }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        super.setMessageConverters(messageConverters)

        val cred = Credential(properties.secretId, properties.secretKey)
        val httpProfile = HttpProfile()
        httpProfile.endpoint = "vod.tencentcloudapi.com"
        val clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        vodClient = VodClient(cred, properties.region, clientProfile)

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
            }&classId=${properties.classId}&procedure=${properties.procedure ?: ""}"
        if (log.isDebugEnabled) {
            log.debug("original signature:{}", original)
        }
        val mac: Mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(properties.secretKey.toByteArray(), mac.algorithm)
        mac.init(secretKey)
        val signatureTmp: ByteArray = mac.doFinal(original.toByteArray())
        val signature = Base64Utils.encodeToString(
            byteMerger(
                signatureTmp,
                original.toByteArray(charset("utf8"))
            )
        ).replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
        log.info("signature: $signature")
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
    fun describeMediaInfo(
        fileId: String,
        filter: String
    ): DescribeMediaInfosResponse {
        return describeMediaInfos(arrayOf(fileId), arrayOf(filter))
    }

    /**
     * 获取媒体详细信息
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
    fun describeMediaInfos(
        fileIds: Array<String>,
        filters: Array<String>
    ): DescribeMediaInfosResponse {
        val req = DescribeMediaInfosRequest()
        req.fileIds = fileIds
        req.filters = filters

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: DescribeMediaInfosResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.DescribeMediaInfos(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 该接口用于： 1. 查询点播可开通的所有存储园区列表。 2. 查询已经开通的园区列表。 3. 查询默认使用的存储园区。
     */
    fun storageRegions(): DescribeStorageRegionsResponse {
        val req = DescribeStorageRegionsRequest()

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: DescribeStorageRegionsResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.DescribeStorageRegions(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 删除媒体
     */
    fun deleteMedia(fileId: String): DeleteMediaResponse {
        val req = DeleteMediaRequest()
        req.fileId = fileId

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: DeleteMediaResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.DeleteMedia(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 使用任务流模板进行视频处理
     * https://cloud.tencent.com/document/product/266/34782
     */
    fun processMediaByProcedure(
        fileId: String,
        procedureName: String
    ): ProcessMediaByProcedureResponse {
        val req = ProcessMediaByProcedureRequest()
        req.fileId = fileId
        req.procedureName = procedureName
        req.sessionId = UUID.randomUUID().toString()

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: ProcessMediaByProcedureResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.ProcessMediaByProcedure(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 音视频审核
     *
     * https://cloud.tencent.com/document/product/266/33427
     */
    fun processMedia(fileId: String): ProcessMediaResponse {
        val req = ProcessMediaRequest()
        req.fileId = fileId
        val aiContentReviewTaskInput = AiContentReviewTaskInput()
        aiContentReviewTaskInput.definition = 20
        req.aiContentReviewTask = aiContentReviewTaskInput

        req.sessionId = UUID.randomUUID().toString()
        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: ProcessMediaResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.ProcessMedia(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 图片审核
     */
    fun reviewImage(fileId: String): ReviewImageResponse {
        val req = ReviewImageRequest()
        req.fileId = fileId
        req.definition = 10L

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: ReviewImageResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.ReviewImage(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 拉取事件通知
     */
    fun pullEvents(): PullEventsResponse {
        val req = PullEventsRequest()

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: PullEventsResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.PullEvents(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: TencentCloudSDKException) {
            if (e.errorCode.equals("ResourceNotFound")) {
                resp = PullEventsResponse()
                resp.requestId = e.requestId
                resp.eventSet = arrayOf()
                return resp
            }
            throwable = e
            throw e
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 拉取事件通知
     */
    fun taskDetail(taskId: String): DescribeTaskDetailResponse {
        val req = DescribeTaskDetailRequest()
        req.taskId = taskId

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: DescribeTaskDetailResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.DescribeTaskDetail(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }

    /**
     * 确认事件通知
     */
    fun confirmEvents(vararg eventHandle: String): ConfirmEventsResponse {
        val req = ConfirmEventsRequest()
        req.eventHandles = eventHandle

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: ConfirmEventsResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodClient.ConfirmEvents(req)
            durationMillis = System.currentTimeMillis() - start
            return resp
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\n{}\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else StringUtil.json(resp, true)
            )
        }
    }


    private fun byteMerger(byte1: ByteArray, byte2: ByteArray): ByteArray {
        val byte3 = ByteArray(byte1.size + byte2.size)
        System.arraycopy(byte1, 0, byte3, 0, byte1.size)
        System.arraycopy(byte2, 0, byte3, byte1.size, byte2.size)
        return byte3
    }

}