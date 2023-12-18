package top.bettercode.summer.web.kaptcha

import com.google.code.kaptcha.Producer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import top.bettercode.summer.logging.annotation.NoRequestLogging
import javax.imageio.ImageIO

@Controller
class CaptchaController(private val producer: Producer,
                        private val captchaService: ICaptchaService) {

    private val log = LoggerFactory.getLogger(CaptchaController::class.java)

    @NoRequestLogging
    @RequestMapping(value = ["/captcha.jpg"], name = "图片验证码")
    fun captcha(request: HttpServletRequest, response: HttpServletResponse, loginId: String?) {

        //生成文字验证码
        var id = loginId
        val text = producer.createText()
        if (log.isDebugEnabled) {
            log.debug("验证码：{}", text)
        }
        //生成图片验证码
        val image = producer.createImage(text)
        if (id.isNullOrBlank()) {
            id = request.requestedSessionId
        }
        captchaService.save(id, text)
        response.contentType = "image/jpeg"
        response.addHeader("loginId", id)
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setDateHeader("Expires", 0)
        val out = response.outputStream
        ImageIO.write(image, "jpg", out)
    }
}