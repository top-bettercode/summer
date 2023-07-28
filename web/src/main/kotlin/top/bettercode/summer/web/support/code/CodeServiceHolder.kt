package top.bettercode.summer.web.support.code

import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.property.Settings.dicCode
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author Peter Wu
 */
object CodeServiceHolder {
    private val CODE_SERVICE_MAP: ConcurrentMap<String, ICodeService> = ConcurrentHashMap()
    val PROPERTIES_CODESERVICE: ICodeService = CodeService(dicCode)
    const val DEFAULT_BEAN_NAME = "defaultCodeService"

    @JvmStatic
    val default: ICodeService
        get() = CodeServiceHolder[DEFAULT_BEAN_NAME]

    @JvmStatic
    operator fun get(beanName: String): ICodeService {
        val codeService = CODE_SERVICE_MAP.computeIfAbsent(
                if (StringUtils.hasText(beanName)) beanName else DEFAULT_BEAN_NAME
        ) { s: String -> if (ApplicationContextHolder.isInitialized) ApplicationContextHolder.getBean(s, ICodeService::class.java) else null }
        return codeService ?: PROPERTIES_CODESERVICE
    }
}
