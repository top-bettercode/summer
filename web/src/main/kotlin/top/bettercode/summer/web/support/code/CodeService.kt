package top.bettercode.summer.web.support.code

import com.github.benmanes.caffeine.cache.Caffeine
import top.bettercode.summer.tools.lang.property.PropertiesSource
import java.util.concurrent.TimeUnit

/**
 * @author Peter Wu
 */
class CodeService(private val propertiesSource: PropertiesSource) : ICodeService {
    private val expireSeconds = 60L
    private val cache = Caffeine.newBuilder()
            .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build<String, DicCodes>()

    override fun getDicCodes(codeType: String): DicCodes? {
        return cache[codeType, { type: String ->
            DicCodes(type, propertiesSource[type],
                    propertiesSource.mapOf(type, "Int" == propertiesSource["$type|TYPE"]))
        }]
    }
}
