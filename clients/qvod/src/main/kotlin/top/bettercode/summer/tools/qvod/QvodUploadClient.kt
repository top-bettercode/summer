package top.bettercode.summer.tools.qvod

import com.qcloud.vod.VodUploadClient
import com.qcloud.vod.model.VodUploadRequest
import com.qcloud.vod.model.VodUploadResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.StringUtil
import java.io.File


/**
 * 腾讯云上传接口
 *
 * @author Peter Wu
 */
@LogMarker(QvodUploadClient.MARKER)
open class QvodUploadClient(
    val properties: QvodProperties
) {

    private val log: Logger = LoggerFactory.getLogger(QvodUploadClient::class.java)

    companion object {
        const val MARKER = "qvod"
    }

    val vodUploadClient = VodUploadClient(properties.secretId, properties.secretKey)

    /**
     * 简单上传
     */
    @JvmOverloads
    open fun upload(file: File, procedure: String? = null): VodUploadResponse {
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
                MarkerFactory.getMarker(MARKER),
                "DURATION MILLIS : {}\n{}\n\n{}",
                durationMillis,
                StringUtil.json(req, true),
                if (resp == null) StringUtil.valueOf(throwable) else StringUtil.json(resp, true)
            )
        }
    }

}