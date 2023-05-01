/**
 * @author Peter Wu
 */
package top.bettercode.summer.web.resolver.multipart

import top.bettercode.summer.tools.lang.util.FileUtil.delete
import top.bettercode.summer.tools.lang.util.FilenameUtil.getExtension
import top.bettercode.summer.tools.lang.util.FilenameUtil.getNameWithoutExtension
import top.bettercode.summer.web.serializer.annotation.JsonUrl
import org.springframework.web.multipart.MultipartFile
import top.bettercode.summer.web.resolver.multipart.MuipartFileToAttachmentConverter
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.HandlerMapping
import top.bettercode.summer.tools.lang.util.FilenameUtil
