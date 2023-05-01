package top.bettercode.summer.web.validator

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.UnknownHostException
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * `IP` 验证器
 *
 * @author Peter wu
 */
class IPValidator : ConstraintValidator<IP?, String?> {
    override fun initialize(constraintAnnotation: IP?) {}
    override fun isValid(charSequence: String?,
                         constraintValidatorContext: ConstraintValidatorContext): Boolean {
        return if (charSequence == null || charSequence.length == 0) {
            true
        } else isValidInet4Address(charSequence) || isValidInet6Address(charSequence)
    }

    companion object {
        fun isValidInet4Address(ip: String): Boolean {
            return try {
                Inet4Address.getByName(ip).hostAddress == ip
            } catch (ex: UnknownHostException) {
                false
            }
        }

        fun isValidInet6Address(ip: String): Boolean {
            return try {
                Inet6Address.getByName(ip).hostAddress == ip
            } catch (ex: UnknownHostException) {
                false
            }
        }
    }
}
