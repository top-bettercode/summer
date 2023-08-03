package top.bettercode.summer.data.jpa.support

import org.hibernate.exception.GenericJDBCException
import org.springframework.context.MessageSource
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.util.StringUtils
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.error.AbstractErrorHandler
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
class DataJpaErrorHandler(
        messageSource: MessageSource,
        request: HttpServletRequest
) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(
            error: Throwable, respEntity: RespEntity<*>,
            errors: MutableMap<String?, String?>, separator: String
    ) {
        var message: String? = null
        if (error is JpaSystemException) {
            var cause = error.cause
            if (cause != null) {
                if (cause is GenericJDBCException) {
                    cause = if (cause.cause != null) cause.cause else cause
                }
                val causeMessage = cause!!.message
                if (causeMessage != null) {
                    message = causeMessage.trim { it <= ' ' }
                    //ORA-12899: 列 "YUNTUDEV"."PU_ASK_SEND_TMS"."FROM_ADDRESS" 的值太大 (实际值: 1421, 最大值: 600)
                    val regex = ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)"
                    //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
                    val regex1 = ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)"
                    if (message.matches(regex.toRegex())) {
                        val field = message.replace(regex.toRegex(), "$1")
                        val maxLeng = message.replace(regex.toRegex(), "$2")
                        message = getText(field) + "长度不能大于" + maxLeng
                        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
                    } else if (message.matches(regex1.toRegex())) {
                        val field = message.replace(regex1.toRegex(), "$1")
                        val maxLeng = message.replace(regex1.toRegex(), "$2")
                        message = getText(field) + "长度不能大于" + maxLeng
                        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
                    }
                }
            }
        } else if (error is InvalidDataAccessApiUsageException) {
            message = error.message
            if (message != null && message.contains("detached entity passed to persist")) {
                message = "更新的数据在数据库中不存在"
            }
        }
        if (StringUtils.hasText(message)) {
            respEntity.message = message
        }
    }
}
