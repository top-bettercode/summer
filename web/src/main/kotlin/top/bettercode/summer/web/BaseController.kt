package top.bettercode.summer.web

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.MessageSource
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.util.ParameterUtil.hasParameter
import top.bettercode.summer.tools.lang.util.ParameterUtil.hasParameterKey
import top.bettercode.summer.web.error.ErrorAttributes
import top.bettercode.summer.web.exception.ResourceNotFoundException
import top.bettercode.summer.web.support.DeviceUtil
import java.util.*
import java.util.function.Supplier
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 基础Controller
 *
 * @author Peter Wu
 */
@ConditionalOnWebApplication
open class BaseController : Response() {

    @JvmField
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    @JvmField
    @Autowired(required = false)
    protected var request: HttpServletRequest? = null

    @JvmField
    @Autowired(required = false)
    protected var response: HttpServletResponse? = null

    @Autowired
    private val mappingJackson2HttpMessageConverter: MappingJackson2HttpMessageConverter? = null

    @Autowired(required = false)
    private val servletContext: ServletContext? = null

    @Autowired
    private val messageSource: MessageSource? = null
    fun plainTextError() {
        request!!.setAttribute(ErrorAttributes.IS_PLAIN_TEXT_ERROR, true)
    }

    /**
     * 得到国际化信息 未找到时返回代码 code
     *
     * @param code 模板
     * @param args 参数
     * @return 信息
     */
    fun getText(code: Any, vararg args: Any?): String? {
        val codeString = code.toString()
        return messageSource!!.getMessage(codeString, args, codeString,
                if (request == null) Locale.CHINA else request!!.locale)
    }

    /**
     * 得到国际化信息，未找到时返回 `null`
     *
     * @param code 模板
     * @param args 参数
     * @return 信息
     */
    fun getTextDefaultNull(code: Any, vararg args: Any?): String? {
        return messageSource!!.getMessage(code.toString(), args, null,
                if (request == null) Locale.CHINA else request!!.locale)
    }

    /**
     * @param path 路径
     * @return 真实路径
     */
    fun getRealPath(path: String?): String {
        return servletContext!!.getRealPath(path)
    }

    val userAgent: String?
        /**
         * @return UserAgent
         */
        get() = DeviceUtil.getUserAgent(request!!)

    /**
     * @param key 参数名称
     * @return 是否存在此参数（非空），此方法在request body方式提交数据时可能无效
     */
    protected fun hasParameter(key: String?): Boolean {
        return hasParameter(request!!.parameterMap, key!!)
    }

    /**
     * @param key 参数名称
     * @return 是否存在此参数（可为空）
     */
    protected fun hasParameterKey(key: String?): Boolean {
        return hasParameterKey(request!!.parameterMap, key!!)
    }

    protected fun hasText(param: String?, paramName: String?) {
        require(StringUtils.hasText(param)) { getText("param.notnull", paramName) ?: "" }
    }

    protected fun notNull(param: Any?, paramName: String?) {
        requireNotNull(param) { getText("param.notnull", paramName) ?: "" }
    }

    protected fun assertOk(respEntity: RespEntity<*>) {
        RespEntity.Companion.assertOk(respEntity)
    }

    protected fun assertOk(respEntity: RespEntity<*>, message: String?) {
        RespEntity.Companion.assertOk(respEntity, message)
    }

    companion object {
        @JvmStatic
        fun notFound(): Supplier<out RuntimeException> {
            return Supplier<RuntimeException> { ResourceNotFoundException() }
        }

        @JvmStatic
        fun notFound(msg: String?): Supplier<out RuntimeException> {
            return Supplier<RuntimeException> { ResourceNotFoundException(msg) }
        }
    }
}
