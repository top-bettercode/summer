package top.bettercode.summer.tools.sap.connection

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class SapSysException(message: String?) : RuntimeException("SAP系统：$message") {
    companion object
}
