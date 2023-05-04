package top.bettercode.summer.security.authorization

import org.slf4j.LoggerFactory
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.util.AntPathMatcher
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import top.bettercode.summer.security.IResourceService
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.security.authorize.ConfigAuthority
import top.bettercode.summer.security.authorize.DefaultAuthority
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.getAnnotations
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

/**
 * 自定义权限过滤
 *
 * @author Peter Wu
 */
class RequestMappingAuthorizationManager(
        private val securityService: IResourceService,
        handlerMapping: RequestMappingHandlerMapping,
        securityProperties: ApiSecurityProperties,
) : AuthorizationManager<RequestAuthorizationContext> {
    private val log = LoggerFactory.getLogger(RequestMappingAuthorizationManager::class.java)
    private val defaultConfigAuthorities: MutableMap<AntPathRequestMatcher, MutableSet<String>> = HashMap()
    private var configAuthorities: MutableMap<AntPathRequestMatcher, MutableSet<String>> = HashMap()

    // ~ Constructors
    // ===================================================================================================
    init {
        handlerMapping.handlerMethods.forEach { (mappingInfo: RequestMappingInfo, handlerMethod: HandlerMethod?) ->
            for (pathPattern in Objects.requireNonNull(mappingInfo.pathPatternsCondition)
                    .patterns) {
                val pattern = pathPattern.patternString
                val methods = mappingInfo.methodsCondition.methods
                val authorities: MutableSet<String> = HashSet()
                if (securityProperties.ignored(pattern)) {
                    authorities.add(Anonymous.ROLE_ANONYMOUS_VALUE)
                } else {
                    val authoritySet = getAnnotations(handlerMethod!!, ConfigAuthority::class.java)
                    if (authoritySet.isNotEmpty()) {
                        for (authority in authoritySet) {
                            for (s in authority.value) {
                                Assert.hasText(s, "权限标记不能为空")
                                authorities.add(s)
                            }
                        }
                    }
                }
                if (methods.isEmpty()) {
                    defaultConfigAuthorities[AntPathRequestMatcher(pattern)] = authorities
                } else {
                    for (requestMethod in methods) {
                        defaultConfigAuthorities[AntPathRequestMatcher(pattern, requestMethod.name)] = authorities
                    }
                }
            }
        }
        bindAuthorizationManager()
    }

    override fun check(
            authentication: Supplier<Authentication>,
            requestAuthorizationContext: RequestAuthorizationContext,
    ): AuthorizationDecision {
        val request = requestAuthorizationContext.request
        if (log.isTraceEnabled) {
            log.trace("Authorizing {}", request)
        }
        val userAuthorities = authentication.get().authorities
        var matchers = configAuthorities.entries.stream()
                .filter { (key): Map.Entry<AntPathRequestMatcher, Set<String?>> -> key.matcher(request).isMatch }.collect(Collectors.toList())
        if (matchers.isEmpty()) {
            if (log.isTraceEnabled) {
                log.trace("allow request since did not find matching RequestMatcher")
            }
            return ALLOW
        }
        val comparator = AntPathMatcher().getPatternComparator(
                getRequestPath(request))
        matchers = matchers.stream()
                .sorted { (key): Map.Entry<AntPathRequestMatcher, Set<String?>>, (key1): Map.Entry<AntPathRequestMatcher, Set<String?>> -> comparator.compare(key.pattern, key1.pattern) }
                .collect(Collectors.toList())
        val (key, value) = matchers[0]
        if (matchers.size > 1) {
            val (key1) = matchers[1]
            val pattern1 = key.pattern
            val pattern2 = key1.pattern
            check(comparator.compare(pattern1, pattern2) != 0) {
                "Ambiguous handler methods mapped for HTTP path '" +
                        request.requestURL + "': {" + pattern1 + ", " + pattern2 + "}"
            }
        }
        var authorities: MutableSet<String> = mutableSetOf()
        authorities.addAll(value)
        if (authorities.isEmpty()) {
            authorities = mutableSetOf(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE)
        }
        if (authorities.contains(Anonymous.ROLE_ANONYMOUS_VALUE)) {
            if (securityService.supportsAnonymous()) {
                if (log.isDebugEnabled) {
                    log.debug("权限检查，当前用户权限：{}，当前资源({})需要以下权限之一：{}",
                            StringUtils.collectionToCommaDelimitedString(userAuthorities),
                            request.servletPath,
                            authorities)
                }
                return ALLOW
            } else {
                authorities.remove(Anonymous.ROLE_ANONYMOUS_VALUE)
                authorities.add(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE)
            }
        }
        if (log.isDebugEnabled) {
            log.debug("权限检查，当前用户权限：{}，当前资源({})需要以下权限之一：{}",
                    StringUtils.collectionToCommaDelimitedString(userAuthorities),
                    request.servletPath,
                    authorities)
        }
        val granted = isGranted(authentication.get(), authorities)
        return AuthorizationDecision(granted)
    }

    private fun getRequestPath(request: HttpServletRequest): String {
        var url = request.servletPath
        if (request.pathInfo != null) {
            url += request.pathInfo
        }
        return url
    }

    private fun isGranted(authentication: Authentication?, authorities: Set<String?>): Boolean {
        return authentication != null && authentication.isAuthenticated && isAuthorized(
                authentication, authorities)
    }

    private fun isAuthorized(authentication: Authentication, authorities: Set<String?>): Boolean {
        for (grantedAuthority in authentication.authorities) {
            for (authority in authorities) {
                if (authority == grantedAuthority.authority) {
                    return true
                }
            }
        }
        return false
    }

    fun bindAuthorizationManager() {
        configAuthorities = HashMap(defaultConfigAuthorities)
        val allResources = securityService.findAllResources()
        for (resource in allResources) {
            val ress = resource.ress
            val configAttribute = resource.mark.trim { it <= ' ' }
            Assert.hasText(configAttribute, "权限标记不能为空")
            for (api in ress.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (api.contains(":")) {
                    val methodUrl = api.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val method = methodUrl[0].uppercase(Locale.getDefault())
                    val url = methodUrl[1]
                    for (u in url.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        val urlMatcher = AntPathRequestMatcher(u)
                        if (StringUtils.hasText(method)) {
                            Assert.isNull(configAuthorities[urlMatcher],
                                    "\"" + u + "\"对应RequestMapping不包含请求方法描述，请使用通用路径\"" + u
                                            + "\"配置权限")
                            for (m in method.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                                val authorities = configAuthorities.computeIfAbsent(
                                        AntPathRequestMatcher(u, m)) { _: AntPathRequestMatcher? -> HashSet() }
                                authorities.add(configAttribute)
                            }
                        } else {
                            val authorities = configAuthorities.computeIfAbsent(
                                    urlMatcher) { _: AntPathRequestMatcher? -> HashSet() }
                            authorities.add(configAttribute)
                        }
                    }
                } else {
                    for (u in api.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        val authorities = configAuthorities.computeIfAbsent(
                                AntPathRequestMatcher(u)) { _: AntPathRequestMatcher? -> HashSet() }
                        authorities.add(configAttribute)
                    }
                }
            }
        }
    }

    companion object {
        private val DENY = AuthorizationDecision(false)
        private val ALLOW = AuthorizationDecision(true)
    }
}