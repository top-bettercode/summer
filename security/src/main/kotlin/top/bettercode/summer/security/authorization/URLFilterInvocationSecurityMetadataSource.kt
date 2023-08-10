package top.bettercode.summer.security.authorization

import org.springframework.security.access.ConfigAttribute
import org.springframework.security.access.SecurityConfig
import org.springframework.security.web.FilterInvocation
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.util.AntPathMatcher
import org.springframework.util.StringUtils
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import top.bettercode.summer.security.IResourceService
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.security.authorize.ConfigAuthority
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.getAnnotation
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.hasAnnotation
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * 自定义权限过滤
 *
 * @author Peter Wu
 */
class URLFilterInvocationSecurityMetadataSource(
        private val securityService: IResourceService,
        handlerMapping: RequestMappingHandlerMapping,
        securityProperties: ApiSecurityProperties) : FilterInvocationSecurityMetadataSource {
    private val defaultConfigAttributes: MutableMap<AntPathRequestMatcher, MutableSet<ConfigAttribute>> = HashMap()
    private lateinit var requestMatcherConfigAttributes: MutableMap<AntPathRequestMatcher, MutableSet<ConfigAttribute>>

    // ~ Constructors
    // ===================================================================================================
    init {
        handlerMapping.handlerMethods.forEach { (mappingInfo: RequestMappingInfo, handlerMethod: HandlerMethod?) ->
            //非匿名权限
            if (!hasAnnotation(handlerMethod!!, Anonymous::class.java)) {
                for (pattern in mappingInfo.patternsCondition.patterns) {
                    if (!securityProperties.ignored(pattern!!)) {
                        val methods = mappingInfo.methodsCondition.methods
                        val authority = getAnnotation(handlerMethod, ConfigAuthority::class.java)
                        val configAttributes: MutableSet<ConfigAttribute> = HashSet()
                        if (authority != null) {
                            for (s in authority.value) {
                                configAttributes.add(SecurityConfig(s.trim { it <= ' ' }))
                            }
                        }
                        if (methods.isEmpty()) {
                            defaultConfigAttributes[AntPathRequestMatcher(pattern)] = configAttributes
                        } else {
                            for (requestMethod in methods) {
                                defaultConfigAttributes[AntPathRequestMatcher(pattern, requestMethod.name)] = configAttributes
                            }
                        }
                    }
                }
            }
        }
        bindConfigAttributes()
    }

    /**
     * @return 不再检查是否支持
     */
    override fun getAllConfigAttributes(): Collection<ConfigAttribute> {
        return emptyList()
    }

    override fun getAttributes(`object`: Any): Collection<ConfigAttribute> {
        val request = (`object` as FilterInvocation).request
        val matches: MutableList<Match> = ArrayList()
        val comparator = AntPathMatcher()
                .getPatternComparator(getRequestPath(request))
        for ((key, value) in requestMatcherConfigAttributes) {
            if (key.matches(request)) {
                matches.add(Match(comparator, key.pattern, value))
            }
        }
        if (matches.isNotEmpty()) {
            matches.sort()
            val bestMatch = matches[0]
            if (matches.size > 1) {
                val secondBestMatch = matches[1]
                if (comparator.compare(bestMatch.path, secondBestMatch.path) == 0) {
                    val m1 = bestMatch.path
                    val m2 = secondBestMatch.path
                    throw IllegalStateException("Ambiguous handler methods mapped for HTTP path '" +
                            request.requestURL + "': {" + m1 + ", " + m2 + "}")
                }
            }
            return bestMatch.configAttributes.ifEmpty {
                SecurityConfig
                        .createList("authenticated")
            }
        }
        return emptyList()
    }

    private fun getRequestPath(request: HttpServletRequest): String {
        var url = request.servletPath
        if (request.pathInfo != null) {
            url += request.pathInfo
        }
        return url
    }

    private class Match(private val comparator: Comparator<String>, val path: String,
                        val configAttributes: Collection<ConfigAttribute>) : Comparable<Match> {
        override fun compareTo(other: Match): Int {
            return comparator.compare(path, other.path)
        }
    }

    override fun supports(clazz: Class<*>?): Boolean {
        return FilterInvocation::class.java.isAssignableFrom(clazz)
    }

    protected fun bindConfigAttributes() {
        requestMatcherConfigAttributes = HashMap(defaultConfigAttributes)
        val allResources = securityService.findAllResources()
        for (resource in allResources) {
            val ress = resource.ress
            val configAttribute: ConfigAttribute = SecurityConfig(resource.mark.trim { it <= ' ' })
            for (api in ress.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (api.contains(":")) {
                    val methodUrl = api.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val method = methodUrl[0].toUpperCase()
                    val url = methodUrl[1]
                    for (u in url.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        if (StringUtils.hasText(method)) {
                            for (m in method.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                                val authorities = requestMatcherConfigAttributes
                                        .computeIfAbsent(AntPathRequestMatcher(u, m)
                                        ) { _: AntPathRequestMatcher? -> HashSet() }
                                authorities.add(configAttribute)
                            }
                        } else {
                            val authorities = requestMatcherConfigAttributes
                                    .computeIfAbsent(AntPathRequestMatcher(u)
                                    ) { _: AntPathRequestMatcher? -> HashSet() }
                            authorities.add(configAttribute)
                        }
                    }
                } else {
                    for (u in api.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        val authorities = requestMatcherConfigAttributes
                                .computeIfAbsent(AntPathRequestMatcher(u)) { _: AntPathRequestMatcher? -> HashSet() }
                        authorities.add(configAttribute)
                    }
                }
            }
        }
    }

    /**
     * 刷新资源权限配置
     */
    fun refreshResuorces() {
        synchronized(this) { bindConfigAttributes() }
    }
}