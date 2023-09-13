package top.bettercode.summer.test.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.util.Assert
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.bettercode.summer.tools.lang.property.Settings.dicCode
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.web.BaseController
import top.bettercode.summer.web.exception.SystemException
import top.bettercode.summer.web.form.FormDuplicateCheck
import top.bettercode.summer.web.resolver.Unit
import top.bettercode.summer.web.support.code.CodeService
import top.bettercode.summer.web.support.code.ICodeService
import top.bettercode.summer.web.validator.ChinaCell
import java.util.*

/**
 * @author Peter Wu
 */
@RestController
@Validated
class WebTestController : BaseController() {
    @FormDuplicateCheck
    @RequestMapping(value = ["/webtest"])
    fun test(@Validated form: DataDicBean, @Unit cent: Long?, a: Date?, @ChinaCell cell: String?): Any {
        System.err.println(a)
        System.err.println(cent)
        Assert.isTrue(cent == 2200L, "cent != 2200")
        System.err.println(form.price)
        System.err.println(json(form, true))
        val dataDicBean = DataDicBean()
        dataDicBean.code = "code"
        dataDicBean.intCode = 1
        dataDicBean.path = "/abc.jpg"
        return ok(dataDicBean)
    }

    @FormDuplicateCheck
    @RequestMapping(value = ["/weberrors"])
    fun error(): Any {
        throw SystemException(HttpStatus.BAD_GATEWAY.value().toString(), "xx")
    }

    @Configuration(proxyBeanMethods = false)
    protected class CodeConfiguration {
        @Bean
        fun codeService(): ICodeService {
            return CodeService(dicCode)
        }
    }
}