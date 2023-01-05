package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.annotation.JsonInclude
import com.qcloud.vod.VodUploadClient
import com.qcloud.vod.model.VodUploadRequest
import com.qcloud.vod.model.VodUploadResponse
import org.slf4j.MarkerFactory
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.qvod.QvodUploadClient.Companion.LOG_MARKER
import top.bettercode.summer.web.form.IFormkeyService.log
import top.bettercode.summer.web.support.client.ApiTemplate
import java.io.File


/**
 * 腾讯云上传接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class QvodUploadClient(
    val properties: QvodProperties
) {

    companion object {
        const val LOG_MARKER = "qvod"
    }

    val vodUploadClient = VodUploadClient(properties.secretId, properties.secretKey)

    /**
     * 简单上传
     */
    @JvmOverloads
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
                MarkerFactory.getMarker(LOG_MARKER),
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

}