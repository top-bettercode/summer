package top.bettercode.summer.web.support.code

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import top.bettercode.summer.tools.lang.property.Settings.dicCode
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

/**
 * @author Peter Wu
 */
object CodeServiceHolder {
    private val CODE_SERVICE_MAP: ConcurrentMap<String, ICodeService> = ConcurrentHashMap()
    private val cache: Cache<String, DicCodes> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(200).build()

    val PROPERTIES_CODESERVICE: ICodeService = CodeService(dicCode)
    const val DEFAULT_BEAN_NAME = "defaultCodeService"

    @JvmStatic
    val default: ICodeService
        get() = CodeServiceHolder[DEFAULT_BEAN_NAME]

    @JvmStatic
    operator fun get(beanName: String?): ICodeService {
        val codeService = CODE_SERVICE_MAP.computeIfAbsent(
            if (beanName.isNullOrBlank()) DEFAULT_BEAN_NAME else beanName
        ) { s: String -> ApplicationContextHolder.getBean(s, ICodeService::class.java) }
        return codeService ?: PROPERTIES_CODESERVICE
    }

    @JvmStatic
    fun get(beanName: String?, codeType: String): DicCodes? {
        val codeService = get(beanName)
        return if (PROPERTIES_CODESERVICE == codeService) codeService.getDicCodes(codeType) else cache.get(
            "$beanName|$codeType"
        ) {
            codeService.getDicCodes(codeType)
        }
    }
}
