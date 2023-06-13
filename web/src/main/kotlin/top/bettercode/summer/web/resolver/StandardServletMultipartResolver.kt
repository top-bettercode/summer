package top.bettercode.summer.web.resolver

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * 支持PUT上传
 *
 * @author Peter Wu
 */
class StandardServletMultipartResolver : MultipartResolver {
    private val log = LoggerFactory.getLogger(StandardServletMultipartResolver::class.java)
    private var resolveLazily = false
    fun setResolveLazily(resolveLazily: Boolean) {
        this.resolveLazily = resolveLazily
    }

    override fun isMultipart(request: HttpServletRequest): Boolean {
        // Same check as in Commons FileUpload...
        val method = request.method
        if (RequestMethod.POST.name == method || (RequestMethod.PUT.name
                        == method)) { //支持PUT方法
            val contentType = request.contentType
            return contentType != null && contentType.lowercase(Locale.getDefault()).startsWith("multipart/")
        }
        return false
    }

    override fun resolveMultipart(request: HttpServletRequest): MultipartHttpServletRequest {
        return StandardMultipartHttpServletRequest(request, resolveLazily)
    }

    override fun cleanupMultipart(request: MultipartHttpServletRequest) {
        // To be on the safe side: explicitly delete the parts,
        // but only actual file parts (for Resin compatibility)
        try {
            for (part in request.parts) {
                if (request.getFile(part.name) != null) {
                    part.delete()
                }
            }
        } catch (ex: Exception) {
            if (log.isWarnEnabled) {
                log.warn("Failed to perform cleanup of multipart items", ex)
            }
        }
    }
}
