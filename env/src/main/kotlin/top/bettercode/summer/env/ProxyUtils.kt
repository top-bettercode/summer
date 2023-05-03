package top.bettercode.summer.env

import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils

/**
 * @author Ryan Baxter
 */
class ProxyUtils private constructor() {
    init {
        throw IllegalStateException("Can't instantiate a utility class")
    }

    companion object {
        fun <T> getTargetObject(candidate: Any?): T? {
            try {
                if (AopUtils.isAopProxy(candidate) && candidate is Advised) {
                    @Suppress("UNCHECKED_CAST")
                    return candidate.targetSource.target as T
                }
            } catch (ex: Exception) {
                throw IllegalStateException("Failed to unwrap proxied object", ex)
            }
            @Suppress("UNCHECKED_CAST")
            return candidate as T?
        }
    }
}
