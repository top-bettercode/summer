package top.bettercode.summer.util.qvod

import com.fasterxml.jackson.annotation.JsonInclude
import com.qcloud.vod.VodUploadClient
import com.qcloud.vod.model.VodUploadRequest
import com.qcloud.vod.model.VodUploadResponse
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.vod.v20180717.VodClient
import com.tencentcloudapi.vod.v20180717.models.*
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.Base64Utils
import top.bettercode.lang.util.RandomUtil
import top.bettercode.lang.util.StringUtil
import top.bettercode.simpleframework.support.client.ApiTemplate
import java.io.File
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * 天气接口
 *
 * @author Peter Wu
 */
open class QvodClient(
    private val properties: QvodProperties
) : ApiTemplate(
    "第三方平台", "腾讯云点播", "qvod", properties.connectTimeout, properties.readTimeout
) {

    val vodClient: VodClient
    val vodUploadClient = VodUploadClient(properties.secretId, properties.secretKey)

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
     * 签名
     */
    @JvmOverloads
    fun signature(isPicture: Boolean = false): String {
        val currentTimeStamp = System.currentTimeMillis() / 1000
        val original =
            "secretId=${properties.secretId}&currentTimeStamp=$currentTimeStamp&expireTime=${currentTimeStamp + properties.validSeconds}&random=${
                RandomUtil.nextInt(9)
            }${if (isPicture || properties.procedure.isNullOrBlank()) "" else "&procedure=${properties.procedure}"}"
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
     * 简单上传
     */
    fun upload(file: File, procedure: String? = null): VodUploadResponse {
        val req = VodUploadRequest()
        req.mediaFilePath = file.absolutePath
        if (procedure != null) {
            req.procedure = procedure
        }

        val start = System.currentTimeMillis()
        var durationMillis: Long? = null

        var resp: VodUploadResponse? = null
        var throwable: Throwable? = null
        try {
            resp = vodUploadClient.upload(properties.region, req)
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
                "DURATION MILLIS : {}\\n{}\\n{}",
                durationMillis,
                VodUploadRequest.toJsonString(req),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else VodUploadResponse.toJsonString(resp)
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
                "DURATION MILLIS : {}\\n{}\\n{}",
                durationMillis,
                VodUploadRequest.toJsonString(req),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else DeleteMediaResponse.toJsonString(resp)
            )
        }
    }

    /**
     * 音视频审核
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
                "DURATION MILLIS : {}\\n{}\\n{}",
                durationMillis,
                ProcessMediaRequest.toJsonString(req),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else ProcessMediaResponse.toJsonString(resp)
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
                "DURATION MILLIS : {}\\n{}\\n{}",
                durationMillis,
                ReviewImageRequest.toJsonString(req),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else ReviewImageResponse.toJsonString(resp)
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
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            log.info(
                "DURATION MILLIS : {}\\n{}\\n{}",
                durationMillis,
                PullEventsRequest.toJsonString(req),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else PullEventsResponse.toJsonString(resp)
            )
        }
    }

    /**
     * 确认事件通知
     */
    fun confirmEvents(vararg eventHandles: String): ConfirmEventsResponse {
        val req = ConfirmEventsRequest()
        req.eventHandles = eventHandles

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
                "DURATION MILLIS : {}\\n{}\\n{}",
                durationMillis,
                ConfirmEventsRequest.toJsonString(req),
                if (resp == null) StringUtil.valueOf(
                    throwable,
                    true
                ) else ConfirmEventsResponse.toJsonString(resp)
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