package top.bettercode.summer.web.kaptcha

import com.google.code.kaptcha.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.now
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.of
import java.time.temporal.ChronoUnit
import java.util.*
import javax.servlet.http.HttpSession

/**
 * @author Peter Wu
 */
open class DefaultCaptchaServiceImpl(@param:Autowired(required = false) protected val httpSession: HttpSession?,
                                private val kaptchaProperties: KaptchaProperties) : ICaptchaService {
    override fun save(loginId: String?, text: String?) {
        httpSession!!.setAttribute(Constants.KAPTCHA_SESSION_KEY, text)
        httpSession.setAttribute(Constants.KAPTCHA_SESSION_DATE, Date())
    }

    override fun match(loginId: String?, text: String): Boolean {
        Assert.hasText(loginId, "验证码错误")
        Assert.hasText(text, "验证码错误")
        val kaptcha = httpSession!!.getAttribute(Constants.KAPTCHA_SESSION_KEY) as String
        val date = httpSession.getAttribute(Constants.KAPTCHA_SESSION_DATE) as Date?
        return (date != null && of(date).toLocalDateTime()
                .plus(kaptchaProperties.expireSeconds.toLong(),
                        ChronoUnit.SECONDS).isAfter(now().toLocalDateTime())
                && text.equals(kaptcha, ignoreCase = true))
    }
}
