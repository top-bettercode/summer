package top.bettercode.summer.web.resolver.multipart

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StreamUtils
import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.HandlerMapping
import top.bettercode.summer.tools.lang.util.FilenameUtil.getExtension
import top.bettercode.summer.tools.lang.util.FilenameUtil.getNameWithoutExtension
import top.bettercode.summer.web.properties.SummerMultipartProperties
import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * MultipartFile 转换为对应的url或路径。
 *
 * @author Peter Wu
 */
class MuipartFileToAttachmentConverter(private val multipartProperties: SummerMultipartProperties) : Converter<MultipartFile, Attachment> {
    private val log = LoggerFactory.getLogger(MuipartFileToAttachmentConverter::class.java)
    override fun convert(source: MultipartFile): Attachment {
        return try {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            val request = requestAttributes.request
            val name = source.name
            require(!source.isEmpty) { "不能上传空文件" }
            var fileType: String? = request.getAttribute(FILE_TYPE_PARAM_TYPE) as String
            if (fileType == null) {
                @Suppress("UNCHECKED_CAST") val values = (request
                        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, String>).values
                fileType = if (values.isEmpty()) {
                    request.getParameter(FILE_TYPE_PARAM_TYPE)
                } else {
                    values.iterator().next()
                }
                if (!StringUtils.hasText(fileType)) {
                    fileType = multipartProperties.defaultFileType
                }
            }
            var filePath = File.separator + fileType + File.separator
            filePath += LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            val originalFilename = source.originalFilename
            val hasOriginalName = StringUtils.hasText(originalFilename)
            val extension = if (hasOriginalName) getExtension(originalFilename!!) else ""
            var path: String
            if (multipartProperties.isKeepOriginalFilename) {
                filePath += File.separator + UUID.randomUUID() + File.separator
                val nameWithoutExtension = if (hasOriginalName) StringUtils
                        .trimAllWhitespace(getNameWithoutExtension(originalFilename!!)) else ""
                path = filePath + URLEncoder.encode(nameWithoutExtension, "UTF-8")
                filePath += nameWithoutExtension
            } else {
                filePath += File.separator + UUID.randomUUID()
                path = filePath
            }
            if (hasOriginalName) {
                filePath += ".$extension"
                path += ".$extension"
            }
            val baseSavePath = multipartProperties.baseSavePath
            val dest = File(baseSavePath, filePath)
            val parentFile = dest.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            StreamUtils.copy(source.inputStream, Files.newOutputStream(dest.toPath()))
            if (log.isDebugEnabled) {
                log.debug("上传文件保存至：" + dest.absolutePath)
            }
            val attachment = Attachment(originalFilename, path, dest)
            @Suppress("UNCHECKED_CAST") var files = request
                    .getAttribute(REQUEST_FILES) as MultiValueMap<String, Attachment>?
            if (files == null) {
                files = LinkedMultiValueMap()
                request.setAttribute(REQUEST_FILES, files)
            }
            files.add(name, attachment)
            attachment
        } catch (e: Exception) {
            cleanFile()
            log.error("转存文件失败", e)
            throw IllegalArgumentException("转存文件失败", e)
        }
    }

    /**
     * 清除上传的文件
     */
    fun cleanFile() {
        val requestAttributes = RequestContextHolder
                .getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request
        @Suppress("UNCHECKED_CAST") val files = request
                .getAttribute(REQUEST_FILES) as MultiValueMap<String, Attachment>?
        if (files != null) {
            for (attachments in files.values) {
                for (attachment in attachments) {
                    attachment.delete()
                }
            }
        }
    }

    companion object {
        const val FILE_TYPE_PARAM_TYPE = "fileType"
        val REQUEST_FILES = MuipartFileToAttachmentConverter::class.java.name + ":REQUEST_FILES"
    }
}