package top.bettercode.summer.data.jpa.support

import org.hibernate.exception.GenericJDBCException
import org.springframework.context.MessageSource
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.orm.jpa.JpaSystemException
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
        if (error is JpaSystemException) {
            var cause = error.cause
            if (cause != null) {
                if (cause is GenericJDBCException) {
                    cause = if (cause.cause != null) cause.cause else cause
                }
                val causeMessage = cause!!.message
                if (causeMessage != null) {
                    val message = causeMessage.trim { it <= ' ' }
                    //ORA-12899: 列 "YUNTUDEV"."PU_ASK_SEND_TMS"."FROM_ADDRESS" 的值太大 (实际值: 1421, 最大值: 600)
                    val regex = ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)"
                    //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
                    val regex1 = ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)"
                    if (message.matches(regex.toRegex())) {
                        val field = message.replace(regex.toRegex(), "$1")
                        val maxLeng = message.replace(regex.toRegex(), "$2")
                        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
                        respEntity.message = getText(field) + getText("theLengthCannotBeGreaterThan") + maxLeng
                    } else if (message.matches(regex1.toRegex())) {
                        val field = message.replace(regex1.toRegex(), "$1")
                        val maxLeng = message.replace(regex1.toRegex(), "$2")
                        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
                        respEntity.message = getText(field) + getText("theLengthCannotBeGreaterThan") + maxLeng
                    }
                }
            }
            if(respEntity.message.isNullOrBlank()){
                respEntity.message = "JpaSystemException"
            }
        } else if (error is InvalidDataAccessApiUsageException) {
            if (error.message != null && error.message!!.contains("detached entity passed to persist")) {
                respEntity.message = "theUpdatedDataDoesNotExistInTheDatabase"
            }
        } else if (error is org.hibernate.NonUniqueResultException) {
            if (error.message != null && error.message!!.matches(".*query did not return a unique result:.*".toRegex())) {
                respEntity.message = "data.not.unique.result"
            }
        } else if (error is ObjectOptimisticLockingFailureException) {
            respEntity.message = "data.optimistic.locking.failure"
        }

    }
}
