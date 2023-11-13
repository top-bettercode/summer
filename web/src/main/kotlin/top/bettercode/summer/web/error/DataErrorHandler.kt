package top.bettercode.summer.web.error

import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.transaction.TransactionSystemException
import top.bettercode.summer.web.RespEntity
import java.sql.SQLRecoverableException
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

/**
 * @author Peter Wu
 */
class DataErrorHandler(messageSource: MessageSource,
                       request: HttpServletRequest?) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(error: Throwable, respEntity: RespEntity<*>, errors: MutableMap<String?, String?>, separator: String) {
        var e: Throwable? = error
        if (e is TransactionSystemException) { //数据验证
            e = e.rootCause
            if (e is ConstraintViolationException) {
                constraintViolationException(e, respEntity, errors,
                        separator)
            }
        } else if (e is DataIntegrityViolationException) {
            val specificCauseMessage = e.mostSpecificCause
                    .message!!.trim { it <= ' ' }
            val notNullRegex = "Column '(.*?)' cannot be null"
            val notNullRegex1 = "ORA-01400: 无法将 NULL 插入 \\(.+\\.\"(.*?)\"\\)"
            val duplicateRegex = "^Duplicate entry '(.*?)'.*"
            val dataTooLongRegex = "^Data truncation: Data too long for column '(.*?)'.*"
            val outOfRangeRegex = "^Data truncation: Out of range value for column '(.*?)'.*"
            val constraintSubfix = "Cannot delete or update a parent row"
            val incorrectRegex = "^Data truncation: Incorrect .* value: '(.*?)' for column '(.*?)' at.*"
            if (specificCauseMessage.matches(notNullRegex.toRegex())) {
                val columnName = getText(
                        specificCauseMessage.replace(notNullRegex.toRegex(), "$1"))
                respEntity.message = getText("notnull", columnName)
            } else if (specificCauseMessage.matches(notNullRegex1.toRegex())) {
                val columnName = getText(
                        specificCauseMessage.replace(notNullRegex1.toRegex(), "$1"))
                respEntity.message = getText("notnull", columnName)
            } else if (specificCauseMessage.matches(duplicateRegex.toRegex())) {
                val columnName = getText(
                        specificCauseMessage.replace(duplicateRegex.toRegex(), "$1"))
                var message = getText("duplicate.entry", columnName)
                if (message.isBlank()) {
                    message = "data.valid.failed"
                }
                respEntity.message = message
            } else if (specificCauseMessage.matches(dataTooLongRegex.toRegex())) {
                val columnName = getText(
                        specificCauseMessage.replace(dataTooLongRegex.toRegex(), "$1"))
                var message = getText("data.too.long", columnName)
                if (message.isBlank()) {
                    message = "data.valid.failed"
                }
                respEntity.message = message
            } else if (specificCauseMessage.matches(outOfRangeRegex.toRegex())) {
                val columnName = getText(
                        specificCauseMessage.replace(outOfRangeRegex.toRegex(), "$1"))
                var message = getText("data Out of range", columnName)
                if (message.isBlank()) {
                    message = "data.valid.failed"
                }
                respEntity.message = message
            } else if (specificCauseMessage.startsWith(constraintSubfix)) {
                var message = "cannot.delete.update.parent"
                if (message.isBlank()) {
                    message = "data.valid.failed"
                }
                respEntity.message = message
            } else if (specificCauseMessage.matches(incorrectRegex.toRegex())) {
                val columnName = getText(specificCauseMessage.replace(incorrectRegex.toRegex(), "$2"))
                respEntity.message = columnName + getText("incorrectFormatting")
            }
        } else if (e is UncategorizedSQLException) {
            val detailMessage = e.sqlException.message
                    ?.trim { it <= ' ' } ?: ""
            val regex = ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)"
            //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
            val regex1 = ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)"
            //Incorrect string value: '\xF0\x9F\x98\x84\xF0\x9F...' for column 'remark' at row 1
            if (detailMessage.matches("^Incorrect string value: '.*\\\\xF0.*$".toRegex())) {
                respEntity.message = "datasource.incorrect.emoji"
                respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
            } else if (detailMessage.matches(regex.toRegex())) {
                val field = detailMessage.replace(regex.toRegex(), "$1")
                val maxLeng = detailMessage.replace(regex.toRegex(), "$2")
                respEntity.message = getText(field) + getText("theLengthCannotBeGreaterThan") + maxLeng
                respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
            } else if (detailMessage.matches(regex1.toRegex())) {
                val field = detailMessage.replace(regex1.toRegex(), "$1")
                val maxLeng = detailMessage.replace(regex1.toRegex(), "$2")
                respEntity.message = getText(field) + getText("theLengthCannotBeGreaterThan") + maxLeng
                respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
            }
        } else if (e is DataAccessResourceFailureException || e is SQLRecoverableException) {
            val cause = e.cause
            val message = if (cause != null && "org.hibernate.exception.JDBCConnectionException" == cause.javaClass.name) {
                if (cause.cause != null) cause.cause!!.message else cause.message
            } else {
                e.message
            }
            if (message != null) {
                if (message.contains("Socket read timed out")) {
                    respEntity.message = "datasource.request.timeout"
                } else if (message.contains("Unable to acquire JDBC Connection") || message.contains("Connection is not available")) {
                    respEntity.message = "Unable to acquire JDBC Connection"
                }
            }
        }
    }
}
