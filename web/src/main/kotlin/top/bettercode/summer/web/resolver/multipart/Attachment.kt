package top.bettercode.summer.web.resolver.multipart

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.FileUtil.delete
import top.bettercode.summer.web.serializer.annotation.JsonUrl
import java.io.File
import java.io.Serializable

/**
 * @author Peter Wu
 * @since 0.1.31
 */
open class Attachment(var name: String?, @field:JsonUrl var path: String, @field:JsonIgnore var file: File?) : Serializable {

    val length: Long
        get() = file!!.length()

    //--------------------------------------------
    fun delete(): Boolean {
        if (file != null) {
            val delete = delete(file!!)
            if (delete) {
                log.info("删除出错请求上传的文件：{}", file)
            }
            return delete
        }
        return false
    }

    companion object {
        protected val log: Logger = LoggerFactory.getLogger(Attachment::class.java)
    }
}
