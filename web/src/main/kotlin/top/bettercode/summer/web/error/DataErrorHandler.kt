package top.bettercode.summer.web.error

import org.springframework.context.MessageSource
import org.springframework.dao.*
import org.springframework.http.HttpStatus
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.transaction.CannotCreateTransactionException
import org.springframework.transaction.TransactionSystemException
import top.bettercode.summer.web.RespEntity
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLRecoverableException
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

/**
 * @author Peter Wu
 */
class DataErrorHandler(
    messageSource: MessageSource,
    request: HttpServletRequest?
) : AbstractErrorHandler(messageSource, request) {
    override fun handlerException(
        error: Throwable,
        respEntity: RespEntity<*>,
        errors: MutableMap<String?, String?>,
        separator: String
    ) {
        var e: Throwable? = error
        if (e is TransactionSystemException) { //数据验证
            e = e.rootCause
            if (e is ConstraintViolationException) {
                constraintViolationException(
                    e, respEntity, errors,
                    separator
                )
            }
        } else if (e is UncategorizedSQLException) {
            val detailMessage = e.sqlException.message
                ?.trim { it <= ' ' } ?: ""
            val regex =
                ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)"
            //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
            val regex1 =
                ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)"
            //Incorrect string value: '\xF0\x9F\x98\x84\xF0\x9F...' for column 'remark' at row 1
            if (detailMessage.matches("^Incorrect string value: '.*\\\\xF0.*$".toRegex())) {
                respEntity.message = "datasource.incorrect.emoji"
                respEntity.httpStatusCode = HttpStatus.BAD_REQUEST.value()
            } else if (detailMessage.matches(regex.toRegex())) {
                val field = detailMessage.replace(regex.toRegex(), "$1")
                val maxLeng = detailMessage.replace(regex.toRegex(), "$2")
                respEntity.message =
                    getText(field) + getText("theLengthCannotBeGreaterThan") + maxLeng
                respEntity.httpStatusCode = HttpStatus.BAD_REQUEST.value()
                errors[field] = respEntity.message
            } else if (detailMessage.matches(regex1.toRegex())) {
                val field = detailMessage.replace(regex1.toRegex(), "$1")
                val maxLeng = detailMessage.replace(regex1.toRegex(), "$2")
                respEntity.message =
                    getText(field) + getText("theLengthCannotBeGreaterThan") + maxLeng
                respEntity.httpStatusCode = HttpStatus.BAD_REQUEST.value()
                errors[field] = respEntity.message
            } else {
                respEntity.message = "UncategorizedSQL"
            }
        } else if (e is DataAccessResourceFailureException || e is SQLRecoverableException) {
            val cause = e.cause
            val message =
                if (cause != null && "org.hibernate.exception.JDBCConnectionException" == cause.javaClass.name) {
                    if (cause.cause != null) cause.cause!!.message else cause.message
                } else {
                    e.message
                }
            if (message != null) {
                if (message.contains("Socket read timed out")) {
                    respEntity.message = "datasource.request.timeout"
                } else if (message.contains("Unable to acquire JDBC Connection") || message.contains(
                        "Connection is not available"
                    )
                ) {
                    respEntity.message = "Unable to acquire JDBC Connection"
                }
            }
        } else if (e is OptimisticLockingFailureException) {
            respEntity.message = "data.optimistic.locking.failure"
        } else if (e is PessimisticLockingFailureException) {
            respEntity.message = "data.optimistic.locking.failure"
        } else if (e is CannotCreateTransactionException) {
            respEntity.message = "datasource.request.timeout"
        } else if (e is EmptyResultDataAccessException) {
            respEntity.message = "resource.not.found"
        } else if (e is IncorrectResultSizeDataAccessException) {
            if (error.message != null && error.message!!.matches(".*query did not return a unique result:.*".toRegex())) {
                respEntity.message = "data.not.unique.result"
            }
        } else if (e != null) {
            val sqlCause = getSqlCause(e)
            if (sqlCause != null) {
                val specificCauseMessage = (sqlCause.message ?: e.message)?.trim()
                if (!specificCauseMessage.isNullOrBlank()) {
                    val notNullRegex = "Column '(.*?)' cannot be null".toRegex()
                    val notNullRegex1 = "ORA-01400: 无法将 NULL 插入 \\(.+\\.\"(.*?)\"\\)".toRegex()
                    val notNullRegex2 = "ORA-01407: 无法更新 \\(.+\\.\"(.*?)\"\\) 为 NULL".toRegex()
                    val duplicateRegex = "^Duplicate entry '(.*?)'.*".toRegex()
                    val dataTooLongRegex =
                        "^Data truncation: Data too long for column '(.*?)'.*".toRegex()
                    val outOfRangeRegex =
                        "^Data truncation: Out of range value for column '(.*?)'.*".toRegex()
                    val constraintSubfix = "Cannot delete or update a parent row"
                    val incorrectRegex =
                        "^Data truncation: Incorrect .* value: '(.*?)' for column '(.*?)' at.*".toRegex()
                    if (specificCauseMessage.matches(notNullRegex)) {
                        val columnName = specificCauseMessage.replace(notNullRegex, "$1")
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = getText("notnull", getText(columnName))
                        errors[columnName] = respEntity.message
                    } else if (specificCauseMessage.matches(notNullRegex1)) {
                        val columnName = specificCauseMessage.replace(notNullRegex1, "$1")
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = getText("notnull", getText(columnName))
                        errors[columnName] = respEntity.message
                    } else if (specificCauseMessage.matches(notNullRegex2)) {
                        val columnName =
                            specificCauseMessage.replace(notNullRegex2, "$1")
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = getText("notnull", getText(columnName))
                        errors[columnName] = respEntity.message
                    } else if (specificCauseMessage.matches(duplicateRegex)) {
                        val columnName =
                            specificCauseMessage.replace(duplicateRegex, "$1")

                        var message = getText("duplicate.entry", getText(columnName))
                        if (message.isBlank()) {
                            message = "data.valid.failed"
                        }
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = message
                        errors[columnName] = respEntity.message
                    } else if (specificCauseMessage.matches(dataTooLongRegex)) {
                        val columnName =
                            specificCauseMessage.replace(dataTooLongRegex, "$1")

                        var message = getText("data.too.long", getText(columnName))
                        if (message.isBlank()) {
                            message = "data.valid.failed"
                        }
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = message
                        errors[columnName] = respEntity.message
                    } else if (specificCauseMessage.matches(outOfRangeRegex)) {
                        val columnName =
                            specificCauseMessage.replace(outOfRangeRegex, "$1")
                        var message = getText("data Out of range", getText(columnName))
                        if (message.isBlank()) {
                            message = "data.valid.failed"
                        }
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = message
                        errors[columnName] = respEntity.message
                    } else if (specificCauseMessage.startsWith(constraintSubfix)) {
                        var message = "cannot.delete.update.parent"
                        if (message.isBlank()) {
                            message = "data.valid.failed"
                        }
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = message
                    } else if (specificCauseMessage.matches(incorrectRegex)) {
                        val columnName =
                            specificCauseMessage.replace(incorrectRegex, "$2")
                        respEntity.httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value()
                        respEntity.message = getText(columnName) + getText("incorrectFormatting")
                        errors[columnName] = respEntity.message
                    } else {
                        respEntity.message = "Data Integrity Violation Exception"
                    }
                }
            }
        }
    }

    private fun getSqlCause(original: Throwable?): Throwable? {
        if (original == null) {
            return null
        } else {
            var rootCause: Throwable? = null

            var cause: Throwable? = original.cause
            while (cause != null && cause !is SQLIntegrityConstraintViolationException && cause !is SQLException && cause != rootCause) {
                rootCause = cause
                cause = cause.cause
            }

            return cause ?: rootCause
        }
    }
}
