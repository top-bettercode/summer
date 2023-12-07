package top.bettercode.summer.web.config

import com.google.code.kaptcha.Producer
import com.google.code.kaptcha.impl.DefaultKaptcha
import com.google.code.kaptcha.util.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.kaptcha.CaptchaController
import top.bettercode.summer.web.kaptcha.DefaultCaptchaServiceImpl
import top.bettercode.summer.web.kaptcha.ICaptchaService
import top.bettercode.summer.web.kaptcha.KaptchaProperties
import java.util.*
import javax.servlet.http.HttpSession


@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DefaultKaptcha::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(KaptchaProperties::class)
class KaptchaConfiguration {
    @Bean
    @ConditionalOnMissingBean(Producer::class)
    fun kaptcha(kaptchaProperties: KaptchaProperties): Producer {
        val properties = Properties()
        properties["kaptcha.border"] = kaptchaProperties.border
        properties["kaptcha.textproducer.font.color"] = kaptchaProperties.textproducerFontColor
        properties["kaptcha.textproducer.char.space"] = kaptchaProperties.textproducerCharSpace
        val config = Config(properties)
        val defaultKaptcha = DefaultKaptcha()
        defaultKaptcha.config = config
        return defaultKaptcha
    }

    @Bean
    @ConditionalOnMissingBean(ICaptchaService::class)
    fun captchaService(@Autowired(required = false) httpSession: HttpSession?,
                       kaptchaProperties: KaptchaProperties): ICaptchaService {
        return DefaultCaptchaServiceImpl(httpSession, kaptchaProperties)
    }

    @Bean
    fun captchaController(producer: Producer, captchaService: ICaptchaService): CaptchaController {
        return CaptchaController(producer, captchaService)
    }
}